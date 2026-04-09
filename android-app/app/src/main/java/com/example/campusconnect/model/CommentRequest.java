package com.example.campusconnect.model;

public class CommentRequest {

    private int user_id;
    private int post_id;
    private String content;

    public CommentRequest(int user_id, int post_id, String content) {
        this.user_id = user_id;
        this.post_id = post_id;
        this.content = content;
    }
}