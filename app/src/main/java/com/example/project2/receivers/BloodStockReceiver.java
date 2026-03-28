package com.example.project2.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class BloodStockReceiver extends BroadcastReceiver {

    private static final String TAG = "BloodStockReceiver";

    // Define action constants
    public static final String ACTION_BLOOD_STOCK_UPDATE = "BLOOD_STOCK_UPDATE";
    public static final String ACTION_URGENT_BLOOD_REQUEST = "URGENT_BLOOD_REQUEST";
    public static final String ACTION_HOSPITAL_STATUS_UPDATE = "HOSPITAL_STATUS_UPDATE";

    // Extra keys
    public static final String EXTRA_HOSPITAL_NAME = "hospitalName";
    public static final String EXTRA_BLOOD_GROUP = "bloodGroup";
    public static final String EXTRA_NEW_STATUS = "newStatus";
    public static final String EXTRA_OLD_STATUS = "oldStatus";

    // Interface for callback
    public interface OnBloodStockUpdateListener {
        void onBloodStockUpdated(String hospitalName, String bloodGroup, String newStatus);
    }

    private OnBloodStockUpdateListener listener;

    public BloodStockReceiver() {
        // Default constructor
    }

    public BloodStockReceiver(OnBloodStockUpdateListener listener) {
        this.listener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Log.d(TAG, "📡 Broadcast received: " + action);

        if (action != null) {
            switch (action) {
                case ACTION_BLOOD_STOCK_UPDATE:
                    handleBloodStockUpdate(context, intent);
                    break;

                case ACTION_URGENT_BLOOD_REQUEST:
                    handleUrgentBloodRequest(context, intent);
                    break;

                case ACTION_HOSPITAL_STATUS_UPDATE:
                    handleHospitalStatusUpdate(context, intent);
                    break;
            }
        }
    }

    private void handleBloodStockUpdate(Context context, Intent intent) {
        String hospitalName = intent.getStringExtra(EXTRA_HOSPITAL_NAME);
        String bloodGroup = intent.getStringExtra(EXTRA_BLOOD_GROUP);
        String newStatus = intent.getStringExtra(EXTRA_NEW_STATUS);
        String oldStatus = intent.getStringExtra(EXTRA_OLD_STATUS);

        Log.d(TAG, "🩸 Blood Stock Update - Hospital: " + hospitalName +
                ", Blood: " + bloodGroup + ", Status: " + newStatus);

        // Show notification to user
        String message = "🩸 " + hospitalName + " - Blood " + bloodGroup + " is now " + newStatus;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        // Notify listener if available
        if (listener != null) {
            listener.onBloodStockUpdated(hospitalName, bloodGroup, newStatus);
        }

        // Forward to LocalBroadcast for fragments
        Intent localIntent = new Intent(ACTION_BLOOD_STOCK_UPDATE);
        localIntent.putExtra(EXTRA_HOSPITAL_NAME, hospitalName);
        localIntent.putExtra(EXTRA_BLOOD_GROUP, bloodGroup);
        localIntent.putExtra(EXTRA_NEW_STATUS, newStatus);
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent);
    }

    private void handleUrgentBloodRequest(Context context, Intent intent) {
        String patientBloodGroup = intent.getStringExtra("patientBloodGroup");
        String currentHospital = intent.getStringExtra("currentHospital");
        String patientCondition = intent.getStringExtra("patientCondition");

        Log.d(TAG, "🚨 URGENT Blood Request - Blood: " + patientBloodGroup +
                ", Hospital: " + currentHospital + ", Condition: " + patientCondition);

        String message = "🚨 URGENT: Need blood " + patientBloodGroup + " at " + currentHospital;
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    private void handleHospitalStatusUpdate(Context context, Intent intent) {
        String hospitalName = intent.getStringExtra("hospitalName");
        int availableBeds = intent.getIntExtra("availableBeds", 0);
        int icuBeds = intent.getIntExtra("icuBeds", 0);

        Log.d(TAG, "🏥 Hospital Status Update - " + hospitalName +
                ", Beds: " + availableBeds + ", ICU: " + icuBeds);

        String message = "🏥 " + hospitalName + " - " + availableBeds + " beds available";
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public void setListener(OnBloodStockUpdateListener listener) {
        this.listener = listener;
    }
}