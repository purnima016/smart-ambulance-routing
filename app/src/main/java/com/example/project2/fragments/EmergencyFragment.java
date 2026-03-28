package com.example.project2.fragments;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.project2.model.Hospital;
import com.example.project2.model.PatientData;
import com.example.project2.R;
import com.example.project2.providers.EmergencyHistoryContract;
import com.example.project2.activities.MapsActivity;
import com.example.project2.DataManager;
import com.example.project2.DatabaseHelper;
import com.example.project2.model.EmergencyHistory;
import com.example.project2.service.GeofencingService;
import android.net.Uri;
import android.util.Log;

public class EmergencyFragment extends Fragment {

    private Hospital selectedHospital;
    private PatientData patientData;
    private TextView tvEmergencyInfo, tvHospitalDetails, tvPatientDetails, tvActionStatus;
    private Button btnCompleteTrip, btnStartNavigation;
    private long emergencyStartTime;
    private boolean navigationStarted = false;

    // Voice Player Variables
    private MediaPlayer mediaPlayer;
    private ImageButton btnPlayPause;
    private SeekBar seekBarAudio;
    private TextView tvTimer, tvPlaybackStatus;
    private boolean isPlaying = false;
    private Handler updateSeekBarHandler;
    private Runnable updateSeekBarRunnable;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency, container, false);

        // Initialize views
        tvEmergencyInfo = view.findViewById(R.id.tvEmergencyInfo);
        tvHospitalDetails = view.findViewById(R.id.tvHospitalDetails);
        tvPatientDetails = view.findViewById(R.id.tvPatientDetails);
        tvActionStatus = view.findViewById(R.id.tvActionStatus);
        btnCompleteTrip = view.findViewById(R.id.btnCompleteTrip);
        btnStartNavigation = view.findViewById(R.id.btnStartNavigation);

        // Initialize Voice Player Views
        btnPlayPause = view.findViewById(R.id.btnPlayPause);
        seekBarAudio = view.findViewById(R.id.seekBarAudio);
        tvTimer = view.findViewById(R.id.tvTimer);
        tvPlaybackStatus = view.findViewById(R.id.tvPlaybackStatus);

        Log.d("EmergencyFragment", "🔄 onCreateView called");

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get data from arguments FIRST
        getDataFromArguments();

        // If no data from arguments, try to get from SharedPreferences
        if (selectedHospital == null || patientData == null) {
            getDataFromSharedPreferences();
        }

        // Setup UI
        displayEmergencyDetails();
        setupButtons();
        setupVoicePlayer();

        // ✅ START GEOFENCING - Hospital will be notified automatically at 500m
        startGeofencing();
    }

    private void getDataFromArguments() {
        Bundle args = getArguments();
        if (args != null) {
            selectedHospital = (Hospital) args.getSerializable("hospital");
            patientData = (PatientData) args.getSerializable("patientData");

            if (selectedHospital != null && patientData != null) {
                Log.d("EmergencyFragment", "✅ Data from arguments:");
                Log.d("EmergencyFragment", "🏥 Hospital: " + selectedHospital.getName());
                Log.d("EmergencyFragment", "📞 Hospital Phone: " + selectedHospital.getPhone());
                Log.d("EmergencyFragment", "👤 Patient: " + patientData.getEmergencyType());

                // Save to SharedPreferences as backup
                DataManager.savePatientData(requireContext(), patientData);
                DataManager.saveSelectedHospital(requireContext(), selectedHospital);
                emergencyStartTime = System.currentTimeMillis();
            } else {
                Log.e("EmergencyFragment", "❌ Incomplete data from arguments");
            }
        } else {
            Log.e("EmergencyFragment", "❌ No arguments received");
        }
    }

    private void getDataFromSharedPreferences() {
        patientData = DataManager.getPatientData(requireContext());
        selectedHospital = DataManager.getSelectedHospital(requireContext());

        if (patientData != null) {
            Log.d("EmergencyFragment", "✅ Patient data from SharedPreferences:");
            Log.d("EmergencyFragment", "👤 Patient: " + patientData.getEmergencyType());
        } else {
            Log.e("EmergencyFragment", "❌ No patient data in SharedPreferences");
        }

        if (selectedHospital != null) {
            Log.d("EmergencyFragment", "✅ Hospital data from SharedPreferences:");
            Log.d("EmergencyFragment", "🏥 Hospital: " + selectedHospital.getName());
            Log.d("EmergencyFragment", "📞 Hospital Phone: " + selectedHospital.getPhone());
        } else {
            Log.e("EmergencyFragment", "❌ No hospital data in SharedPreferences");
        }

        emergencyStartTime = System.currentTimeMillis();
    }

    private void setupButtons() {
        btnCompleteTrip.setOnClickListener(v -> completeTrip());
        btnStartNavigation.setOnClickListener(v -> startNavigation());
    }

    // ✅ GEOFENCING - Hospital will be notified automatically at 500m
    private void startGeofencing() {
        try {
            GeofencingService geofencingService = new GeofencingService(requireContext());

            if (geofencingService.canStartGeofencing()) {
                geofencingService.startGeofencingForHospital(selectedHospital);
                Log.d("EmergencyFragment", "📍 Geofencing started for: " + selectedHospital.getName());
                Log.d("EmergencyFragment", "📞 Hospital will receive SMS at 500m: " + selectedHospital.getPhone());

                // Update status to show geofencing is active
                tvActionStatus.setText("✅ Ready to navigate!\n📍 Geofencing active - Hospital will be alerted at 500m");
            } else {
                String status = geofencingService.getGeofencingStatus();
                Log.w("EmergencyFragment", "⚠️ Geofencing not available: " + status);
                tvActionStatus.setText("✅ Ready to navigate!\n⚠️ Geofencing unavailable: " + status);
            }
        } catch (Exception e) {
            Log.e("EmergencyFragment", "❌ Error starting geofencing: " + e.getMessage());
        }
    }

    private void setupVoicePlayer() {
        try {
            // Initialize MediaPlayer
            mediaPlayer = MediaPlayer.create(requireContext(), R.raw.trip_protocol_complete);

            mediaPlayer.setOnCompletionListener(mp -> {
                isPlaying = false;
                btnPlayPause.setImageResource(R.drawable.ic_play);
                tvPlaybackStatus.setText("Voice guide completed");
                stopSeekBarUpdate();
                resetSeekBar();
            });

            mediaPlayer.setOnPreparedListener(mp -> {
                seekBarAudio.setMax(mediaPlayer.getDuration());
                updateTimerText();
            });

            // Setup SeekBar
            seekBarAudio.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && mediaPlayer != null) {
                        mediaPlayer.seekTo(progress);
                        updateTimerText();
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    // Pause updates while user is dragging
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    // Resume updates if playing
                    if (isPlaying) {
                        startSeekBarUpdate();
                    }
                }
            });

            // Setup Play/Pause button
            btnPlayPause.setOnClickListener(v -> togglePlayPause());

            // Initialize handler
            updateSeekBarHandler = new Handler(Looper.getMainLooper());

        } catch (Exception e) {
            Log.e("EmergencyFragment", "❌ Error setting up voice player: " + e.getMessage());
            tvPlaybackStatus.setText("Voice guide unavailable");
            btnPlayPause.setEnabled(false);
        }
    }

    private void togglePlayPause() {
        if (isPlaying) {
            pauseAudio();
        } else {
            playAudio();
        }
    }

    private void playAudio() {
        try {
            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
                isPlaying = true;
                btnPlayPause.setImageResource(R.drawable.ic_pause);
                tvPlaybackStatus.setText("Playing trip protocols...");

                startSeekBarUpdate();
            }
        } catch (Exception e) {
            Log.e("EmergencyFragment", "❌ Audio play error: " + e.getMessage());
            tvPlaybackStatus.setText("Error playing voice guide");
        }
    }

    private void pauseAudio() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            btnPlayPause.setImageResource(R.drawable.ic_play);
            tvPlaybackStatus.setText("Paused");

            stopSeekBarUpdate();
        }
    }

    private void startSeekBarUpdate() {
        updateSeekBarRunnable = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    seekBarAudio.setProgress(currentPosition);
                    updateTimerText();
                    updateSeekBarHandler.postDelayed(this, 1000);
                }
            }
        };
        updateSeekBarHandler.post(updateSeekBarRunnable);
    }

    private void stopSeekBarUpdate() {
        if (updateSeekBarHandler != null && updateSeekBarRunnable != null) {
            updateSeekBarHandler.removeCallbacks(updateSeekBarRunnable);
        }
    }

    private void resetSeekBar() {
        if (mediaPlayer != null) {
            seekBarAudio.setProgress(0);
            updateTimerText();
        }
    }

    private void updateTimerText() {
        if (mediaPlayer != null) {
            int currentPosition = mediaPlayer.getCurrentPosition();
            int minutes = currentPosition / 1000 / 60;
            int seconds = currentPosition / 1000 % 60;
            tvTimer.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private void displayEmergencyDetails() {
        if (selectedHospital != null && patientData != null) {
            // Emergency Info
            tvEmergencyInfo.setText("🚨 EMERGENCY ALERT ACTIVATED\n" +
                    "Ambulance dispatched to: " + selectedHospital.getName());

            // Hospital Details
            String hospitalInfo = "🏥 " + selectedHospital.getName() + "\n\n" +
                    "📍 " + (selectedHospital.getAddress() != null ? selectedHospital.getAddress() : "Address not available") + "\n\n" +
                    "📞 " + (selectedHospital.getPhone() != null ? selectedHospital.getPhone() : "Phone not available") + "\n\n" +
                    "⏱ Est. Response: " + selectedHospital.getResponseTime() + " mins\n\n" +
                    "🛏 Available Beds: " + selectedHospital.getAvailableBeds() + "\n\n" +
                    "🎯 Specialties: " + selectedHospital.getSpecialties() + "\n\n" +
                    "🩸 Blood Available: " + selectedHospital.getBloodGroups();

            tvHospitalDetails.setText(hospitalInfo);

            // Patient Details
            String patientInfo = "👤 PATIENT DETAILS:\n\n" +
                    "🆘 Emergency: " + patientData.getEmergencyType() + "\n\n" +
                    "👥 Age Group: " + patientData.getAgeGroup() + "\n\n" +
                    "🩸 Blood Group: " + patientData.getBloodGroup() + "\n\n" +
                    "📊 Condition: " + patientData.getCondition();

            tvPatientDetails.setText(patientInfo);

            // Initial state - show only Start Navigation
            showInitialState();
        } else {
            showErrorState();
        }
    }

    private void showErrorState() {
        tvEmergencyInfo.setText("❌ EMERGENCY DATA UNAVAILABLE");
        tvHospitalDetails.setText("Please go back and select a hospital again");
        tvPatientDetails.setText("Patient data not available");
        tvActionStatus.setText("Unable to proceed. Please restart the emergency process.");

        btnStartNavigation.setVisibility(View.GONE);
        btnCompleteTrip.setVisibility(View.GONE);
        btnPlayPause.setEnabled(false);

        Toast.makeText(requireContext(), "Emergency data missing. Please try again.", Toast.LENGTH_LONG).show();
    }

    private void showInitialState() {
        btnStartNavigation.setVisibility(View.VISIBLE);
        btnCompleteTrip.setVisibility(View.GONE);
        tvActionStatus.setText("✅ Ready to navigate!\nClick 'Start Navigation' to view route");
    }

    private void showCompletionReadyState() {
        btnStartNavigation.setVisibility(View.GONE);
        btnCompleteTrip.setVisibility(View.VISIBLE);
        tvActionStatus.setText("✅ Navigation Completed!\n\n" +
                "• Route viewed successfully\n" +
                "• Hospital alert system active\n" +
                "• Ready to complete trip\n" +
                "• Click 'Complete Trip' to finish");

        Toast.makeText(requireContext(), "Ready to complete your trip!", Toast.LENGTH_SHORT).show();
    }

    private void startNavigation() {
        if (selectedHospital != null) {
            try {
                navigationStarted = true;

                if (isPlaying) {
                    pauseAudio();
                }

                Intent mapsIntent = new Intent(requireContext(), MapsActivity.class);
                mapsIntent.putExtra("hospital", selectedHospital);
                startActivity(mapsIntent);

                tvActionStatus.setText("🗺 Navigation Started!\n\n" +
                        "• Google Maps opened with route\n" +
                        "• Hospital alert system active\n" +
                        "• Press BACK button to return here\n" +
                        "• Complete trip after viewing route");

                Log.d("EmergencyFragment", "📍 Navigation started to: " + selectedHospital.getName());
                Toast.makeText(requireContext(),
                        "Navigation started - View route in Google Maps",
                        Toast.LENGTH_SHORT).show();

            } catch (Exception e) {
                Log.e("EmergencyFragment", "❌ Error opening maps: " + e.getMessage());
                Toast.makeText(requireContext(), "Error opening maps: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(requireContext(), "No hospital selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (navigationStarted) {
            showCompletionReadyState();
            navigationStarted = false;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isPlaying) {
            pauseAudio();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            stopSeekBarUpdate();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void completeTrip() {
        if (selectedHospital != null && patientData != null) {
            boolean saved = saveTripToHistory();

            if (saved) {
                tvActionStatus.setText("✅ TRIP COMPLETED SUCCESSFULLY!\n\n" +
                        "• Patient admitted to " + selectedHospital.getName() + "\n" +
                        "• Medical team received patient\n" +
                        "• Treatment initiated successfully\n" +
                        "• Trip saved to history\n" +
                        "• Navigating to History tab...");

                btnCompleteTrip.setVisibility(View.GONE);
                btnStartNavigation.setVisibility(View.GONE);

                Log.d("EmergencyFragment", "✅ Trip completed for: " + selectedHospital.getName());

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    navigateToHistoryFragment();
                }, 3000);
            } else {
                Toast.makeText(requireContext(), "Failed to save trip history. Please try again.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private boolean saveTripToHistory() {
        try {
            long tripEndTime = System.currentTimeMillis();

            Log.d("EmergencyFragment", "💾 Saving trip to history:");
            Log.d("EmergencyFragment", "🏥 Hospital: " + selectedHospital.getName());
            Log.d("EmergencyFragment", "🆘 Emergency: " + patientData.getEmergencyType());

            DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

            EmergencyHistory history = new EmergencyHistory(
                    tripEndTime,
                    selectedHospital.getName(),
                    patientData.getEmergencyType(),
                    patientData.getCondition(),
                    "Completed",
                    emergencyStartTime,
                    tripEndTime,
                    patientData.getBloodGroup(),
                    selectedHospital.getResponseTime()
            );

            boolean dbSuccess = dbHelper.addEmergencyHistory(history);
            dbHelper.close();

            if (dbSuccess) {
                Log.d("EmergencyFragment", "✅ Trip saved to database successfully!");
                Toast.makeText(requireContext(), "✅ Trip completed and saved to history!", Toast.LENGTH_LONG).show();

                saveViaContentProvider(tripEndTime);
                return true;
            } else {
                Log.e("EmergencyFragment", "❌ Failed to save trip to database, trying Content Provider...");

                boolean cpSuccess = saveViaContentProvider(tripEndTime);
                if (cpSuccess) {
                    Toast.makeText(requireContext(), "✅ Trip completed and saved to history!", Toast.LENGTH_LONG).show();
                    return true;
                } else {
                    Toast.makeText(requireContext(), "❌ Failed to save trip history", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

        } catch (Exception e) {
            Log.e("EmergencyFragment", "❌ Error saving trip history: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error saving trip history: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean saveViaContentProvider(long tripEndTime) {
        try {
            ContentValues values = new ContentValues();
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_TIMESTAMP, tripEndTime);
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_HOSPITAL_NAME, selectedHospital.getName());
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_EMERGENCY_TYPE, patientData.getEmergencyType());
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_PATIENT_CONDITION, patientData.getCondition());
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_STATUS, "Completed");
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_START_TIME, emergencyStartTime);
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_END_TIME, tripEndTime);
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_BLOOD_GROUP, patientData.getBloodGroup());
            values.put(EmergencyHistoryContract.HistoryEntry.COLUMN_RESPONSE_TIME, selectedHospital.getResponseTime());

            Uri newUri = requireContext().getContentResolver().insert(
                    EmergencyHistoryContract.HistoryEntry.CONTENT_URI,
                    values
            );

            if (newUri != null) {
                Log.d("EmergencyFragment", "✅ Trip saved via Content Provider: " + newUri);
                return true;
            } else {
                Log.e("EmergencyFragment", "❌ Content Provider insert returned null");
                return false;
            }
        } catch (Exception e) {
            Log.e("EmergencyFragment", "❌ Content Provider error: " + e.getMessage());
            return false;
        }
    }

    private void navigateToHistoryFragment() {
        if (getActivity() != null) {
            com.google.android.material.bottomnavigation.BottomNavigationView bottomNav =
                    getActivity().findViewById(R.id.bottom_navigation);
            if (bottomNav != null) {
                bottomNav.setSelectedItemId(R.id.nav_history);
                Log.d("EmergencyFragment", "➡ Navigating to History tab");
            }
        }
    }
}