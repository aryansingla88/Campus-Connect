package com.example.campusconnect.phase1.model;

public class Comment {

    private int id;
    private String username;
    private String content;
    private String created_at;

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setContent(String content) {
        this.content = content;
    }
}