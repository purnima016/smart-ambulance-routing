package com.example.project2.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.example.project2.model.Hospital;
import com.example.project2.receivers.GeofenceBroadcastReceiver;
import java.util.Collections;

import android.Manifest;
import androidx.core.content.ContextCompat;

public class GeofencingService {
    private static final String TAG = "GeofencingService";

    private final Context context;
    private GeofencingClient geofencingClient;

    public GeofencingService(Context context) {
        this.context = context;
        this.geofencingClient = LocationServices.getGeofencingClient(context);
    }

    public void startGeofencingForHospital(Hospital hospital) {
        Log.d(TAG, "📍 Starting geofencing for hospital: " + hospital.getName());

        // Check if we have the required permissions
        if (!hasRequiredPermissions()) {
            Log.e(TAG, "❌ Missing required permissions for geofencing");
            Toast.makeText(context, "Location permissions required for geofencing", Toast.LENGTH_LONG).show();
            return;
        }

        if (hospital.getLatitude() == 0.0 || hospital.getLongitude() == 0.0) {
            Log.e(TAG, "❌ Invalid hospital coordinates");
            Toast.makeText(context, "Hospital coordinates are invalid", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            Geofence geofence = createGeofence(hospital);
            GeofencingRequest geofencingRequest = createGeofencingRequest(geofence);
            PendingIntent pendingIntent = getGeofencePendingIntent(hospital);

            if (pendingIntent == null) {
                Log.e(TAG, "❌ Failed to create PendingIntent");
                return;
            }

            Log.d(TAG, "🎯 Adding geofence for: " + hospital.getName() + " at " +
                    hospital.getLatitude() + ", " + hospital.getLongitude());

            geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ Geofence ADDED successfully for: " + hospital.getName());
                        Toast.makeText(context,
                                "Geofencing activated - " + hospital.getName() + " will be alerted at 500m",
                                Toast.LENGTH_LONG).show();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ FAILED to add geofence: " + e.getMessage());
                        handleGeofencingError(e);
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "❌ SecurityException: " + e.getMessage());
            Toast.makeText(context, "Location permission denied", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Unexpected error: " + e.getMessage());
            Toast.makeText(context, "Geofencing setup failed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean hasRequiredPermissions() {
        boolean hasFineLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == android.content.pm.PackageManager.PERMISSION_GRANTED;

        // For Android 10 (API 29) and above, background location is required for geofencing
        boolean hasBackgroundLocation = true; // Assume true for older devices

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            hasBackgroundLocation = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
        }

        Log.d(TAG, "📍 Permission check - Fine: " + hasFineLocation + ", Background: " + hasBackgroundLocation);
        return hasFineLocation && hasBackgroundLocation;
    }

    private Geofence createGeofence(Hospital hospital) {
        return new Geofence.Builder()
                .setRequestId("HOSPITAL_" + hospital.getId())
                .setCircularRegion(
                        hospital.getLatitude(),
                        hospital.getLongitude(),
                        500 // 500 meters radius
                )
                .setExpirationDuration(24 * 60 * 60 * 1000) // 24 hours instead of NEVER_EXPIRE
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .setNotificationResponsiveness(30000) // 30 seconds
                .setLoiteringDelay(10000) // 10 seconds
                .build();
    }

    private GeofencingRequest createGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofences(Collections.singletonList(geofence))
                .build();
    }

    private PendingIntent getGeofencePendingIntent(Hospital hospital) {
        try {
            Intent intent = new Intent(context, GeofenceBroadcastReceiver.class);

            // Add hospital data
            if (hospital != null) {
                intent.putExtra("hospital_name", hospital.getName());
                intent.putExtra("hospital_phone", hospital.getPhone());
                intent.putExtra("hospital_lat", hospital.getLatitude());
                intent.putExtra("hospital_lng", hospital.getLongitude());
                intent.setAction("HOSPITAL_GEOFENCE_" + hospital.getId()); // Unique action
            }

            int requestCode = hospital != null ? (int) hospital.getId() : (int) System.currentTimeMillis();

            int flags = PendingIntent.FLAG_UPDATE_CURRENT;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                flags |= PendingIntent.FLAG_MUTABLE; // Use MUTABLE for geofencing
            }

            return PendingIntent.getBroadcast(context, requestCode, intent, flags);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating PendingIntent: " + e.getMessage());
            return null;
        }
    }

    private void handleGeofencingError(Exception e) {
        String errorMessage = "Geofencing setup failed";
        String errorDetails = e.getMessage();

        Log.e(TAG, "🔍 Geofencing error details: " + errorDetails);

        if (errorDetails != null) {
            if (errorDetails.contains("1004") || errorDetails.contains("GEOFENCE_NOT_AVAILABLE")) {
                errorMessage = "Geofencing not available on this device. Please check location settings.";
            } else if (errorDetails.contains("1000")) {
                errorMessage = "Location permission required for geofencing";
            } else if (errorDetails.contains("1002")) {
                errorMessage = "Too many active geofences";
            } else if (errorDetails.contains("7:")) {
                errorMessage = "Network error - please check internet connection";
            }
        }

        // Additional diagnostic logging
        Log.d(TAG, "📱 Device Info - API: " + android.os.Build.VERSION.SDK_INT +
                ", Play Services: " + checkPlayServices());
        Log.d(TAG, "🔐 Permissions - Fine Location: " +
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                        android.content.pm.PackageManager.PERMISSION_GRANTED));

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            Log.d(TAG, "🔐 Permissions - Background Location: " +
                    (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                            android.content.pm.PackageManager.PERMISSION_GRANTED));
        }

        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
    }

    private boolean checkPlayServices() {
        try {
            int resultCode = com.google.android.gms.common.GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(context);
            boolean available = resultCode == com.google.android.gms.common.ConnectionResult.SUCCESS;
            Log.d(TAG, "🔍 Play Services Available: " + available + " (Code: " + resultCode + ")");
            return available;
        } catch (Exception e) {
            Log.e(TAG, "Error checking Play Services: " + e.getMessage());
            return false;
        }
    }

    public void stopGeofencing() {
        if (geofencingClient != null && hasRequiredPermissions()) {
            geofencingClient.removeGeofences(getGeofencePendingIntent(null))
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "✅ All geofences removed successfully");
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "❌ Failed to remove geofences: " + e.getMessage());
                    });
        }
    }

    public boolean canStartGeofencing() {
        return hasRequiredPermissions() && checkPlayServices();
    }

    public String getGeofencingStatus() {
        if (!hasRequiredPermissions()) {
            return "Location permissions required (including background location)";
        }
        if (!checkPlayServices()) {
            return "Google Play Services not available";
        }
        return "Geofencing ready";
    }
}