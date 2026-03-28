package com.example.project2.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.project2.R;
import com.example.project2.model.PatientData;
import androidx.appcompat.widget.AppCompatButton;
import com.example.project2.MainActivity;
import com.example.project2.DataManager;

public class HomeFragment extends Fragment {

    private AppCompatButton btnCardiac, btnAccident, btnStroke, btnBreathing;
    private AppCompatButton btnChild, btnAdult, btnSenior;
    private AppCompatButton btnAPos, btnBPos, btnOPos, btnABPos, btnUnknown;
    private AppCompatButton btnCritical, btnSerious, btnStable;
    private AppCompatButton btnClear;
    private TextView tvSelectedOptions;

    private String selectedEmergency = "";
    private String selectedAgeGroup = "";
    private String selectedBloodGroup = "";
    private String selectedCondition = "";
    private boolean showSelectAllToast = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        initializeViews(view);
        setupClickListeners();
        updateSelectedDisplay();
        return view;
    }

    private void initializeViews(View view) {
        btnCardiac = view.findViewById(R.id.btnCardiac);
        btnAccident = view.findViewById(R.id.btnAccident);
        btnStroke = view.findViewById(R.id.btnStroke);
        btnBreathing = view.findViewById(R.id.btnBreathing);

        btnChild = view.findViewById(R.id.btnChild);
        btnAdult = view.findViewById(R.id.btnAdult);
        btnSenior = view.findViewById(R.id.btnSenior);

        btnAPos = view.findViewById(R.id.btnAPos);
        btnBPos = view.findViewById(R.id.btnBPos);
        btnOPos = view.findViewById(R.id.btnOPos);
        btnABPos = view.findViewById(R.id.btnABPos);
        btnUnknown = view.findViewById(R.id.btnUnknown);

        btnCritical = view.findViewById(R.id.btnCritical);
        btnSerious = view.findViewById(R.id.btnSerious);
        btnStable = view.findViewById(R.id.btnStable);

        tvSelectedOptions = view.findViewById(R.id.tvSelectedOptions);
        btnClear = view.findViewById(R.id.btnClear);
    }

    private void setupClickListeners() {
        btnCardiac.setOnClickListener(v -> selectEmergencyType("CARDIAC", btnCardiac));
        btnAccident.setOnClickListener(v -> selectEmergencyType("ACCIDENT", btnAccident));
        btnStroke.setOnClickListener(v -> selectEmergencyType("STROKE", btnStroke));
        btnBreathing.setOnClickListener(v -> selectEmergencyType("BREATHING", btnBreathing));

        btnChild.setOnClickListener(v -> selectAgeGroup("CHILD", btnChild));
        btnAdult.setOnClickListener(v -> selectAgeGroup("ADULT", btnAdult));
        btnSenior.setOnClickListener(v -> selectAgeGroup("SENIOR", btnSenior));

        btnAPos.setOnClickListener(v -> selectBloodGroup("A+", btnAPos));
        btnBPos.setOnClickListener(v -> selectBloodGroup("B+", btnBPos));
        btnOPos.setOnClickListener(v -> selectBloodGroup("O+", btnOPos));
        btnABPos.setOnClickListener(v -> selectBloodGroup("AB+", btnABPos));
        btnUnknown.setOnClickListener(v -> selectBloodGroup("UNKNOWN", btnUnknown));

        btnCritical.setOnClickListener(v -> selectCondition("CRITICAL", btnCritical));
        btnSerious.setOnClickListener(v -> selectCondition("SERIOUS", btnSerious));
        btnStable.setOnClickListener(v -> selectCondition("STABLE", btnStable));

        btnClear.setOnClickListener(v -> clearAllSelections());
    }

    private void selectEmergencyType(String emergencyType, AppCompatButton selectedButton) {
        resetButtonColor(btnCardiac);
        resetButtonColor(btnAccident);
        resetButtonColor(btnStroke);
        resetButtonColor(btnBreathing);
        selectedButton.setBackgroundColor(Color.GRAY);
        selectedEmergency = emergencyType;
        updateSelectedDisplay();
        checkAndNavigate();
    }

    private void selectAgeGroup(String ageGroup, AppCompatButton selectedButton) {
        resetButtonColor(btnChild);
        resetButtonColor(btnAdult);
        resetButtonColor(btnSenior);
        selectedButton.setBackgroundColor(Color.GRAY);
        selectedAgeGroup = ageGroup;
        updateSelectedDisplay();
        checkAndNavigate();
    }

    private void selectBloodGroup(String bloodGroup, AppCompatButton selectedButton) {
        resetButtonColor(btnAPos);
        resetButtonColor(btnBPos);
        resetButtonColor(btnOPos);
        resetButtonColor(btnABPos);
        resetButtonColor(btnUnknown);
        selectedButton.setBackgroundColor(Color.GRAY);
        selectedBloodGroup = bloodGroup;
        updateSelectedDisplay();
        checkAndNavigate();
    }

    private void selectCondition(String condition, AppCompatButton selectedButton) {
        resetButtonColor(btnCritical);
        resetButtonColor(btnSerious);
        resetButtonColor(btnStable);
        selectedButton.setBackgroundColor(Color.GRAY);
        selectedCondition = condition;
        updateSelectedDisplay();
        checkAndNavigate();
    }

    private void resetButtonColor(AppCompatButton button) {
        button.setBackgroundColor(Color.parseColor("#6A0DAD"));
        button.setTextColor(Color.WHITE);
    }

    private void updateSelectedDisplay() {
        StringBuilder selectedText = new StringBuilder("Selected: ");

        if (!selectedEmergency.isEmpty()) selectedText.append(selectedEmergency).append(" | ");
        if (!selectedAgeGroup.isEmpty()) selectedText.append(selectedAgeGroup).append(" | ");
        if (!selectedBloodGroup.isEmpty()) selectedText.append(selectedBloodGroup).append(" | ");
        if (!selectedCondition.isEmpty()) selectedText.append(selectedCondition);

        String finalText = selectedText.toString();
        if (finalText.endsWith(" | ")) finalText = finalText.substring(0, finalText.length() - 3);
        if (finalText.equals("Selected: ")) finalText = "Selected: None";

        tvSelectedOptions.setText(finalText);
        btnClear.setVisibility(!selectedEmergency.isEmpty() || !selectedAgeGroup.isEmpty() ||
                !selectedBloodGroup.isEmpty() || !selectedCondition.isEmpty() ?
                View.VISIBLE : View.GONE);
    }

    private void checkAndNavigate() {
        if (!selectedEmergency.isEmpty() && !selectedAgeGroup.isEmpty() &&
                !selectedBloodGroup.isEmpty() && !selectedCondition.isEmpty()) {

            Log.d("DEBUG", "🎯 HomeFragment: All 4 details selected!");

            PatientData patientData = new PatientData();
            patientData.setEmergencyType(selectedEmergency);
            patientData.setAgeGroup(selectedAgeGroup);
            patientData.setBloodGroup(selectedBloodGroup);
            patientData.setCondition(selectedCondition);

            // ✅ SAVE DATA USING SHAREDPREFS (BACKUP METHOD)
            DataManager.savePatientData(getContext(), patientData);

            // Quick toast and immediate navigation
            Toast.makeText(getContext(), "Going to preferences...", Toast.LENGTH_SHORT).show();

            // Navigate immediately without delay
            if (getActivity() instanceof com.example.project2.MainActivity) {
                ((com.example.project2.MainActivity) getActivity()).switchToPreferencesFragment(patientData);
            }
        } else {
            // Only show "Select all details" ONCE when at least one is selected but not all
            if ((!selectedEmergency.isEmpty() || !selectedAgeGroup.isEmpty() ||
                    !selectedBloodGroup.isEmpty() || !selectedCondition.isEmpty()) && showSelectAllToast) {
                Toast.makeText(getContext(), "Select all details", Toast.LENGTH_SHORT).show();
                showSelectAllToast = false; // Set to false so it doesn't show again
            }
        }
    }

    private void clearAllSelections() {
        selectedEmergency = "";
        selectedAgeGroup = "";
        selectedBloodGroup = "";
        selectedCondition = "";
        showSelectAllToast = true; // Reset the flag when clearing selections

        resetButtonColor(btnCardiac);
        resetButtonColor(btnAccident);
        resetButtonColor(btnStroke);
        resetButtonColor(btnBreathing);
        resetButtonColor(btnChild);
        resetButtonColor(btnAdult);
        resetButtonColor(btnSenior);
        resetButtonColor(btnAPos);
        resetButtonColor(btnBPos);
        resetButtonColor(btnOPos);
        resetButtonColor(btnABPos);
        resetButtonColor(btnUnknown);
        resetButtonColor(btnCritical);
        resetButtonColor(btnSerious);
        resetButtonColor(btnStable);

        updateSelectedDisplay();
        Toast.makeText(getContext(), "Selections cleared", Toast.LENGTH_SHORT).show();
    }
}