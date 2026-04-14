package com.campus.Campus_Connect.model;

public class CreatePostRequest {

    private int userId;
    private String content;

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}