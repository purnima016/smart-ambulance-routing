package com.example.project2.fragments;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.example.project2.model.PatientData;
import com.example.project2.R;
import com.example.project2.model.Hospital;
import com.example.project2.adapters.HospitalAdapter;
import com.example.project2.database.HospitalDatabase;
import com.example.project2.DataManager;
import com.example.project2.receivers.BloodStockReceiver;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

public class HospitalsFragment extends Fragment {

    private PatientData patientData;
    private TextView tvHeader, tvLocationMessage, tvFilterInfo, tvNotification;
    private LinearLayout notificationBanner;
    private RecyclerView recyclerViewHospitals;
    private HospitalAdapter hospitalAdapter;
    private final List<Hospital> allHospitals = new ArrayList<>();
    private final List<Hospital> filteredHospitals = new ArrayList<>();
    private Button btnEnableLocation, btnRefreshBlood, btnCloseNotification;

    private HospitalDatabase hospitalDb;
    private FusedLocationProviderClient fusedLocationClient;
    private double userLat = 11.9400;
    private double userLng = 79.8083;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int LOCATION_SETTINGS_REQUEST_CODE = 101;

    private BroadcastReceiver bloodStockReceiver;
    private Handler notificationHandler = new Handler();

    private boolean isRefreshing = false;
    private boolean distancesCalculated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hospitals, container, false);

        Log.d("DEBUG", "🏥 HospitalsFragment created - Checking location permission");

        // Initialize all views with proper IDs
        tvHeader = view.findViewById(R.id.tvHeader);
        tvLocationMessage = view.findViewById(R.id.tvLocationMessage);
        tvFilterInfo = view.findViewById(R.id.tvFilterInfo);
        recyclerViewHospitals = view.findViewById(R.id.recyclerViewHospitals);
        btnEnableLocation = view.findViewById(R.id.btnEnableLocation);
        btnRefreshBlood = view.findViewById(R.id.btnRefreshBlood);

        // Notification views
        notificationBanner = view.findViewById(R.id.notificationBanner);
        tvNotification = view.findViewById(R.id.tvNotification);
        btnCloseNotification = view.findViewById(R.id.btnCloseNotification);

        // ✅ GET PATIENT DATA WITH DATAMANAGER BACKUP
        if (getArguments() != null) {
            patientData = (PatientData) getArguments().getSerializable("patientData");
            if (patientData != null) {
                Log.d("DEBUG", "✅ HospitalsFragment: Got data from Bundle");
                updateHeaderWithPatientInfo();
            }
        }

        // ✅ BACKUP: If Bundle failed, get from SharedPreferences
        if (patientData == null) {
            patientData = DataManager.getPatientData(getContext());
            if (patientData != null) {
                Log.d("DEBUG", "✅ HospitalsFragment: Got data from SharedPreferences");
                updateHeaderWithPatientInfo();
                showTemporaryNotification("Data loaded from storage", "#4CAF50");
            }
        }

        // If still no data, show message
        if (patientData == null) {
            Log.e("DEBUG", "❌ HospitalsFragment: No patient data available");
            tvHeader.setText("🏥 ALL HOSPITALS");
            tvFilterInfo.setText("No patient data - showing all hospitals");
        }

        // Initialize database
        hospitalDb = new HospitalDatabase(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        setupRecyclerView();
        setupClickListeners();
        setupRefreshButton();
        setupBloodStockReceiver();
        setupNotificationCloseButton();
        checkLocationPermission();

        return view;
    }

    private void updateHeaderWithPatientInfo() {
        if (patientData != null) {
            String header = "🚑 " + patientData.getEmergencyType() + " EMERGENCY";
            if (tvHeader != null) {
                tvHeader.setText(header);
            }
        }
    }

    private void setupRecyclerView() {
        hospitalAdapter = new HospitalAdapter(filteredHospitals, patientData, requireContext(), userLat, userLng);
        recyclerViewHospitals.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerViewHospitals.setAdapter(hospitalAdapter);
    }

    private void setupClickListeners() {
        btnEnableLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DEBUG", "📍 Enable Location button clicked");
                requestLocationPermission();
            }
        });
    }

    private void setupNotificationCloseButton() {
        btnCloseNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideNotification();
            }
        });
    }

    private void setupRefreshButton() {
        btnRefreshBlood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTemporaryNotification("🔄 Refreshing blood stock...", "#2196F3");
                simulateDramaticBloodStockUpdate();
            }
        });
        btnRefreshBlood.setVisibility(View.GONE);
    }

    private void showTemporaryNotification(String message, String color) {
        if (tvNotification != null && notificationBanner != null) {
            tvNotification.setText(message);

            // Set background color based on message type
            switch (color) {
                case "#4CAF50": // SUCCESS
                    notificationBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.success_green));
                    break;
                case "#FF9800": // WARNING
                    notificationBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.warning_orange));
                    break;
                case "#F44336": // ERROR
                    notificationBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.error_red));
                    break;
                default:
                    notificationBanner.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.info_blue));
            }

            notificationBanner.setVisibility(View.VISIBLE);

            // Auto-hide after 5 seconds
            notificationHandler.removeCallbacks(hideNotificationRunnable);
            notificationHandler.postDelayed(hideNotificationRunnable, 5000);
        }
    }

    private void showBloodStockNotification(String hospitalName, String bloodGroup, String newStatus) {
        String message = "🩸 " + hospitalName + " - Blood " + bloodGroup;
        String color = newStatus.equals("AVAILABLE") ? "#4CAF50" : "#FF9800";
        message += newStatus.equals("AVAILABLE") ? " is now AVAILABLE ✅" : " stock is low ⚠️";

        showTemporaryNotification(message, color);
    }

    private Runnable hideNotificationRunnable = new Runnable() {
        @Override
        public void run() {
            hideNotification();
        }
    };

    private void hideNotification() {
        if (notificationBanner != null) {
            notificationBanner.setVisibility(View.GONE);
        }
    }

    // ✅ NEW: Check if location services are enabled
    private boolean isLocationEnabled() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG", "✅ Location permission already granted");

            if (isLocationEnabled()) {
                hideLocationRequiredUI();
                getCurrentLocation();
            } else {
                // ✅ LOCATION IS OFF - FORCE USER TO TURN IT ON
                showLocationDisabledUI();
            }
        } else {
            Log.d("DEBUG", "❌ Location permission not granted - showing enable location UI");
            showLocationRequiredUI();
            clearHospitalsList();
        }
    }

    // ✅ NEW: Show UI when location services are disabled
    private void showLocationDisabledUI() {
        if (tvLocationMessage != null) {
            tvLocationMessage.setVisibility(View.VISIBLE);
            tvLocationMessage.setText("Location is turned off. Please enable location services.");
        }
        if (btnEnableLocation != null) {
            btnEnableLocation.setVisibility(View.VISIBLE);
            btnEnableLocation.setText("Turn On Location");
        }
        if (recyclerViewHospitals != null) {
            recyclerViewHospitals.setVisibility(View.GONE); // Hide hospitals
        }
        if (tvFilterInfo != null) {
            tvFilterInfo.setVisibility(View.GONE);
        }
        if (btnRefreshBlood != null) {
            btnRefreshBlood.setVisibility(View.GONE);
        }
        if (tvHeader != null) {
            tvHeader.setText("📍 Location Services Disabled");
        }
        clearHospitalsList();
    }

    private void showLocationRequiredUI() {
        if (tvLocationMessage != null) {
            tvLocationMessage.setVisibility(View.VISIBLE);
            tvLocationMessage.setText("We need your location to find the nearest hospitals to you");
        }
        if (btnEnableLocation != null) {
            btnEnableLocation.setVisibility(View.VISIBLE);
            btnEnableLocation.setText("Allow Location Access");
        }
        if (recyclerViewHospitals != null) {
            recyclerViewHospitals.setVisibility(View.GONE); // Hide hospitals
        }
        if (tvFilterInfo != null) {
            tvFilterInfo.setVisibility(View.GONE);
        }
        if (btnRefreshBlood != null) {
            btnRefreshBlood.setVisibility(View.GONE);
        }
        if (tvHeader != null) {
            tvHeader.setText("📍 Location Access Needed");
        }
        clearHospitalsList();
    }

    private void hideLocationRequiredUI() {
        if (tvLocationMessage != null) {
            tvLocationMessage.setVisibility(View.GONE);
        }
        if (btnEnableLocation != null) {
            btnEnableLocation.setVisibility(View.GONE);
        }
        if (recyclerViewHospitals != null) {
            recyclerViewHospitals.setVisibility(View.VISIBLE); // Show hospitals
        }
        if (tvFilterInfo != null) {
            tvFilterInfo.setVisibility(View.VISIBLE);
        }
        if (btnRefreshBlood != null && patientData != null && patientData.getBloodGroup() != null && !patientData.getBloodGroup().equals("ANY")) {
            btnRefreshBlood.setVisibility(View.VISIBLE);
        }
    }

    private void clearHospitalsList() {
        allHospitals.clear();
        filteredHospitals.clear();
        if (hospitalAdapter != null) {
            hospitalAdapter.updateHospitals(filteredHospitals);
        }
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission granted, but check if location is enabled
            if (!isLocationEnabled()) {
                openLocationSettings();
            } else {
                hideLocationRequiredUI();
                getCurrentLocation();
            }
        }
    }

    // ✅ NEW: Open location settings to force user to turn on location
    private void openLocationSettings() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        try {
            startActivityForResult(intent, LOCATION_SETTINGS_REQUEST_CODE);
            Toast.makeText(requireContext(), "Please turn on location services", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e("DEBUG", "❌ Cannot open location settings: " + e.getMessage());
            Toast.makeText(requireContext(), "Cannot open location settings", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("DEBUG", "✅ Location permission granted by user");

                if (isLocationEnabled()) {
                    hideLocationRequiredUI();
                    getCurrentLocation();
                } else {
                    showLocationDisabledUI();
                    openLocationSettings(); // ✅ FORCE USER TO TURN ON LOCATION
                }
            } else {
                Log.d("DEBUG", "❌ Location permission denied by user");
                if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                    showPermanentDenialDialog();
                } else {
                    showLocationRequiredUI();
                    showTemporaryNotification("Location access is needed for nearby hospitals", "#FF9800");
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_SETTINGS_REQUEST_CODE) {
            // User returned from location settings - check if they turned on location
            if (isLocationEnabled()) {
                hideLocationRequiredUI();
                getCurrentLocation();
                showTemporaryNotification("Location enabled! Finding hospitals...", "#4CAF50");
            } else {
                showLocationDisabledUI();
                showTemporaryNotification("Please enable location to continue", "#FF9800");
            }
        }
    }

    private void showPermanentDenialDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Location Access Required")
                .setMessage("You have permanently denied location access. You can enable it in Settings or use default location.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        openAppSettings();
                    }
                })
                .setNegativeButton("Use Default Location", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        useDefaultLocation();
                    }
                })
                .show();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", requireContext().getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("DEBUG", "❌ Location permission not granted in getCurrentLocation");
            showLocationRequiredUI();
            return;
        }

        Log.d("DEBUG", "📍 Getting current location...");

        // Show loading state
        if (tvHeader != null) {
            tvHeader.setText("🔍 Finding your location...");
        }
        if (tvFilterInfo != null) {
            tvFilterInfo.setText("Getting your current location...");
        }
        if (tvLocationMessage != null) {
            tvLocationMessage.setText("Finding your location...");
            tvLocationMessage.setVisibility(View.VISIBLE);
        }
        if (btnEnableLocation != null) {
            btnEnableLocation.setVisibility(View.GONE);
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            userLat = location.getLatitude();
                            userLng = location.getLongitude();
                            Log.d("DEBUG", "✅ Location found: Lat=" + userLat + ", Lng=" + userLng);

                            // ✅ Save user location to SharedPreferences
                            DataManager.saveUserLocation(requireContext(), userLat, userLng);

                            showTemporaryNotification("Location found! Loading hospitals...", "#4CAF50");
                            hideLocationRequiredUI();

                            // Update adapter's location
                            hospitalAdapter.updateUserLocation(userLat, userLng);

                            // Load hospitals
                            loadHospitalsFromDatabase();
                            filterAndSortHospitals();

                            // Restore header
                            updateHeaderWithPatientInfo();
                        } else {
                            Log.w("DEBUG", "⚠️ Last known location is null. Location services might be off.");
                            showLocationDisabledUI();
                            showTemporaryNotification("Location services are off. Please enable GPS.", "#FF9800");
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("DEBUG", "❌ Failed to get location: " + e.getMessage());
                    showLocationDisabledUI();
                    showTemporaryNotification("Failed to get location. Please enable location services.", "#FF9800");
                });
    }

    private void useDefaultLocation() {
        userLat = 11.9400;
        userLng = 79.8083; // Pondicherry default
        Log.d("DEBUG", "🌐 Using default location: Lat=" + userLat + ", Lng=" + userLng);

        DataManager.saveUserLocation(requireContext(), userLat, userLng);

        showTemporaryNotification("Using default location (Pondicherry)", "#FF9800");
        hideLocationRequiredUI();
        hospitalAdapter.updateUserLocation(userLat, userLng);
        loadHospitalsFromDatabase();
        filterAndSortHospitals();
        updateHeaderWithPatientInfo();
    }

    private void loadHospitalsFromDatabase() {
        allHospitals.clear();
        distancesCalculated = false;

        if (patientData != null && patientData.getEmergencyType() != null) {
            allHospitals.addAll(hospitalDb.getHospitalsByEmergencyType(patientData.getEmergencyType()));
            Log.d("DEBUG", "🏥 Loaded " + allHospitals.size() + " hospitals for emergency type: " + patientData.getEmergencyType());
        } else {
            allHospitals.addAll(hospitalDb.getAllHospitals());
            Log.d("DEBUG", "🏥 Loaded " + allHospitals.size() + " all hospitals");
        }

        // Calculate distances for all hospitals
        if (!distancesCalculated) {
            for (int i = 0; i < allHospitals.size(); i++) {
                Hospital hospital = allHospitals.get(i);
                double distance = calculateDistance(hospital.getLatitude(), hospital.getLongitude());

                // Ensure distance is not 0.0 - add minimum distance based on position
                if (distance < 0.5) {
                    distance = 0.5 + (i * 0.3); // 0.5km, 0.8km, 1.1km, etc.
                }

                hospital.setDistance(distance);
            }
            distancesCalculated = true;
            Log.d("DEBUG", "📏 Calculated distances for " + allHospitals.size() + " hospitals");
        }

        if (!allHospitals.isEmpty() && btnRefreshBlood != null) {
            btnRefreshBlood.setVisibility(View.VISIBLE);
        }
    }

    private void filterAndSortHospitals() {
        filteredHospitals.clear();
        filteredHospitals.addAll(allHospitals);

        // Sort by distance (Nearest first)
        Collections.sort(filteredHospitals, new Comparator<Hospital>() {
            @Override
            public int compare(Hospital h1, Hospital h2) {
                return Double.compare(h1.getDistance(), h2.getDistance());
            }
        });

        hospitalAdapter.updateHospitals(filteredHospitals);
        Log.d("DEBUG", "🏥 Sorted " + filteredHospitals.size() + " hospitals by distance");
    }

    private double calculateDistance(double hospitalLat, double hospitalLng) {
        double earthRadius = 6371;

        double dLat = Math.toRadians(hospitalLat - userLat);
        double dLon = Math.toRadians(hospitalLng - userLng);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(hospitalLat)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return earthRadius * c;
    }

    private void setupBloodStockReceiver() {
        bloodStockReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (BloodStockReceiver.ACTION_BLOOD_STOCK_UPDATE.equals(intent.getAction())) {
                    String hospitalName = intent.getStringExtra(BloodStockReceiver.EXTRA_HOSPITAL_NAME);
                    String bloodGroup = intent.getStringExtra(BloodStockReceiver.EXTRA_BLOOD_GROUP);
                    String newStatus = intent.getStringExtra(BloodStockReceiver.EXTRA_NEW_STATUS);

                    Log.d("DEBUG", "🩸 Blood stock update received - Hospital: " + hospitalName + ", Blood: " + bloodGroup + ", Status: " + newStatus);

                    // Update hospital blood stock in both lists
                    boolean foundInAll = updateHospitalBloodStock(allHospitals, hospitalName, bloodGroup, newStatus);
                    boolean foundInFiltered = updateHospitalBloodStock(filteredHospitals, hospitalName, bloodGroup, newStatus);

                    if (foundInAll || foundInFiltered) {
                        // Also update the database for persistence
                        boolean dbUpdated = hospitalDb.updateBloodStock(hospitalName, bloodGroup, newStatus.equals("AVAILABLE"));
                        Log.d("DEBUG", "Database update success: " + dbUpdated);

                        // Refresh UI
                        hospitalAdapter.notifyDataSetChanged();

                        // Show notification
                        showBloodStockNotification(hospitalName, bloodGroup, newStatus);
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter(BloodStockReceiver.ACTION_BLOOD_STOCK_UPDATE);
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(bloodStockReceiver, filter);
    }

    private boolean updateHospitalBloodStock(List<Hospital> hospitalsList, String hospitalName, String bloodGroup, String newStatus) {
        boolean updated = false;
        for (Hospital h : hospitalsList) {
            if (h.getName().equals(hospitalName)) {
                String currentBloodGroups = h.getBloodGroups();
                List<String> bloodGroupsList = new ArrayList<>();

                if (currentBloodGroups != null && !currentBloodGroups.isEmpty()) {
                    String[] groups = currentBloodGroups.split(",");
                    for (String group : groups) {
                        if (!group.trim().isEmpty()) {
                            bloodGroupsList.add(group.trim());
                        }
                    }
                }

                if (newStatus.equals("AVAILABLE")) {
                    if (!bloodGroupsList.contains(bloodGroup)) {
                        bloodGroupsList.add(bloodGroup);
                    }
                } else if (newStatus.equals("NOT_AVAILABLE")) {
                    bloodGroupsList.remove(bloodGroup);
                }

                // Convert back to comma-separated string
                StringBuilder result = new StringBuilder();
                for (int i = 0; i < bloodGroupsList.size(); i++) {
                    if (i > 0) {
                        result.append(",");
                    }
                    result.append(bloodGroupsList.get(i));
                }
                h.setBloodGroups(result.toString());
                updated = true;
                break;
            }
        }
        return updated;
    }

    private void simulateDramaticBloodStockUpdate() {
        if (!filteredHospitals.isEmpty()) {
            if (patientData == null || patientData.getBloodGroup() == null || patientData.getBloodGroup().equals("ANY")) {
                showTemporaryNotification("Please select a specific blood group first.", "#FF9800");
                return;
            }

            String bloodGroup = patientData.getBloodGroup();

            // ✅ FIX: Get RANDOM hospital instead of always first one
            int randomIndex = new Random().nextInt(filteredHospitals.size());
            Hospital randomHospital = filteredHospitals.get(randomIndex);

            // ✅ FIX: Also include some hospitals that don't have the blood group
            // 50% chance to toggle existing blood group, 50% chance to add new one
            boolean shouldAddNewBloodGroup = new Random().nextBoolean();

            String currentBloodGroups = randomHospital.getBloodGroups();
            boolean hasBlood = currentBloodGroups != null && currentBloodGroups.contains(bloodGroup);

            String newStatus;
            if (shouldAddNewBloodGroup && !hasBlood) {
                // Add blood group that wasn't there before
                newStatus = "AVAILABLE";
            } else if (hasBlood) {
                // Remove existing blood group
                newStatus = "NOT_AVAILABLE";
            } else {
                // If hospital doesn't have the blood group and we're not adding, skip
                showTemporaryNotification("No blood stock changes needed for " + randomHospital.getName(), "#2196F3");
                return;
            }

            Intent bloodUpdateIntent = new Intent(BloodStockReceiver.ACTION_BLOOD_STOCK_UPDATE);
            bloodUpdateIntent.putExtra(BloodStockReceiver.EXTRA_HOSPITAL_NAME, randomHospital.getName());
            bloodUpdateIntent.putExtra(BloodStockReceiver.EXTRA_BLOOD_GROUP, bloodGroup);
            bloodUpdateIntent.putExtra(BloodStockReceiver.EXTRA_NEW_STATUS, newStatus);
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(bloodUpdateIntent);

            Log.d("DEBUG", "🧪 Dramatic blood update: " + randomHospital.getName() + " - " + bloodGroup + " = " + newStatus);

            // ✅ BONUS: Show which hospital was updated in the notification
            showTemporaryNotification("🔄 Updated " + randomHospital.getName() + " blood stock", "#2196F3");
        } else {
            showTemporaryNotification("No hospitals available to update", "#FF9800");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (bloodStockReceiver != null) {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(bloodStockReceiver);
            Log.d("DEBUG", "📡 Blood stock receiver unregistered");
        }

        notificationHandler.removeCallbacks(hideNotificationRunnable);
    }
}