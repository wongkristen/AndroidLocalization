package com.kristenwong.localizationanddailypath;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.util.Log;

import com.kristenwong.localizationanddailypath.DBSchema.CheckInsTable;
import com.kristenwong.localizationanddailypath.DBSchema.LocationsTable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Created by kristenwong on 11/15/17.
 */

public class CheckInsManager {
    private static CheckInsManager checkInsManager;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private static final String MAIN_DEBUG_TAG = "MainActivity";

    public static CheckInsManager get(Context context) {
        if(checkInsManager == null)
            checkInsManager = new CheckInsManager(context);
        return checkInsManager;
    }

    private CheckInsManager(Context context) {
        mContext = context;
        mDatabase = new DBHelper(mContext).getWritableDatabase();
    }

    private static ContentValues getCheckInContentValues(CheckIn checkIn) {
        ContentValues values = new ContentValues();
        values.put(CheckInsTable.Columns.UUID, checkIn.getUuid().toString());
        values.put(CheckInsTable.Columns.NAME, checkIn.getName());
        values.put(CheckInsTable.Columns.LATITUDE, checkIn.getLatitude());
        values.put(CheckInsTable.Columns.LONGITUDE, checkIn.getLongitude());
        values.put(CheckInsTable.Columns.TIME, checkIn.getTime());
        values.put(CheckInsTable.Columns.ADDRESS, checkIn.getAddress());

        return values;
    }

    private static ContentValues getLocationContentValues(SavedLocation location) {
        ContentValues values = new ContentValues();
        values.put(LocationsTable.Columns.UUID, location.getUuid().toString());
        values.put(LocationsTable.Columns.NAME, location.getName());
        values.put(LocationsTable.Columns.LATITUDE, location.getLatitude());
        values.put(LocationsTable.Columns.LONGITUDE, location.getLongitude());
        values.put(LocationsTable.Columns.ADDRESS, location.getAddress());

        return values;
    }

    public void addCheckIn(CheckIn checkIn) {
        CheckInCursorWrapper locationCursor = queryLocations(null, null);
        boolean newLocation = true;
        try {
            locationCursor.moveToFirst();

            Log.d(MAIN_DEBUG_TAG, "addCheckIn: initialized location cursor");
            while(!locationCursor.isAfterLast()) {
                Log.d(MAIN_DEBUG_TAG, "addCheckIn: loop iteration - checking within 30m");
                SavedLocation location = locationCursor.getSavedLocation();

                if(calculateWithin30m(checkIn.getLatitude(),
                                      checkIn.getLongitude(),
                                      location.getLatitude(),
                                      location.getLongitude())) {
                    checkIn.setAddress(location.getAddress());
                    checkIn.setName(location.getName());
                    newLocation = false;
                    break;
                }
                locationCursor.moveToNext();
            }
        } finally {
            locationCursor.close();
        }

        if (newLocation) {
            Log.d(MAIN_DEBUG_TAG, "addCheckIn: adding new location");
            SavedLocation location = new SavedLocation();
            location.setName(checkIn.getName());
            location.setLatitude(checkIn.getLatitude());
            location.setLongitude(checkIn.getLongitude());
            location.setAddress(checkIn.getAddress());
            mDatabase.insert(LocationsTable.NAME, null, getLocationContentValues(location));
        }
        ContentValues values = getCheckInContentValues(checkIn);
        mDatabase.insert(CheckInsTable.NAME, null, values);
    }

    public void addSavedLocation(SavedLocation savedLocation) {
        mDatabase.insert(LocationsTable.NAME, null, getLocationContentValues(savedLocation));
    }

    public void updateCheckIn(CheckIn checkIn) {
        String uuid = checkIn.getUuid().toString();
        ContentValues values = getCheckInContentValues(checkIn);
        mDatabase.update(CheckInsTable.NAME, values, CheckInsTable.Columns.UUID +
                            " = ?", new String[] {uuid});
    }

    public void updateSavedLocation(SavedLocation savedLocation) {
        String uuid = savedLocation.getUuid().toString();
        ContentValues values = getLocationContentValues(savedLocation);
        mDatabase.update(LocationsTable.NAME, values, LocationsTable.Columns.UUID +
                            " = ?", new String[] {uuid});
    }

    private CheckInCursorWrapper queryCheckIns(String whereClause, String[] whereArgs) {
        Cursor cursor =  mDatabase.query(
                CheckInsTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return new CheckInCursorWrapper(cursor);
    }

    private CheckInCursorWrapper queryLocations(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                LocationsTable.NAME,
                null,
                whereClause,
                whereArgs,
                null,
                null,
                null);
        return new CheckInCursorWrapper(cursor);
    }

    public List<CheckIn> getCheckIns() {
        List<CheckIn> checkIns = new ArrayList<>();

//        CheckInCursorWrapper cursor = queryCheckIns(null, null);
//        Query check ins in alphabetical order by name
        Cursor c = mDatabase.query(CheckInsTable.NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        CheckInsTable.Columns.NAME + " ASC");
        CheckInCursorWrapper cursor = new CheckInCursorWrapper(c);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                checkIns.add(cursor.getCheckIn());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return checkIns;
    }

    public List<SavedLocation> getSavedLocations() {
        List<SavedLocation> savedLocations = new ArrayList<>();
        CheckInCursorWrapper cursor = queryLocations(null, null);

        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                savedLocations.add(cursor.getSavedLocation());
                cursor.moveToNext();
            }
        } finally {
            cursor.close();
        }
        return savedLocations;
    }

    public CheckIn getCheckIn(UUID uuid) {
        CheckInCursorWrapper cursor = queryCheckIns(CheckInsTable.Columns.UUID +
                                        " = ?", new String[] {uuid.toString()});
        try {
            if (cursor.getCount() == 0) return null;

            cursor.moveToFirst();
            return cursor.getCheckIn();
        } finally {
            cursor.close();
        }
    }

    public CheckIn getLatestCheckin(String locationName) {
        Cursor cursor;
        cursor = mDatabase.query(CheckInsTable.NAME,
                null,
                CheckInsTable.Columns.NAME + " = ?",
                new String[] {locationName},
                null,
                null,
                CheckInsTable.Columns.TIME + " DESC",
                "1");

        CheckInCursorWrapper checkInCursor = new CheckInCursorWrapper(cursor);
        try{
            checkInCursor.moveToFirst();
            if (!checkInCursor.isAfterLast()) {
                return checkInCursor.getCheckIn();
            }
        } finally {
            checkInCursor.close();
        }
        return null;
    }

    public boolean calculateWithin30m(double lat1, double lon1, double lat2, double lon2) {
        float ans[] = {1, 1, 1};
        Location.distanceBetween(lat1, lon1, lat2, lon2, ans);
        return (ans[0] <= 30);
    }
}
