package com.example.project2.providers;

import android.net.Uri;
import android.provider.BaseColumns;

public class EmergencyHistoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.project2.provider.emergencyhistory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_HISTORY = "history";

    public static class HistoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HISTORY).build();

        public static final String CONTENT_TYPE =
                "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;
        public static final String CONTENT_ITEM_TYPE =
                "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_HISTORY;

        // Table name
        public static final String TABLE_NAME = "emergency_history";

        // Column names
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_HOSPITAL_NAME = "hospital_name";
        public static final String COLUMN_EMERGENCY_TYPE = "emergency_type";
        public static final String COLUMN_PATIENT_CONDITION = "patient_condition";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_START_TIME = "start_time";
        public static final String COLUMN_END_TIME = "end_time";
        public static final String COLUMN_BLOOD_GROUP = "blood_group";
        public static final String COLUMN_RESPONSE_TIME = "response_time";
    }
}