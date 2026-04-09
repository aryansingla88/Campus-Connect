package com.example.campusconnect.model;

public class UpvoteRequest {

    private int user_id;
    private int post_id;

    public UpvoteRequest(int user_id, int post_id) {
        this.user_id = user_id;
        this.post_id = post_id;
    }
}