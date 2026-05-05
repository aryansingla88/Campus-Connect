package com.example.campusconnect.phase1.model;

public class LoginResponse {

    private boolean success;
    private String message;
    private int user_id;

    public boolean isSuccess() {
        return success;
    }
    public String getMessage(){return message;}
    public int getUser_id() {
        return user_id;
    }
}