package com.campus.Campus_Connect.features.post;

import com.campus.Campus_Connect.features.post.entity.Post;

/** Backend-owned classification derived from a post's event relationship and visibility scope. */
public enum PostClassification {
    EVENTS,
    CUSTOM,
    GENERAL;

    public static PostClassification classify(Post post) {
        if (post.getEventId() != null) {
            return EVENTS;
        }
        return post.getVisibilityType() != null ? CUSTOM : GENERAL;
    }
}
