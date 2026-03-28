package com.example.project2;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.example.project2.model.PatientData;
import com.example.project2.model.Hospital;
import com.example.project2.Driver;
import com.example.project2.fragments.HomeFragment;
import com.example.project2.fragments.PreferencesFragment;
import com.example.project2.fragments.HospitalsFragment;
import com.example.project2.fragments.EmergencyFragment;
import com.example.project2.fragments.HistoryFragment;
import com.example.project2.DatabaseHelper;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseHelper dbHelper;
    private TextView tvWelcome;
    private BottomNavigationView bottomNav;

    // ✅ ONLY fragments that don't need data
    private HomeFragment homeFragment;
    private HistoryFragment historyFragment;

    // ❌ REMOVED: preferencesFragment, hospitalsFragment, emergencyFragment

    // Permission request codes
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(this);

        requestNotificationPermission();
        initializeViews();
        setupFragments();
        loadDriverData();
        setupBottomNavigation();
    }

    private void initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome);
        bottomNav = findViewById(R.id.bottom_navigation);

        TextView tvLive = findViewById(R.id.tvLive);
        if (tvLive != null) {
            tvLive.setBackgroundColor(Color.RED);
            tvLive.setTextColor(Color.WHITE);
            tvLive.setPadding(16, 8, 16, 8);
        }
    }

    private void setupFragments() {
        // ✅ ONLY create fragments that don't need initial data
        homeFragment = new HomeFragment();
        historyFragment = new HistoryFragment();

        // ❌ REMOVED: preferencesFragment, hospitalsFragment, emergencyFragment

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, homeFragment)
                .commit();
    }

    private void loadDriverData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Driver driver = dbHelper.getDriverByUid(currentUser.getUid());
            if (driver != null) {
                tvWelcome.setText("Welcome, " + driver.getUsername() + " 🚑");
            } else {
                tvWelcome.setText("Welcome, Driver 🚑");
            }
        } else {
            tvWelcome.setText("Welcome, Driver 🚑");
        }
    }

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                selectedFragment = homeFragment;
            } else if (itemId == R.id.nav_preferences) {
                // ✅ Create new instance for manual navigation
                selectedFragment = new PreferencesFragment();
            } else if (itemId == R.id.nav_hospitals) {
                // ✅ Create new instance for manual navigation
                selectedFragment = new HospitalsFragment();
            } else if (itemId == R.id.nav_emergency) {
                // ✅ Create new instance for manual navigation
                selectedFragment = new EmergencyFragment();
            } else if (itemId == R.id.nav_history) {
                selectedFragment = historyFragment;
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case NOTIFICATION_PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    android.widget.Toast.makeText(this, "Notification permission granted", android.widget.Toast.LENGTH_SHORT).show();
                } else {
                    android.widget.Toast.makeText(this, "Notifications will not work without permission", android.widget.Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    // ✅ PATIENT DATA TO PREFERENCES - FIXED
    // ✅ PATIENT DATA TO PREFERENCES - FIXED WITH DEBUG
    public void switchToPreferencesFragment(@NonNull PatientData patientData) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Log.d("DEBUG", "🎯 MainActivity: switchToPreferencesFragment called");
        Log.d("DEBUG", "🎯 MainActivity: PatientData - " +
                patientData.getEmergencyType() + ", " + patientData.getAgeGroup());

        PreferencesFragment preferencesFragment = new PreferencesFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientData", patientData);
        preferencesFragment.setArguments(bundle);

        // DEBUG: Check if bundle has data
        PatientData debugData = (PatientData) bundle.getSerializable("patientData");
        Log.d("DEBUG", "🎯 MainActivity: Bundle has data - " + (debugData != null));
        if (debugData != null) {
            Log.d("DEBUG", "🎯 MainActivity: Bundle data - " +
                    debugData.getEmergencyType() + ", " + debugData.getAgeGroup());
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, preferencesFragment)
                .addToBackStack("preferences")
                .commit();

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_preferences);
        }

        Toast.makeText(this, "✅ Opening preferences...", Toast.LENGTH_SHORT).show();
    }

    // ✅ PATIENT DATA TO HOSPITALS - FIXED
    public void switchToHospitalsFragment(@NonNull PatientData patientData) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        Log.d("MAIN_ACTIVITY", "Switching to HospitalsFragment with patient data");

        HospitalsFragment hospitalsFragment = new HospitalsFragment(); // ✅ Local instance
        Bundle bundle = new Bundle();
        bundle.putSerializable("patientData", patientData);
        hospitalsFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, hospitalsFragment)
                .addToBackStack("hospitals")
                .commit();

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_hospitals);
        }
    }

    // ✅ HOSPITAL & PATIENT DATA TO EMERGENCY
    public void switchToEmergencyFragment(@NonNull Hospital hospital, @NonNull PatientData patientData) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        EmergencyFragment emergencyFragment = new EmergencyFragment(); // ✅ Local instance
        Bundle bundle = new Bundle();
        bundle.putSerializable("hospital", hospital);
        bundle.putSerializable("patientData", patientData);
        emergencyFragment.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, emergencyFragment)
                .addToBackStack(null)
                .commit();

        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_emergency);
        }
    }

    public void setSelectedNavItem(int itemId) {
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(itemId);
        }
    }

    public void refreshCurrentFragment() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof HomeFragment) {
        } else if (currentFragment instanceof HospitalsFragment) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDriverData();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}