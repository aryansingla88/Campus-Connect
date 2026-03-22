package com.campus.Campus_Connect.model;

public class Comment {

    private int id;
    private String username;
    private String content;
    private String created_at;

    public Comment() {}

    public Comment(int id, String username, String content, String created_at) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.created_at = created_at;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}