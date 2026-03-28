package com.example.project2.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.project2.model.PatientData;
import com.example.project2.MainActivity;
import com.example.project2.R;
import com.example.project2.DataManager;

public class PreferencesFragment extends Fragment {

    private PatientData patientData;
    private TextView tvPatientDetails, tvRecommendation;
    private RadioGroup paymentRadioGroup, priorityRadioGroup;
    private String selectedPayment = "", selectedPriority = "";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_preferences, container, false);

        Log.d("DEBUG", "🎯 PreferencesFragment: onCreateView called");

        // ✅ INITIALIZE VIEWS
        tvPatientDetails = view.findViewById(R.id.tvPatientDetails);
        tvRecommendation = view.findViewById(R.id.tvRecommendation);
        paymentRadioGroup = view.findViewById(R.id.paymentRadioGroup);
        priorityRadioGroup = view.findViewById(R.id.priorityRadioGroup);

        // ✅ METHOD 1: Try to get data from Bundle
        if (getArguments() != null) {
            patientData = (PatientData) getArguments().getSerializable("patientData");
        }

        // ✅ METHOD 2: If Bundle failed, get from SharedPreferences (BACKUP)
        if (patientData == null) {
            patientData = DataManager.getPatientData(getContext());
        }

        // ✅ METHOD 3: If both failed, show error
        if (patientData == null) {
            tvPatientDetails.setText("❌ No patient data available");
            return view;
        }

        // ✅ SUCCESS: Update UI with patient data
        updatePatientDetails();
        setupRadioListeners();

        return view;
    }

    private void updatePatientDetails() {
        String details = getAgeIcon(patientData.getAgeGroup()) + " " + patientData.getAgeGroup() +
                " | " + getEmergencyIcon(patientData.getEmergencyType()) + " " + patientData.getEmergencyType() +
                " | " + getConditionIcon(patientData.getCondition()) + " " + patientData.getCondition() +
                " | Blood: " + patientData.getBloodGroup();

        tvPatientDetails.setText(details);
        showRecommendation();
    }

    private void showRecommendation() {
        if (patientData.getCondition() != null) {
            String condition = patientData.getCondition();
            if ("CRITICAL".equals(condition)) {
                tvRecommendation.setText("💡 RECOMMENDED: ULTRA FAST-NEAREST");
            } else if ("SERIOUS".equals(condition)) {
                tvRecommendation.setText("💡 RECOMMENDED: QUALITY AND SPEED");
            } else {
                tvRecommendation.setText("💡 RECOMMENDED: COST EFFECTIVE");
            }
        }
    }

    private void setupRadioListeners() {
        paymentRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.paymentPrivate) {
                selectedPayment = "Private Insurance/Cash";
            } else if (checkedId == R.id.paymentGovernment) {
                selectedPayment = "Government Health Scheme";
            } else if (checkedId == R.id.paymentNoInsurance) {
                selectedPayment = "No Insurance-Self Pay";
            }
            Log.d("DEBUG", "💰 Payment selected: " + selectedPayment);
            checkAndNavigate();
        });

        priorityRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.priorityNearest) {
                selectedPriority = "Ultra Fast-Nearest";
            } else if (checkedId == R.id.priorityNearestGood) {
                selectedPriority = "Quality And Speed-Rated 4+";
            } else if (checkedId == R.id.priorityAffordable) {
                selectedPriority = "Cost Effective-Government";
            } else if (checkedId == R.id.priorityTopPrivate) {
                selectedPriority = "Premium Care-Top Private";
            }
            Log.d("DEBUG", "🎯 Priority selected: " + selectedPriority);
            checkAndNavigate();
        });
    }

    private void checkAndNavigate() {
        Log.d("DEBUG", "🔍 Checking: Payment=" + selectedPayment + ", Priority=" + selectedPriority);

        if (!selectedPayment.isEmpty() && !selectedPriority.isEmpty()) {
            Log.d("DEBUG", "✅ ALL CONDITIONS MET! Navigating to Hospitals...");

            // Set preferences to patient data
            patientData.setPaymentPreference(selectedPayment);
            patientData.setPriorityPreference(selectedPriority);

            // Save updated data with preferences
            DataManager.savePatientData(getContext(), patientData);

            // Navigate immediately without toast
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).switchToHospitalsFragment(patientData);
            }
        } else {
            // Only show toast if at least one is selected but not both
            if (!selectedPayment.isEmpty() || !selectedPriority.isEmpty()) {
                Toast.makeText(getContext(), "Select all preferences", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getEmergencyIcon(String type) {
        switch (type) {
            case "CARDIAC": return "❤️";
            case "ACCIDENT": return "🚗";
            case "STROKE": return "🧠";
            case "BREATHING": return "🌬️";
            default: return "🆘";
        }
    }

    private String getAgeIcon(String age) {
        switch (age) {
            case "CHILD": return "👶";
            case "ADULT": return "👨";
            case "SENIOR": return "👴";
            default: return "👤";
        }
    }

    private String getConditionIcon(String condition) {
        switch (condition) {
            case "CRITICAL": return "🔴";
            case "SERIOUS": return "🟡";
            case "STABLE": return "🟢";
            default: return "📊";
        }
    }
}