package com.example.campusconnect.model;

public class UserPresence {

    private int userId;        // ✅ needed for POST
    private String username;   // ✅ for GET (display)
    private double latitude;
    private double longitude;

    public UserPresence() {}  // ✅ REQUIRED for Retrofit

    public UserPresence(int userId, String username, double latitude, double longitude) {
        this.userId = userId;
        this.username = username;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public void setUserId(int userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
}