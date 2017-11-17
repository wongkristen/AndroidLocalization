package com.kristenwong.localizationanddailypath;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.kristenwong.localizationanddailypath.DBSchema.CheckInsTable;
import com.kristenwong.localizationanddailypath.DBSchema.LocationsTable;

import java.util.UUID;

/**
 * Created by kristenwong on 11/15/17.
 */

public class CheckInCursorWrapper extends CursorWrapper {
    public CheckInCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public CheckIn getCheckIn() {
        String uuid = getString(getColumnIndex(CheckInsTable.Columns.UUID));
        String name = getString(getColumnIndex(CheckInsTable.Columns.NAME));
        double latitude = getDouble(getColumnIndex(CheckInsTable.Columns.LATITUDE));
        double longitude = getDouble(getColumnIndex(CheckInsTable.Columns.LONGITUDE));
        String time = getString(getColumnIndex(CheckInsTable.Columns.TIME));
        String address = getString(getColumnIndex(CheckInsTable.Columns.ADDRESS));

        CheckIn checkIn = new CheckIn(UUID.fromString(uuid));
        checkIn.setName(name);
        checkIn.setLatitude(latitude);
        checkIn.setLongitude(longitude);
        checkIn.setTime(time);
        checkIn.setAddress(address);
        return checkIn;
    }

    public SavedLocation getSavedLocation(){
        String uuid = getString((getColumnIndex(LocationsTable.Columns.UUID)));
        String name = getString(getColumnIndex(LocationsTable.Columns.NAME));
        double latitude = getDouble(getColumnIndex(LocationsTable.Columns.LATITUDE));
        double longitude = getDouble(getColumnIndex(LocationsTable.Columns.LONGITUDE));
        String address = getString(getColumnIndex(LocationsTable.Columns.ADDRESS));

        return new SavedLocation(UUID.fromString(uuid), name, latitude, longitude, address);
    }
}
