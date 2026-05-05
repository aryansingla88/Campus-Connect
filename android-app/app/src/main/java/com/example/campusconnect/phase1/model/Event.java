package com.example.campusconnect.phase1.model;

public class Event {
    public String title;
    public String description;
    public double latitude;
    public double longitude;
    public String event_time;
    public int created_by;

    public Event(String title, String description, String event_time,
                 double latitude, double longitude, int created_by) {
        this.title = title;
        this.description = description;
        this.event_time = event_time;
        this.latitude = latitude;
        this.longitude = longitude;
        this.created_by = created_by;
    }
}