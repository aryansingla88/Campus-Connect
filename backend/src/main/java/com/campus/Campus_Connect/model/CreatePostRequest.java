package com.campus.Campus_Connect.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreatePostRequest {

    @JsonProperty("user_id")
    private int userId;

    private String content;

    public CreatePostRequest() {}

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}