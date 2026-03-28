package com.example.project2.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.example.project2.R;
import com.example.project2.model.EmergencyHistory;
import com.example.project2.providers.EmergencyHistoryContract;
import com.example.project2.DatabaseHelper;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import com.example.project2.AuthActivity;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import android.util.Log;

public class HistoryFragment extends Fragment {

    private LinearLayout historyContainer;
    private TextView tvEmptyHistory;
    private Button btnLogout;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);

        historyContainer = view.findViewById(R.id.historyContainer);
        tvEmptyHistory = view.findViewById(R.id.tvEmptyHistory);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Initialize database helper
        dbHelper = new DatabaseHelper(requireContext());

        // Setup logout
        btnLogout.setOnClickListener(v -> logoutUser());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Load history when fragment becomes visible
        loadEmergencyHistory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    public void loadEmergencyHistory() {
        try {
            Log.d("HistoryFragment", "🔄 Loading emergency history...");

            // Try DatabaseHelper method first (most reliable)
            List<EmergencyHistory> historyList = dbHelper.getAllEmergencyHistory();

            if (historyList != null && !historyList.isEmpty()) {
                displayHistoryFromList(historyList);
            } else {
                // Fallback to Content Provider method
                loadHistoryViaContentProvider();
            }

        } catch (Exception e) {
            Log.e("HistoryFragment", "❌ Error loading history: " + e.getMessage());
            e.printStackTrace();
            showErrorState("Error loading history: " + e.getMessage());
        }
    }

    private void displayHistoryFromList(List<EmergencyHistory> historyList) {
        tvEmptyHistory.setVisibility(View.GONE);
        historyContainer.setVisibility(View.VISIBLE);

        // Clear existing views (except first child which is the title)
        while (historyContainer.getChildCount() > 1) {
            historyContainer.removeViewAt(1);
        }

        // Add history items from list
        for (EmergencyHistory history : historyList) {
            addHistoryItem(history);
        }

        Log.d("HistoryFragment", "✅ Loaded " + historyList.size() + " records from database");
    }

    private void loadHistoryViaContentProvider() {
        try {
            Cursor cursor = requireContext().getContentResolver().query(
                    EmergencyHistoryContract.HistoryEntry.CONTENT_URI,
                    null, // all columns
                    null, // where clause
                    null, // where args
                    EmergencyHistoryContract.HistoryEntry.COLUMN_TIMESTAMP + " DESC"
            );

            if (cursor != null && cursor.getCount() > 0) {
                tvEmptyHistory.setVisibility(View.GONE);
                historyContainer.setVisibility(View.VISIBLE);

                // Clear existing views (except first child which is the title)
                while (historyContainer.getChildCount() > 1) {
                    historyContainer.removeViewAt(1);
                }

                // Add history items from cursor
                while (cursor.moveToNext()) {
                    EmergencyHistory history = cursorToEmergencyHistory(cursor);
                    addHistoryItem(history);
                }
                cursor.close();

                Log.d("HistoryFragment", "✅ Loaded history via Content Provider");
            } else {
                showEmptyState();
                if (cursor != null) cursor.close();
            }
        } catch (Exception e) {
            Log.e("HistoryFragment", "❌ Content Provider error: " + e.getMessage());
            showEmptyState();
        }
    }

    private EmergencyHistory cursorToEmergencyHistory(Cursor cursor) {
        EmergencyHistory history = new EmergencyHistory();

        try {
            int idIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry._ID);
            int timestampIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_TIMESTAMP);
            int hospitalNameIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_HOSPITAL_NAME);
            int emergencyTypeIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_EMERGENCY_TYPE);
            int patientConditionIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_PATIENT_CONDITION);
            int statusIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_STATUS);
            int startTimeIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_START_TIME);
            int endTimeIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_END_TIME);
            int bloodGroupIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_BLOOD_GROUP);
            int responseTimeIndex = cursor.getColumnIndex(EmergencyHistoryContract.HistoryEntry.COLUMN_RESPONSE_TIME);

            if (idIndex != -1) history.setId(cursor.getLong(idIndex));
            if (timestampIndex != -1) history.setTimestamp(cursor.getLong(timestampIndex));
            if (hospitalNameIndex != -1) history.setHospitalName(cursor.getString(hospitalNameIndex));
            if (emergencyTypeIndex != -1) history.setEmergencyType(cursor.getString(emergencyTypeIndex));
            if (patientConditionIndex != -1) history.setPatientCondition(cursor.getString(patientConditionIndex));
            if (statusIndex != -1) history.setStatus(cursor.getString(statusIndex));
            if (startTimeIndex != -1) history.setStartTime(cursor.getLong(startTimeIndex));
            if (endTimeIndex != -1) history.setEndTime(cursor.getLong(endTimeIndex));
            if (bloodGroupIndex != -1) history.setBloodGroup(cursor.getString(bloodGroupIndex));
            if (responseTimeIndex != -1) history.setResponseTime(cursor.getInt(responseTimeIndex));

        } catch (Exception e) {
            Log.e("HistoryFragment", "❌ Error converting cursor to EmergencyHistory: " + e.getMessage());
        }

        return history;
    }

    private void addHistoryItem(EmergencyHistory history) {
        try {
            View historyItem = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_emergency_history, historyContainer, false);

            TextView tvHospitalName = historyItem.findViewById(R.id.tvHospitalName);
            TextView tvEmergencyType = historyItem.findViewById(R.id.tvEmergencyType);
            TextView tvDate = historyItem.findViewById(R.id.tvDate);
            TextView tvStatus = historyItem.findViewById(R.id.tvStatus);
            TextView tvDuration = historyItem.findViewById(R.id.tvDuration);
            TextView tvBloodGroup = historyItem.findViewById(R.id.tvBloodGroup);
            TextView tvResponseTime = historyItem.findViewById(R.id.tvResponseTime);

            // Set data
            tvHospitalName.setText(history.getHospitalName() != null ? history.getHospitalName() : "Unknown Hospital");
            tvEmergencyType.setText(history.getEmergencyType() != null ? history.getEmergencyType() : "Unknown Emergency");
            tvStatus.setText(history.getStatus() != null ? history.getStatus() : "Unknown");

            // Display blood group
            if (tvBloodGroup != null) {
                tvBloodGroup.setText("🩸 " + (history.getBloodGroup() != null ? history.getBloodGroup() : "Unknown"));
            }

            // Display response time
            if (tvResponseTime != null) {
                tvResponseTime.setText("⏱️ " + history.getResponseTime() + " mins");
            }

            // Format date
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            String dateStr = sdf.format(history.getStartTime());
            tvDate.setText(dateStr);

            // Calculate duration using the utility method from EmergencyHistory model
            tvDuration.setText(history.getFormattedDuration());

            // Set status color
            setStatusColor(tvStatus, history.getStatus());

            historyContainer.addView(historyItem);

        } catch (Exception e) {
            Log.e("HistoryFragment", "❌ Error adding history item: " + e.getMessage());
        }
    }

    private void setStatusColor(TextView tvStatus, String status) {
        if (status == null) return;

        switch (status.toLowerCase()) {
            case "completed":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "in progress":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "cancelled":
                tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                tvStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
        }
    }

    private void showEmptyState() {
        tvEmptyHistory.setText("No emergency history found\nComplete your first trip to see history here!");
        tvEmptyHistory.setVisibility(View.VISIBLE);
        historyContainer.setVisibility(View.GONE);
        Log.d("HistoryFragment", "ℹ️ No history data found");
    }

    private void showErrorState(String message) {
        tvEmptyHistory.setText("Error loading history\n" + message);
        tvEmptyHistory.setVisibility(View.VISIBLE);
        historyContainer.setVisibility(View.GONE);
    }

    private void logoutUser() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(requireContext(), AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}