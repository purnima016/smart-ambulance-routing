package com.example.project2;

public class Driver {
    private int id;
    private String firebaseUid;
    private String username;
    private String email;
    private String driverLicense;
    private String ambulanceNumber;
    private String phone;
    private String createdAt;

    // Empty constructor (IMPORTANT for SQLite)
    public Driver() {
    }

    // Constructor with parameters
    public Driver(int id, String firebaseUid, String username, String email,
                  String driverLicense, String ambulanceNumber, String phone, String createdAt) {
        this.id = id;
        this.firebaseUid = firebaseUid;
        this.username = username;
        this.email = email;
        this.driverLicense = driverLicense;
        this.ambulanceNumber = ambulanceNumber;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDriverLicense() {
        return driverLicense;
    }

    public void setDriverLicense(String driverLicense) {
        this.driverLicense = driverLicense;
    }

    public String getAmbulanceNumber() {
        return ambulanceNumber;
    }

    public void setAmbulanceNumber(String ambulanceNumber) {
        this.ambulanceNumber = ambulanceNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}