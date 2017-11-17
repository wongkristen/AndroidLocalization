package com.kristenwong.localizationanddailypath;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.kristenwong.localizationanddailypath.DBSchema.CheckInsTable;
import com.kristenwong.localizationanddailypath.DBSchema.LocationsTable;

/**
 * Created by kristenwong on 11/15/17.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "localizer.db";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table " + CheckInsTable.NAME + "(" +
                                "_id integer primary key autoincrement, " +
                                CheckInsTable.Columns.UUID + ", " +
                                CheckInsTable.Columns.NAME + ", " +
                                CheckInsTable.Columns.LATITUDE + ", " +
                                CheckInsTable.Columns.LONGITUDE + ", " +
                                CheckInsTable.Columns.ADDRESS + ", " +
                                CheckInsTable.Columns.TIME + ")");
        sqLiteDatabase.execSQL("create table " + LocationsTable.NAME + "(" +
                                "_id integer primary key autoincrement, " +
                                LocationsTable.Columns.UUID + ", " +
                                LocationsTable.Columns.NAME + ", " +
                                LocationsTable.Columns.ADDRESS + ", " +
                                LocationsTable.Columns.LATITUDE + ", " +
                                LocationsTable.Columns.LONGITUDE + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
