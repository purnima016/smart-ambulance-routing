package com.example.project2.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.project2.model.Hospital;
import java.util.ArrayList;
import java.util.List;

public class HospitalDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "Hospitals.db";
    private static final int DATABASE_VERSION = 2; // ✅ CHANGED FROM 1 TO 2

    private static final String TABLE_HOSPITALS = "hospitals";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_PHONE = "phone";
    private static final String COLUMN_LATITUDE = "latitude";
    private static final String COLUMN_LONGITUDE = "longitude";
    private static final String COLUMN_EMERGENCY_TYPES = "emergency_types";
    private static final String COLUMN_BLOOD_GROUPS = "blood_groups";
    private static final String COLUMN_AVAILABLE_BEDS = "available_beds";
    private static final String COLUMN_ICU_BEDS = "icu_beds";
    private static final String COLUMN_SPECIALTIES = "specialties";
    private static final String COLUMN_RESPONSE_TIME = "response_time";

    private static final String CREATE_HOSPITALS_TABLE = "CREATE TABLE " + TABLE_HOSPITALS + "("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_NAME + " TEXT,"
            + COLUMN_ADDRESS + " TEXT,"
            + COLUMN_PHONE + " TEXT,"
            + COLUMN_LATITUDE + " REAL,"
            + COLUMN_LONGITUDE + " REAL,"
            + COLUMN_EMERGENCY_TYPES + " TEXT,"
            + COLUMN_BLOOD_GROUPS + " TEXT,"
            + COLUMN_AVAILABLE_BEDS + " INTEGER,"
            + COLUMN_ICU_BEDS + " INTEGER,"
            + COLUMN_SPECIALTIES + " TEXT,"
            + COLUMN_RESPONSE_TIME + " INTEGER"
            + ")";

    public HospitalDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_HOSPITALS_TABLE);
        insertSampleHospitals(db);
        Log.d("DATABASE", "✅ New database created with version " + DATABASE_VERSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DATABASE", "🔄 Upgrading database from version " + oldVersion + " to " + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HOSPITALS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void insertSampleHospitals(SQLiteDatabase db) {
        Log.d("DATABASE", "🗂️ Inserting hospitals with NEW coordinates...");

        // 15+ Pondicherry Hospitals with UPDATED coordinates
        addHospital(db, "JIPMER Hospital", "JIPMER Campus, Dhanvantari Nagar, Gorimedu", "0413-2272380",
                11.9562, 79.8001, "CARDIAC,ACCIDENT,STROKE,BREATHING", "A+,B+,O+,AB+", 45, 15, "Cardiology,Neurology,Trauma", 5);

        addHospital(db, "Indira Gandhi Medical College", "Indira Nagar, Moolakulam", "0413-2276801",
                11.944200, 79.799700, "CARDIAC,ACCIDENT,STROKE", "A+,B+,O+", 30, 10, "General Medicine,Surgery", 8);

        addHospital(db, "Apollo Hospital", "Villianur Road, Muthialpet", "0413-2333888",
                11.9390, 79.8093, "CARDIAC,STROKE,BREATHING", "A+,B+,O+,AB+", 25, 8, "Cardiology,Neurology", 6);

        addHospital(db, "Mahatma Gandhi Medical College", "Pillayarkuppam", "0413-2656565",
                11.8123, 79.7788, "ACCIDENT,STROKE,BREATHING", "A+,O+", 20, 6, "Trauma,Orthopedics", 7);

        addHospital(db, "Pondicherry Institute of Medical Sciences", "Kalathumettupathai", "0413-2656777",
                12.0505, 79.8642, "CARDIAC,ACCIDENT", "B+,O+,AB+", 35, 12, "Cardiology,Trauma", 10);

        addHospital(db, "Mother Hospital", "100 Feet Road, Vaithikuppam", "0413-2290990",
                11.9577, 79.8118, "STROKE,BREATHING", "A+,B+", 15, 4, "Neurology,Pulmonology", 12);

        addHospital(db, "Sri Manakula Vinayagar Medical College", "Kalitheerthalkuppam", "0413-2644000",
                11.9213, 79.6292, "CARDIAC,ACCIDENT,STROKE", "O+,AB+", 28, 9, "Cardiology,Trauma,Neurology", 9);

        addHospital(db, "Aarthi Scans", "JN Street, White Town", "0413-2221122",
                11.9331, 79.8315, "CARDIAC,STROKE", "A+,B+", 12, 3, "Diagnostics,Imaging", 15);

        addHospital(db, "New Medical Centre", "Sardar Vallabhbhai Patel Road", "0413-2223366",
                11.9334, 79.8290, "ACCIDENT,BREATHING", "O+", 18, 5, "Emergency Care", 8);

        addHospital(db, "Kamaraj Medical Centre", "M.G. Road, Heritage Town", "0413-2224455",
                11.9351, 79.8274, "CARDIAC,BREATHING", "A+,AB+", 22, 7, "Cardiology,Respiratory", 11);

        addHospital(db, "Sudha Hospital", "Puducherry - Villianur Road", "0413-2299887",
                11.9224, 79.8022, "ACCIDENT,STROKE", "B+,O+", 16, 4, "Trauma,Neurology", 13);

        addHospital(db, "Jaya Hospital", "Anna Nagar, Mudaliarpet", "0413-2278899",
                11.9493, 79.7956, "CARDIAC,ACCIDENT,BREATHING", "A+,O+,AB+", 24, 8, "Cardiology,Trauma", 7);

        addHospital(db, "Grace Hospital", "Reddiarpalayam", "0413-2200678",
                11.9300, 79.7975, "STROKE,BREATHING", "A+,B+,O+", 14, 3, "Neurology,Pulmonology", 14);

        addHospital(db, "Vinayaka Mission Hospital", "Pillaiyarkuppam", "0413-2654321",
                11.8317, 79.7825, "CARDIAC,ACCIDENT,STROKE", "B+,AB+", 26, 8, "Cardiology,Trauma", 10);

        addHospital(db, "LifeCare Hospital", "Thavalakuppam", "0413-2298765",
                11.9083, 79.8235, "ACCIDENT,BREATHING", "A+,O+", 19, 5, "Emergency Care,Trauma", 9);

        addHospital(db, "East Coast Hospitals", "Puducherry", "0413-2298000",
                11.9397, 79.8066, "CARDIAC,ACCIDENT,STROKE", "A+,B+,O+", 20, 6, "Multi-speciality", 8);

        addHospital(db, "Westmed Hospital", "Bharathidasan St", "0413-2225566",
                11.9512, 79.8154, "ACCIDENT,STROKE", "A+,O+", 16, 4, "General Medicine", 9);

        addHospital(db, "Be Well Hospitals", "Puducherry", "0413-2277000",
                11.9535, 79.8166, "CARDIAC,BREATHING", "B+,AB+", 18, 5, "Cardiology,Respiratory", 7);

        Log.d("DATABASE", "✅ All hospitals inserted with NEW coordinates");
    }

    private void addHospital(SQLiteDatabase db, String name, String address, String phone,
                             double lat, double lng, String emergencies, String blood,
                             int beds, int icu, String specialties, int response) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_ADDRESS, address);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_LATITUDE, lat);
        values.put(COLUMN_LONGITUDE, lng);
        values.put(COLUMN_EMERGENCY_TYPES, emergencies);
        values.put(COLUMN_BLOOD_GROUPS, blood);
        values.put(COLUMN_AVAILABLE_BEDS, beds);
        values.put(COLUMN_ICU_BEDS, icu);
        values.put(COLUMN_SPECIALTIES, specialties);
        values.put(COLUMN_RESPONSE_TIME, response);

        long id = db.insert(TABLE_HOSPITALS, null, values);
        Log.d("DATABASE", "➕ Hospital: " + name + " | Lat: " + lat + " | Lng: " + lng + " | ID: " + id);
    }

    public List<Hospital> getAllHospitals() {
        List<Hospital> hospitals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_HOSPITALS, null, null, null, null, null, null);

        Log.d("DATABASE", "📊 Loading hospitals from database...");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Hospital hospital = cursorToHospital(cursor);
                hospitals.add(hospital);
                // Log each hospital's coordinates
                Log.d("DATABASE_LOAD", "🏥 " + hospital.getName() +
                        " | Lat: " + hospital.getLatitude() +
                        " | Lng: " + hospital.getLongitude());
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            Log.d("DATABASE", "❌ No hospitals found in database");
        }
        db.close();

        Log.d("DATABASE", "✅ Loaded " + hospitals.size() + " hospitals");
        return hospitals;
    }

    public List<Hospital> getHospitalsByEmergencyType(String emergencyType) {
        List<Hospital> hospitals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_EMERGENCY_TYPES + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + emergencyType + "%"};

        Cursor cursor = db.query(TABLE_HOSPITALS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Hospital hospital = cursorToHospital(cursor);
                hospitals.add(hospital);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return hospitals;
    }

    public List<Hospital> getHospitalsByBloodGroup(String bloodGroup) {
        List<Hospital> hospitals = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_BLOOD_GROUPS + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + bloodGroup + "%"};

        Cursor cursor = db.query(TABLE_HOSPITALS, null, selection, selectionArgs, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Hospital hospital = cursorToHospital(cursor);
                hospitals.add(hospital);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
        return hospitals;
    }

    // SIMPLIFIED FILTERING METHOD
    public List<Hospital> getHospitalsByPreferences(String emergencyType, String bloodGroup,
                                                    String paymentType, String priorityType) {

        Log.d("DATABASE", "🔍 Filtering: " + emergencyType + " + " + bloodGroup + " + " + paymentType + " + " + priorityType);

        List<Hospital> allHospitals = getAllHospitals();
        List<Hospital> perfectMatches = new ArrayList<>();
        List<Hospital> emergencyMatches = new ArrayList<>();

        // First, find perfect matches (emergency + blood group)
        for (Hospital hospital : allHospitals) {
            boolean matchesEmergency = hospital.getEmergencyTypes().toLowerCase()
                    .contains(emergencyType.toLowerCase());

            boolean matchesBlood = bloodGroup.equals("ANY") ||
                    hospital.getBloodGroups().toLowerCase()
                            .contains(bloodGroup.toLowerCase());

            if (matchesEmergency && matchesBlood) {
                perfectMatches.add(hospital);
            } else if (matchesEmergency) {
                emergencyMatches.add(hospital);
            }
        }

        // Return perfect matches if found
        if (!perfectMatches.isEmpty()) {
            Log.d("DATABASE", "✅ Found " + perfectMatches.size() + " perfect matches");
            return perfectMatches;
        }

        // If no perfect matches, return hospitals with matching emergency type
        if (!emergencyMatches.isEmpty()) {
            Log.d("DATABASE", "🔄 No perfect matches, showing " + emergencyMatches.size() + " emergency type matches");
            return emergencyMatches;
        }

        // If nothing matches, return ALL hospitals
        Log.d("DATABASE", "📋 No matches found, showing ALL " + allHospitals.size() + " hospitals sorted by distance");
        return allHospitals;
    }

    // ✅ ADD THIS METHOD: Update Blood Stock
    public boolean updateBloodStock(String hospitalName, String bloodGroup, boolean isAvailable) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Get current blood groups for the hospital
        Cursor cursor = db.query(TABLE_HOSPITALS,
                new String[]{COLUMN_BLOOD_GROUPS},
                COLUMN_NAME + " = ?",
                new String[]{hospitalName},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String currentBloodGroups = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUPS));
            cursor.close();

            // Update blood groups
            List<String> bloodGroupsList = new ArrayList<>();
            if (currentBloodGroups != null && !currentBloodGroups.isEmpty()) {
                String[] groups = currentBloodGroups.split(",");
                for (String group : groups) {
                    if (!group.trim().isEmpty()) {
                        bloodGroupsList.add(group.trim());
                    }
                }
            }

            if (isAvailable) {
                // Add blood group if not already present
                if (!bloodGroupsList.contains(bloodGroup)) {
                    bloodGroupsList.add(bloodGroup);
                }
            } else {
                // Remove blood group
                bloodGroupsList.remove(bloodGroup);
            }

            // Convert back to comma-separated string
            StringBuilder newBloodGroups = new StringBuilder();
            for (int i = 0; i < bloodGroupsList.size(); i++) {
                if (i > 0) newBloodGroups.append(",");
                newBloodGroups.append(bloodGroupsList.get(i));
            }

            // Update database
            ContentValues values = new ContentValues();
            values.put(COLUMN_BLOOD_GROUPS, newBloodGroups.toString());

            int rowsAffected = db.update(TABLE_HOSPITALS, values,
                    COLUMN_NAME + " = ?", new String[]{hospitalName});

            db.close();
            return rowsAffected > 0;
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return false;
    }

    // HELPER METHOD: Convert cursor to Hospital object
    private Hospital cursorToHospital(Cursor cursor) {
        Hospital hospital = new Hospital();
        hospital.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        hospital.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
        hospital.setAddress(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)));
        hospital.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
        hospital.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
        hospital.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));
        hospital.setEmergencyTypes(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMERGENCY_TYPES)));
        hospital.setBloodGroups(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BLOOD_GROUPS)));
        hospital.setAvailableBeds(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_AVAILABLE_BEDS)));
        hospital.setIcuBeds(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ICU_BEDS)));
        hospital.setSpecialties(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SPECIALTIES)));
        hospital.setResponseTime(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESPONSE_TIME)));
        return hospital;
    }
}