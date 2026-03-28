package com.example.project2.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.example.project2.R;
import com.example.project2.model.Hospital;
import com.example.project2.service.GeofencingService;
import android.util.Log;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private Hospital hospital;
    private double userLat, userLng;
    private TextView tvHospitalName, tvDistance, tvAddress;
    private Button btnStartNavigation, btnBack;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize views
        tvHospitalName = findViewById(R.id.tvHospitalName);
        tvDistance = findViewById(R.id.tvDistance);
        tvAddress = findViewById(R.id.tvAddress);
        btnStartNavigation = findViewById(R.id.btnStartNavigation);
        btnBack = findViewById(R.id.btnBack);

        // Get hospital data from intent
        if (getIntent() != null) {
            hospital = (Hospital) getIntent().getSerializableExtra("hospital");
            userLat = getIntent().getDoubleExtra("userLat", 11.9400);
            userLng = getIntent().getDoubleExtra("userLng", 79.8083);
        }

        if (hospital != null) {
            tvHospitalName.setText(hospital.getName());
            tvDistance.setText(String.format("%.1f km", hospital.getDistance()));
            tvAddress.setText(hospital.getAddress());

            // Check permissions and request if needed
            checkAndRequestPermissions();
        } else {
            Toast.makeText(this, "Hospital data not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeMap();

        btnStartNavigation.setOnClickListener(v -> startNavigation());
        btnBack.setOnClickListener(v -> finish());
    }

    private void checkAndRequestPermissions() {
        if (hasAllRequiredPermissions()) {
            startGeofencing();
        } else {
            requestLocationPermissions();
        }
    }

    private boolean hasAllRequiredPermissions() {
        boolean hasFineLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;

        boolean hasBackgroundLocation = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasBackgroundLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    == PackageManager.PERMISSION_GRANTED;
        }

        Log.d("MapsActivity", "📍 Permission Status - Fine: " + hasFineLocation + ", Background: " + hasBackgroundLocation);
        return hasFineLocation && hasBackgroundLocation;
    }

    private void requestLocationPermissions() {
        String[] permissions;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            };
        }

        ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
    }

    private void startGeofencing() {
        try {
            GeofencingService geofencingService = new GeofencingService(this);

            if (geofencingService.canStartGeofencing()) {
                geofencingService.startGeofencingForHospital(hospital);
                Log.d("MapsActivity", "📍 Geofencing started for: " + hospital.getName());
            } else {
                String status = geofencingService.getGeofencingStatus();
                Log.w("MapsActivity", "⚠️ Cannot start geofencing: " + status);
                Toast.makeText(this, status, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e("MapsActivity", "❌ Error starting geofencing: " + e.getMessage());
            Toast.makeText(this, "Failed to start geofencing", Toast.LENGTH_SHORT).show();
        }
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "Map fragment not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // FIXED: Added proper permission check before calling location methods
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } catch (SecurityException e) {
                Log.e("MapsActivity", "SecurityException: " + e.getMessage());
                Toast.makeText(this, "Location permission required for map features", Toast.LENGTH_SHORT).show();
            }
        }

        if (hospital != null) {
            LatLng hospitalLocation = new LatLng(hospital.getLatitude(), hospital.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(hospitalLocation)
                    .title(hospital.getName())
                    .snippet("Destination"));

            LatLng userLocation = new LatLng(userLat, userLng);
            mMap.addMarker(new MarkerOptions()
                    .position(userLocation)
                    .title("Your Location")
                    .snippet("Starting point"));

            mMap.addPolyline(new PolylineOptions()
                    .add(userLocation, hospitalLocation)
                    .width(10)
                    .color(ContextCompat.getColor(this, R.color.colorPrimary)));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hospitalLocation, 12));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
    }

    private void startNavigation() {
        if (hospital != null) {
            openGoogleMapsNavigation();
        } else {
            Toast.makeText(this, "Hospital information not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGoogleMapsNavigation() {
        try {
            String uri = "google.navigation:q=" + hospital.getLatitude() + "," + hospital.getLongitude() + "&mode=d";
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.setPackage("com.google.android.apps.maps");

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
                Toast.makeText(this, "Opening Google Maps...", Toast.LENGTH_SHORT).show();
            } else {
                openWebMapsFallback();
            }
        } catch (Exception e) {
            openWebMapsFallback();
        }
    }

    private void openWebMapsFallback() {
        try {
            String webUri = "https://www.google.com/maps/dir/?api=1&destination=" +
                    hospital.getLatitude() + "," + hospital.getLongitude() + "&travelmode=driving";
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUri));
            startActivity(webIntent);
        } catch (Exception e) {
            Toast.makeText(this, "No navigation app available", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            if (allGranted) {
                startGeofencing();
                // FIXED: Added proper permission check before calling map location methods
                if (mMap != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e("MapsActivity", "SecurityException in permission result: " + e.getMessage());
                    }
                }
                Toast.makeText(this, "Permissions granted - geofencing activated", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Location permissions denied - geofencing unavailable", Toast.LENGTH_LONG).show();
                Log.w("MapsActivity", "⚠️ User denied location permissions");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (hospital != null) {
            try {
                GeofencingService geofencingService = new GeofencingService(this);
                geofencingService.stopGeofencing();
                Log.d("MapsActivity", "📍 Geofencing stopped for: " + hospital.getName());
            } catch (Exception e) {
                Log.e("MapsActivity", "Error stopping geofencing: " + e.getMessage());
            }
        }
    }
}