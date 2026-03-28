package com.example.project2.model;

import java.io.Serializable;

public class PatientData implements Serializable {
    private String emergencyType;
    private String ageGroup;
    private String bloodGroup;
    private String condition;
    private String paymentPreference;
    private String priorityPreference;

    // Constructors
    public PatientData() {}

    public PatientData(String emergencyType, String ageGroup, String bloodGroup, String condition) {
        this.emergencyType = emergencyType;
        this.ageGroup = ageGroup;
        this.bloodGroup = bloodGroup;
        this.condition = condition;
    }

    // Getters and Setters
    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getAgeGroup() { return ageGroup; }
    public void setAgeGroup(String ageGroup) { this.ageGroup = ageGroup; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }

    public String getPaymentPreference() { return paymentPreference; }
    public void setPaymentPreference(String paymentPreference) { this.paymentPreference = paymentPreference; }

    public String getPriorityPreference() { return priorityPreference; }
    public void setPriorityPreference(String priorityPreference) { this.priorityPreference = priorityPreference; }
}