package com.example.project2;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.project2.model.PatientData;
import com.example.project2.model.Hospital;
import com.google.gson.Gson;

public class DataManager {
    private static final String PREFS_NAME = "EmergencyDataPrefs";
    private static final String KEY_PATIENT_DATA = "patient_data";
    private static final String KEY_SELECTED_HOSPITAL = "selected_hospital";
    private static final String KEY_USER_LAT = "user_lat";
    private static final String KEY_USER_LNG = "user_lng";

    // Save Patient Data
    public static void savePatientData(Context context, PatientData patientData) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(patientData);
        prefs.edit().putString(KEY_PATIENT_DATA, json).apply();
    }

    // Get Patient Data
    public static PatientData getPatientData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_PATIENT_DATA, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, PatientData.class);
        }
        return null;
    }

    // Save Selected Hospital
    public static void saveSelectedHospital(Context context, Hospital hospital) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = gson.toJson(hospital);
        prefs.edit().putString(KEY_SELECTED_HOSPITAL, json).apply();
    }

    // Get Selected Hospital
    public static Hospital getSelectedHospital(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_SELECTED_HOSPITAL, null);
        if (json != null) {
            Gson gson = new Gson();
            return gson.fromJson(json, Hospital.class);
        }
        return null;
    }

    // ✅ ADD THIS METHOD: Save User Location
    public static void saveUserLocation(Context context, double lat, double lng) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putFloat(KEY_USER_LAT, (float) lat);
        editor.putFloat(KEY_USER_LNG, (float) lng);
        editor.apply();
    }

    // ✅ ADD THIS METHOD: Get User Location
    public static double[] getUserLocation(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        float lat = prefs.getFloat(KEY_USER_LAT, 0f);
        float lng = prefs.getFloat(KEY_USER_LNG, 0f);

        // Return null if no location is saved (both are 0)
        if (lat == 0f && lng == 0f) {
            return null;
        }

        return new double[]{lat, lng};
    }

    // Clear all data
    public static void clearAllData(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}