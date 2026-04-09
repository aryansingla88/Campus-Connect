package com.campus.Campus_Connect.model;

import java.util.List;

public class Post {

    private int id;
    private String username;
    private String content;
    private int upvotes;
    private String created_at;
    private boolean has_upvoted;
    private List<Comment> comments;

    public Post() {}

    public Post(int id, String username, String content, int upvotes,
                String created_at, boolean has_upvoted, List<Comment> comments) {
        this.id = id;
        this.username = username;
        this.content = content;
        this.upvotes = upvotes;
        this.created_at = created_at;
        this.has_upvoted = has_upvoted;
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {  return content; }

    public void setContent(String content) { this.content = content; }

    public int getUpvotes() {
        return upvotes;
    }

    public void setUpvotes(int upvotes) {
        this.upvotes = upvotes;
    }


    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }


    public boolean getHas_upvoted() {
        return has_upvoted;
    }

    public void setHas_upvoted(boolean has_upvoted) {
        this.has_upvoted = has_upvoted;
    }


    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}