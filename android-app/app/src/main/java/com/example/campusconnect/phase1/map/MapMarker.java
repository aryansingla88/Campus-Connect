package com.example.campusconnect.map;

public class MapMarker {

    private MarkerType type;
    private double latitude;
    private double longitude;
    private String label;

    private float x;
    private float y;

    public MapMarker(MarkerType type, double latitude, double longitude, String label) {
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.label = label;
    }

    public MarkerType getType() {
        return type;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getLabel() {
        return label;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setPixelPosition(float x, float y) {
        this.x = x;
        this.y = y;
    }
}