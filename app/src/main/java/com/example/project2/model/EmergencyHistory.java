package com.example.project2.model;

import android.os.Parcel;
import android.os.Parcelable;

public class EmergencyHistory implements Parcelable {
    private long id;
    private long timestamp;
    private String hospitalName;
    private String emergencyType;
    private String patientCondition;
    private String status;
    private long startTime;
    private long endTime;
    private String bloodGroup;
    private int responseTime;

    // Default constructor
    public EmergencyHistory() {}

    // Full constructor
    public EmergencyHistory(long timestamp, String hospitalName, String emergencyType,
                            String patientCondition, String status, long startTime,
                            long endTime, String bloodGroup, int responseTime) {
        this.timestamp = timestamp;
        this.hospitalName = hospitalName;
        this.emergencyType = emergencyType;
        this.patientCondition = patientCondition;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bloodGroup = bloodGroup;
        this.responseTime = responseTime;
    }

    // Parcelable constructor
    protected EmergencyHistory(Parcel in) {
        id = in.readLong();
        timestamp = in.readLong();
        hospitalName = in.readString();
        emergencyType = in.readString();
        patientCondition = in.readString();
        status = in.readString();
        startTime = in.readLong();
        endTime = in.readLong();
        bloodGroup = in.readString();
        responseTime = in.readInt();
    }

    // Getters and setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = hospitalName; }

    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }

    public String getPatientCondition() { return patientCondition; }
    public void setPatientCondition(String patientCondition) { this.patientCondition = patientCondition; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int responseTime) { this.responseTime = responseTime; }

    // Utility methods
    public long getDuration() {
        return endTime - startTime;
    }

    public String getFormattedDuration() {
        long duration = getDuration();
        long minutes = duration / (60 * 1000);
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else {
            return minutes + " minutes";
        }
    }

    public boolean isCompleted() {
        return "Completed".equalsIgnoreCase(status);
    }

    public boolean isInProgress() {
        return "In Progress".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "EmergencyHistory{" +
                "id=" + id +
                ", hospitalName='" + hospitalName + '\'' +
                ", emergencyType='" + emergencyType + '\'' +
                ", bloodGroup='" + bloodGroup + '\'' +
                ", status='" + status + '\'' +
                ", responseTime=" + responseTime +
                '}';
    }

    // Parcelable implementation
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(timestamp);
        dest.writeString(hospitalName);
        dest.writeString(emergencyType);
        dest.writeString(patientCondition);
        dest.writeString(status);
        dest.writeLong(startTime);
        dest.writeLong(endTime);
        dest.writeString(bloodGroup);
        dest.writeInt(responseTime);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<EmergencyHistory> CREATOR = new Creator<EmergencyHistory>() {
        @Override
        public EmergencyHistory createFromParcel(Parcel in) {
            return new EmergencyHistory(in);
        }

        @Override
        public EmergencyHistory[] newArray(int size) {
            return new EmergencyHistory[size];
        }
    };
}