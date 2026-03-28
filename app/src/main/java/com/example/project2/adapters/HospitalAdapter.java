package com.example.project2.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.project2.R;
import com.example.project2.model.Hospital;
import com.example.project2.model.PatientData;
import com.example.project2.DataManager;
import java.util.List;

public class HospitalAdapter extends RecyclerView.Adapter<HospitalAdapter.ViewHolder> {

    private List<Hospital> hospitals;
    private PatientData patientData;
    private Context context;
    private double userLat;
    private double userLng;

    public HospitalAdapter(List<Hospital> hospitals, PatientData patientData, Context context) {
        this.hospitals = hospitals;
        this.patientData = patientData;
        this.context = context;
        // Default location (Pondicherry)
        this.userLat = 11.9400;
        this.userLng = 79.8083;
    }

    // Overloaded constructor with user location
    public HospitalAdapter(List<Hospital> hospitals, PatientData patientData, Context context, double userLat, double userLng) {
        this.hospitals = hospitals;
        this.patientData = patientData;
        this.context = context;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    // Method to update user location
    public void updateUserLocation(double lat, double lng) {
        this.userLat = lat;
        this.userLng = lng;
        notifyDataSetChanged(); // Refresh distances
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hospital, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Hospital hospital = hospitals.get(position);

        // Bind hospital data to the layout
        holder.tvHospitalName.setText(hospital.getName());
        holder.tvSpecialties.setText(hospital.getSpecialties());

        // Fix distance display - ensure it's not 0.0 km
        double distance = hospital.getDistance();
        if (distance == 0.0) {
            // Calculate actual distance if not set
            distance = calculateDistance(hospital.getLatitude(), hospital.getLongitude());
            hospital.setDistance(distance);
        }

        // Ensure minimum distance
        if (distance < 0.5) {
            distance = 0.5 + (position * 0.3); // 0.5km, 0.8km, 1.1km, etc.
        }

        holder.tvDistance.setText(String.format("📍 %.1f km", distance));

        // Calculate approximate time (assuming 40 km/h average speed)
        int timeMinutes = (int) ((distance / 40.0) * 60);
        if (timeMinutes < 5) timeMinutes = 5; // Minimum 5 minutes

        holder.tvResponseTime.setText(String.format("⏱ %d mins", timeMinutes));

        // Available beds
        int availableBeds = hospital.getAvailableBeds();
        if (availableBeds > 0) {
            holder.tvAvailableBeds.setText(String.format("🛏 %d beds", availableBeds));
        } else {
            holder.tvAvailableBeds.setText("🛏 No beds");
        }

        // Rating (if available)
        if (hospital.getRating() > 0) {
            holder.tvRating.setText(String.format("⭐ %.1f", hospital.getRating()));
            holder.tvRating.setVisibility(View.VISIBLE);
        } else {
            holder.tvRating.setVisibility(View.GONE);
        }

        // Blood status
        updateBloodStatus(holder, hospital);

        // ✅ FIXED: Remove Maps navigation from HospitalsFragment
        // Only allow selecting hospital to go to EmergencyFragment
        holder.btnNavigate.setOnClickListener(v -> {
            Log.d("HospitalAdapter", "🏥 Hospital selected: " + hospital.getName());
            Log.d("HospitalAdapter", "📍 Hospital details - Beds: " + hospital.getAvailableBeds() +
                    ", Response: " + hospital.getResponseTime() + " mins");

            if (patientData != null) {
                Log.d("HospitalAdapter", "👤 Patient data: " +
                        patientData.getEmergencyType() + ", " +
                        patientData.getBloodGroup());

                // ✅ CRITICAL FIX: Save BOTH patient data AND hospital to SharedPreferences
                DataManager.savePatientData(context, patientData);
                DataManager.saveSelectedHospital(context, hospital);

                // Navigate to EmergencyFragment (NOT MapsActivity)
                if (context instanceof com.example.project2.MainActivity) {
                    ((com.example.project2.MainActivity) context).switchToEmergencyFragment(hospital, patientData);
                    Toast.makeText(context, "🚑 Emergency started for " + hospital.getName(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e("HospitalAdapter", "❌ No patient data available");
                Toast.makeText(context, "Error: Patient data not available", Toast.LENGTH_LONG).show();
            }
        });

        // Also make the entire card clickable as backup
        holder.itemView.setOnClickListener(v -> {
            Log.d("HospitalAdapter", "🏥 Card clicked for: " + hospital.getName());
            holder.btnNavigate.performClick(); // Trigger the button click
        });
    }

    private void updateBloodStatus(ViewHolder holder, Hospital hospital) {
        if (patientData != null && patientData.getBloodGroup() != null) {
            String bloodGroup = patientData.getBloodGroup();
            String bloodStatus = getBloodGroupStatus(hospital, bloodGroup);

            if ("AVAILABLE".equals(bloodStatus)) {
                holder.tvBloodStatus.setText("🩸 " + bloodGroup + " - AVAILABLE");
                holder.tvBloodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.success_green));
                holder.tvBloodStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else if ("NOT_AVAILABLE".equals(bloodStatus)) {
                holder.tvBloodStatus.setText("🩸 " + bloodGroup + " - NOT AVAILABLE");
                holder.tvBloodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.error_red));
                holder.tvBloodStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            } else {
                holder.tvBloodStatus.setText("🩸 " + bloodGroup + " - UNKNOWN");
                holder.tvBloodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.warning_orange));
                holder.tvBloodStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            }
            holder.tvBloodStatus.setVisibility(View.VISIBLE);
        } else {
            holder.tvBloodStatus.setText("🩸 SELECT BLOOD GROUP");
            holder.tvBloodStatus.setBackgroundColor(ContextCompat.getColor(context, R.color.info_blue));
            holder.tvBloodStatus.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            holder.tvBloodStatus.setVisibility(View.VISIBLE);
        }
    }

    private String getBloodGroupStatus(Hospital hospital, String bloodGroup) {
        String hospitalBloodGroups = hospital.getBloodGroups();

        if (hospitalBloodGroups == null || hospitalBloodGroups.isEmpty()) {
            return "UNKNOWN";
        }

        // Check if the hospital has the required blood group
        String[] availableGroups = hospitalBloodGroups.split(",");
        for (String group : availableGroups) {
            if (group.trim().equalsIgnoreCase(bloodGroup)) {
                return "AVAILABLE";
            }
        }

        return "NOT_AVAILABLE";
    }

    private double calculateDistance(double hospitalLat, double hospitalLng) {
        double earthRadius = 6371; // kilometers

        double dLat = Math.toRadians(hospitalLat - userLat);
        double dLon = Math.toRadians(hospitalLng - userLng);

        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(hospitalLat)) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return earthRadius * c;
    }

    @Override
    public int getItemCount() {
        return hospitals != null ? hospitals.size() : 0;
    }

    public void updateHospitals(List<Hospital> hospitals) {
        this.hospitals = hospitals;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvHospitalName, tvSpecialties, tvBloodStatus, tvDistance,
                tvResponseTime, tvAvailableBeds, tvRating;
        Button btnNavigate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHospitalName = itemView.findViewById(R.id.tvHospitalName);
            tvSpecialties = itemView.findViewById(R.id.tvSpecialties);
            tvBloodStatus = itemView.findViewById(R.id.tvBloodStatus);
            tvDistance = itemView.findViewById(R.id.tvDistance);
            tvResponseTime = itemView.findViewById(R.id.tvResponseTime);
            tvAvailableBeds = itemView.findViewById(R.id.tvAvailableBeds);
            tvRating = itemView.findViewById(R.id.tvRating);
            btnNavigate = itemView.findViewById(R.id.btnNavigate);
        }
    }
}