package com.example.project2.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public class DriverContract {

    public static final String CONTENT_AUTHORITY = "com.example.project2.provider.driver";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_DRIVERS = "drivers";

    public static class DriverEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_DRIVERS).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_DRIVERS;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_DRIVERS;

        // Table name
        public static final String TABLE_NAME = "drivers";

        // Column names
        public static final String COLUMN_FIREBASE_UID = "firebase_uid";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_DRIVER_LICENSE = "driver_license";
        public static final String COLUMN_AMBULANCE_NUMBER = "ambulance_number";
        public static final String COLUMN_PHONE = "phone";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
}