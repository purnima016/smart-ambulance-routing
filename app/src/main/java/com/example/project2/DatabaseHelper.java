package com.example.project2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.project2.Driver;
import com.example.project2.model.EmergencyHistory;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    // Database Information
    private static final String DATABASE_NAME = "AmbulanceApp.db";
    private static final int DATABASE_VERSION = 3; // Updated version

    // Table Names
    private static final String TABLE_DRIVERS = "drivers";
    private static final String TABLE_EMERGENCY_HISTORY = "emergency_history";

    // Common Column Names
    private static final String COLUMN_ID = "_id";

    // Drivers Table Columns
    private static final String COLUMN_FIREBASE_UID = "firebase_uid";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_DRIVER_LICENSE = "driver_license";
    private static final String COLUMN_AMBULANCE_NUMBER = "ambulance_number";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_CREATED_AT = "created_at";

    // Emergency History Table Columns
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_HOSPITAL_NAME = "hospital_name";
    private static final String COLUMN_EMERGENCY_TYPE = "emergency_type";
    private static final String COLUMN_PATIENT_CONDITION = "patient_condition";
    private static final String COLUMN_STATUS = "status";
    private static final String COLUMN_START_TIME = "start_time";
    private static final String COLUMN_END_TIME = "end_time";
    private static final String COLUMN_BLOOD_GROUP = "blood_group";
    private static final String COLUMN_RESPONSE_TIME = "response_time";

    // Create Table Queries
    private static final String CREATE_DRIVERS_TABLE =
            "CREATE TABLE " + TABLE_DRIVERS + "(" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    COLUMN_FIREBASE_UID + " TEXT UNIQUE," +
                    COLUMN_USERNAME + " TEXT," +
                    COLUMN_EMAIL + " TEXT," +
                    COLUMN_DRIVER_LICENSE + " TEXT," +
                    COLUMN_AMBULANCE_NUMBER + " TEXT," +
                    COLUMN_PHONE + " TEXT," +
                    COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")";

    private static final String CREATE_EMERGENCY_HISTORY_TABLE =
            "CREATE TABLE " + TABLE_EMERGENCY_HISTORY + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TIMESTAMP + " INTEGER NOT NULL, " +
                    COLUMN_HOSPITAL_NAME + " TEXT NOT NULL, " +
                    COLUMN_EMERGENCY_TYPE + " TEXT NOT NULL, " +
                    COLUMN_PATIENT_CONDITION + " TEXT NOT NULL, " +
                    COLUMN_STATUS + " TEXT NOT NULL, " +
                    COLUMN_START_TIME + " INTEGER NOT NULL, " +
                    COLUMN_END_TIME + " INTEGER NOT NULL, " +
                    COLUMN_BLOOD_GROUP + " TEXT NOT NULL, " +
                    COLUMN_RESPONSE_TIME + " INTEGER NOT NULL" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database tables");
        db.execSQL(CREATE_DRIVERS_TABLE);
        db.execSQL(CREATE_EMERGENCY_HISTORY_TABLE);
        Log.d(TAG, "✅ Database tables created successfully");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop older tables if they exist
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DRIVERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY_HISTORY);

        // Create tables again
        onCreate(db);
        Log.d(TAG, "✅ Database upgraded successfully");
    }

    // ========== DRIVER METHODS ==========

    /**
     * Add new driver to database
     */
    public boolean addDriver(String firebaseUid, String username, String email,
                             String driverLicense, String ambulanceNumber, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_FIREBASE_UID, firebaseUid);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_DRIVER_LICENSE, driverLicense);
        values.put(COLUMN_AMBULANCE_NUMBER, ambulanceNumber);
        values.put(COLUMN_PHONE, phone);

        long result = db.insert(TABLE_DRIVERS, null, values);
        db.close();

        Log.d(TAG, "👤 Driver added: " + (result != -1 ? "SUCCESS" : "FAILED"));
        return result != -1;
    }

    /**
     * Get driver by Firebase UID
     */
    public Driver getDriverByUid(String firebaseUid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Driver driver = null;

        Cursor cursor = db.query(TABLE_DRIVERS,
                new String[]{
                        COLUMN_ID, COLUMN_FIREBASE_UID, COLUMN_USERNAME,
                        COLUMN_EMAIL, COLUMN_DRIVER_LICENSE,
                        COLUMN_AMBULANCE_NUMBER, COLUMN_PHONE, COLUMN_CREATED_AT
                },
                COLUMN_FIREBASE_UID + " = ?",
                new String[]{firebaseUid},
                null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            driver = new Driver();
            driver.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            driver.setFirebaseUid(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIREBASE_UID)));
            driver.setUsername(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)));
            driver.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL)));
            driver.setDriverLicense(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DRIVER_LICENSE)));
            driver.setAmbulanceNumber(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AMBULANCE_NUMBER)));
            driver.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
            driver.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));

            cursor.close();
            Log.d(TAG, "✅ Driver found: " + driver.getUsername());
        } else {
            Log.d(TAG, "❌ Driver not found for UID: " + firebaseUid);
        }
        db.close();
        return driver;
    }

    /**
     * Check if driver exists
     */
    public boolean driverExists(String firebaseUid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DRIVERS,
                new String[]{COLUMN_ID},
                COLUMN_FIREBASE_UID + " = ?",
                new String[]{firebaseUid},
                null, null, null, null);

        boolean exists = (cursor != null && cursor.getCount() > 0);
        if (cursor != null) cursor.close();
        db.close();

        Log.d(TAG, "🔍 Driver exists: " + exists + " for UID: " + firebaseUid);
        return exists;
    }

    /**
     * Update driver profile
     */
    public boolean updateDriver(String firebaseUid, String username, String driverLicense,
                                String ambulanceNumber, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_DRIVER_LICENSE, driverLicense);
        values.put(COLUMN_AMBULANCE_NUMBER, ambulanceNumber);
        values.put(COLUMN_PHONE, phone);

        int result = db.update(TABLE_DRIVERS, values,
                COLUMN_FIREBASE_UID + " = ?", new String[]{firebaseUid});
        db.close();

        Log.d(TAG, "📝 Driver updated: " + (result > 0 ? "SUCCESS" : "FAILED"));
        return result > 0;
    }

    // ========== EMERGENCY HISTORY METHODS ==========

    /**
     * Add emergency history with all details
     */
    public boolean addEmergencyHistory(EmergencyHistory history) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TIMESTAMP, history.getTimestamp());
        values.put(COLUMN_HOSPITAL_NAME, history.getHospitalName());
        values.put(COLUMN_EMERGENCY_TYPE, history.getEmergencyType());
        values.put(COLUMN_PATIENT_CONDITION, history.getPatientCondition());
        values.put(COLUMN_STATUS, history.getStatus());
        values.put(COLUMN_START_TIME, history.getStartTime());
        values.put(COLUMN_END_TIME, history.getEndTime());
        values.put(COLUMN_BLOOD_GROUP, history.getBloodGroup());
        values.put(COLUMN_RESPONSE_TIME, history.getResponseTime());

        long result = db.insert(TABLE_EMERGENCY_HISTORY, null, values);
        db.close();

        boolean success = result != -1;
        Log.d(TAG, "📝 Emergency history added: " +
                (success ? "SUCCESS, ID: " + result : "FAILED") +
                " | Hospital: " + history.getHospitalName());

        return success;
    }

    /**
     * Get all emergency history sorted by timestamp (newest first)
     */
    public List<EmergencyHistory> getAllEmergencyHistory() {
        List<EmergencyHistory> historyList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EMERGENCY_HISTORY,
                null, null, null, null, null, COLUMN_TIMESTAMP + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                EmergencyHistory history = new EmergencyHistory();
                history.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                history.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
                history.setHospitalName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOSPITAL_NAME)));
                history.setEmergencyType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMERGENCY_TYPE)));
                history.setPatientCondition(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATIENT_CONDITION)));
                history.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
                history.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
                history.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
                history.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUP)));
                history.setResponseTime(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESPONSE_TIME)));

                historyList.add(history);
            } while (cursor.moveToNext());

            cursor.close();
            Log.d(TAG, "✅ Loaded " + historyList.size() + " emergency history records");
        } else {
            Log.d(TAG, "ℹ️ No emergency history records found");
        }
        db.close();
        return historyList;
    }

    /**
     * Get emergency history by ID
     */
    public EmergencyHistory getEmergencyHistoryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        EmergencyHistory history = null;

        Cursor cursor = db.query(TABLE_EMERGENCY_HISTORY,
                null,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            history = new EmergencyHistory();
            history.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
            history.setTimestamp(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)));
            history.setHospitalName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_HOSPITAL_NAME)));
            history.setEmergencyType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMERGENCY_TYPE)));
            history.setPatientCondition(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PATIENT_CONDITION)));
            history.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_STATUS)));
            history.setStartTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_START_TIME)));
            history.setEndTime(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_END_TIME)));
            history.setBloodGroup(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUP)));
            history.setResponseTime(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESPONSE_TIME)));

            cursor.close();
            Log.d(TAG, "✅ Found emergency history with ID: " + id);
        } else {
            Log.d(TAG, "❌ Emergency history not found for ID: " + id);
        }
        db.close();
        return history;
    }

    /**
     * Delete emergency history by ID
     */
    public boolean deleteEmergencyHistory(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EMERGENCY_HISTORY,
                COLUMN_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();

        Log.d(TAG, "🗑️ Emergency history deleted: " + (result > 0 ? "SUCCESS" : "FAILED"));
        return result > 0;
    }

    /**
     * Clear all emergency history
     */
    public boolean clearAllEmergencyHistory() {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_EMERGENCY_HISTORY, null, null);
        db.close();

        Log.d(TAG, "🗑️ All emergency history cleared: " + (result > 0 ? "SUCCESS" : "FAILED"));
        return result > 0;
    }

    /**
     * Get emergency history count
     */
    public int getEmergencyHistoryCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_EMERGENCY_HISTORY, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        db.close();

        Log.d(TAG, "📊 Emergency history count: " + count);
        return count;
    }

    /**
     * Direct database method to add emergency history (for Content Provider)
     */
    public long insertEmergencyHistory(ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();
        long result = db.insert(TABLE_EMERGENCY_HISTORY, null, values);
        db.close();

        Log.d(TAG, "📝 Direct insert emergency history: " + (result != -1 ? "SUCCESS, ID: " + result : "FAILED"));
        return result;
    }

    /**
     * Direct database method to query emergency history (for Content Provider)
     */
    public Cursor queryEmergencyHistory(String[] projection, String selection,
                                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EMERGENCY_HISTORY,
                projection, selection, selectionArgs, null, null, sortOrder);

        Log.d(TAG, "🔍 Query emergency history, rows: " + (cursor != null ? cursor.getCount() : 0));
        return cursor;
    }
}