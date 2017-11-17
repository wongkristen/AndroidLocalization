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

            while(!locationCursor.isAfterLast()) {
//                TODO: check if the check in is within 30m of previous locations

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
            }
        } finally {
            locationCursor.close();
        }

        if (newLocation) {
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

    public void updateCheckIn(CheckIn checkIn) {
        String uuid = checkIn.getUuid().toString();
        ContentValues values = getCheckInContentValues(checkIn);
        mDatabase.update(CheckInsTable.NAME, values, CheckInsTable.Columns.UUID +
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
        Log.d(MAIN_DEBUG_TAG, "getCheckIns: called");
        List<CheckIn> checkIns = new ArrayList<>();

        CheckInCursorWrapper cursor = queryCheckIns(null, null);
        Log.d(MAIN_DEBUG_TAG, "getCheckIns: queryCheckIns called and returned");
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                checkIns.add(cursor.getCheckIn());
                cursor.moveToNext();
            }
            Log.d(MAIN_DEBUG_TAG, "getCheckIns: cursor moved through checkins");
        } finally {
            cursor.close();
        }
        return checkIns;
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

    private boolean calculateWithin30m(double lat1, double lon1, double lat2, double lon2) {
        float ans[] = {};
        Location.distanceBetween(lat1, lon1, lat1, lon2, ans);
        return (ans[0] <= 30);
    }
}
