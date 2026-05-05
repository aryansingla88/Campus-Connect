package com.example.campusconnect.phase1.model;

import java.util.List;

public class Post {

    private int id;
    private String username;
    private String content;
    private int upvotes;
    private boolean has_upvoted;
    private String created_at;

    private List<Comment> comments;

    public int getId() {
        return id;
    }
    public boolean isHas_upvoted() {
        return has_upvoted;
    }

    public String getUsername() {
        return username;
    }

    public String getContent() {
        return content;
    }

    public int getUpvotes() {
        return upvotes;
    }

    public String getCreated_at() {
        return created_at;
    }

    public List<Comment> getComments() {
        return comments;
    }
    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    public void setHas_upvoted(boolean has_upvoted) {
        this.has_upvoted = has_upvoted;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}