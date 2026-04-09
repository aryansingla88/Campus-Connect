package com.campus.Campus_Connect.model;

public class Upvote {

    private int id;
    private int postId;
    private int userId;

    public Upvote() {}

    public Upvote(int id, int postId, int userId) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}