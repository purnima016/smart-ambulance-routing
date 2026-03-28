package com.example.project2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;
import com.example.project2.DataManager;
import com.example.project2.model.PatientData;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "GeofenceReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "🎯 Geofence triggered - Broadcast received!");

        // Extract hospital data from intent
        String hospitalName = intent.getStringExtra("hospital_name");
        String hospitalPhone = intent.getStringExtra("hospital_phone");
        String hospitalAddress = intent.getStringExtra("hospital_address");
        double hospitalLat = intent.getDoubleExtra("hospital_lat", 0.0);
        double hospitalLng = intent.getDoubleExtra("hospital_lng", 0.0);
        long timestamp = intent.getLongExtra("geofence_timestamp", System.currentTimeMillis());

        Log.d(TAG, "🏥 Hospital: " + hospitalName);
        Log.d(TAG, "📞 Phone: " + hospitalPhone);
        Log.d(TAG, "📍 Coordinates: " + hospitalLat + ", " + hospitalLng);
        Log.d(TAG, "⏰ Timestamp: " + timestamp);

        if (hospitalName != null && hospitalPhone != null) {
            // Show alert to driver
            showToast(context, hospitalName);
            Log.d(TAG, "🚨 Alert triggered for: " + hospitalName);

            // ✅ SEND REAL SMS TO HOSPITAL
            sendSMSToHospital(context, hospitalName, hospitalPhone);

            // Log successful geofence trigger
            Log.d(TAG, "✅ Geofence processing completed for: " + hospitalName);
        } else {
            Log.e(TAG, "❌ Missing hospital data in geofence intent");
            Log.e(TAG, "📋 Intent extras: " + intent.getExtras());
        }
    }

    private void showToast(Context context, String hospitalName) {
        try {
            Toast.makeText(context,
                    "🚨 Ambulance 500m from " + hospitalName + " - Hospital notified via SMS!",
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "✅ Toast shown for: " + hospitalName);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error showing toast: " + e.getMessage());
        }
    }

    // ✅ REAL SMS TO HOSPITAL
    private void sendSMSToHospital(Context context, String hospitalName, String phoneNumber) {
        try {
            // Get patient data for SMS message
            PatientData patientData = DataManager.getPatientData(context);

            String message = buildSMSMessage(hospitalName, patientData);

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            Log.d(TAG, "✅ REAL SMS SENT TO HOSPITAL!");
            Log.d(TAG, "📱 Hospital: " + hospitalName);
            Log.d(TAG, "📞 Phone: " + phoneNumber);
            Log.d(TAG, "💬 Message: " + message);

        } catch (Exception e) {
            Log.e(TAG, "❌ SMS failed: " + e.getMessage());
            try {
                Toast.makeText(context, "Failed to send SMS to hospital: " + e.getMessage(), Toast.LENGTH_LONG).show();
            } catch (Exception toastException) {
                Log.e(TAG, "❌ Error showing failure toast: " + toastException.getMessage());
            }
        }
    }

    private String buildSMSMessage(String hospitalName, PatientData patientData) {
        StringBuilder message = new StringBuilder();
        message.append("🚑 EMERGENCY ALERT - AMBULANCE INBOUND\n\n");
        message.append("Hospital: ").append(hospitalName).append("\n");
        message.append("Status: Arriving in 2-3 minutes\n");
        message.append("Emergency: ").append(patientData != null ? patientData.getEmergencyType() : "Emergency").append("\n");
        message.append("Patient Age: ").append(patientData != null ? patientData.getAgeGroup() : "Unknown").append("\n");
        message.append("Blood Group: ").append(patientData != null ? patientData.getBloodGroup() : "Unknown").append("\n");
        message.append("Condition: ").append(patientData != null ? patientData.getCondition() : "Unknown").append("\n\n");
        message.append("Please prepare emergency reception team.");

        return message.toString();
    }
}