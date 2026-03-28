package com.example.project2.model;

import java.io.Serializable;

public class Hospital implements Serializable {
    private int id;
    private String name;
    private String address;
    private String phone;
    private double latitude;
    private double longitude;
    private String emergencyTypes;
    private String bloodGroups;
    private int availableBeds;
    private int icuBeds;
    private String specialties;
    private int responseTime;

    // Additional fields from your current Hospital class
    private String type;
    private double rating;
    private double distance;
    private int fees;
    private String availability;
    private String insurance;

    public Hospital() {}

    // Constructor for database hospitals
    public Hospital(int id, String name, String address, String phone,
                    double latitude, double longitude, String emergencyTypes,
                    String bloodGroups, int availableBeds, int icuBeds,
                    String specialties, int responseTime) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.emergencyTypes = emergencyTypes;
        this.bloodGroups = bloodGroups;
        this.availableBeds = availableBeds;
        this.icuBeds = icuBeds;
        this.specialties = specialties;
        this.responseTime = responseTime;
    }

    // Constructor for your current hospitals
    public Hospital(String name, String type, String specialties, double rating,
                    double distance, int fees, String availability, String insurance,
                    double latitude, double longitude) {
        this.name = name;
        this.type = type;
        this.specialties = specialties;
        this.rating = rating;
        this.distance = distance;
        this.fees = fees;
        this.availability = availability;
        this.insurance = insurance;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // Getters and Setters for ALL fields
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getEmergencyTypes() { return emergencyTypes; }
    public void setEmergencyTypes(String emergencyTypes) { this.emergencyTypes = emergencyTypes; }

    public String getBloodGroups() { return bloodGroups; }
    public void setBloodGroups(String bloodGroups) { this.bloodGroups = bloodGroups; }

    public int getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(int availableBeds) { this.availableBeds = availableBeds; }

    public int getIcuBeds() { return icuBeds; }
    public void setIcuBeds(int icuBeds) { this.icuBeds = icuBeds; }

    public String getSpecialties() {
        if (specialties != null && !specialties.isEmpty()) {
            return specialties;
        }
        return emergencyTypes != null ? emergencyTypes : "General Hospital";
    }
    public void setSpecialties(String specialties) { this.specialties = specialties; }

    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int responseTime) { this.responseTime = responseTime; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public double getDistance() { return distance; }
    public void setDistance(double distance) { this.distance = distance; }

    public int getFees() { return fees; }
    public void setFees(int fees) { this.fees = fees; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getInsurance() { return insurance; }
    public void setInsurance(String insurance) { this.insurance = insurance; }

    // Helper method to get blood stock status for specific blood group
    public String getBloodStockStatus(String bloodGroup) {
        if (bloodGroups == null || bloodGroup == null) {
            return "UNKNOWN";
        }

        if (bloodGroups.toLowerCase().contains(bloodGroup.toLowerCase())) {
            return "AVAILABLE";
        } else {
            return "NOT AVAILABLE";
        }
    }

    // Helper method to check if hospital has specific emergency type
    public boolean hasEmergencyType(String emergencyType) {
        if (emergencyTypes == null || emergencyType == null) {
            return false;
        }
        return emergencyTypes.toLowerCase().contains(emergencyType.toLowerCase());
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "name='" + name + '\'' +
                ", emergencyTypes='" + emergencyTypes + '\'' +
                ", bloodGroups='" + bloodGroups + '\'' +
                ", responseTime=" + responseTime +
                ", distance=" + distance +
                '}';
    }
}