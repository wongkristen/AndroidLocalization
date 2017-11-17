package com.kristenwong.localizationanddailypath;

/**
 * Created by kristenwong on 11/15/17.
 */

public class DBSchema {
    public static final class CheckInsTable {
        public static final String NAME = "checkins";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String NAME = "name";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
            public static final String TIME = "time";
            public static final String ADDRESS = "address";
        }
    }

    public static final class LocationsTable {
        public static final String NAME = "locations";

        public static final class Columns {
            public static final String UUID = "uuid";
            public static final String NAME = "name";
            public static final String ADDRESS = "address";
            public static final String LATITUDE = "latitude";
            public static final String LONGITUDE = "longitude";
        }
    }

}
