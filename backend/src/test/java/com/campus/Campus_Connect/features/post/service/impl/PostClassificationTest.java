package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.PostClassification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostClassificationTest {

    @Test
    void classifiesByEventRelationshipThenVisibilityScope() {
        Post event = Post.builder().eventId(7).visibilityType("CLUB").build();
        Post custom = Post.builder().visibilityType("HOSTEL").build();
        Post general = Post.builder().build();

        assertEquals(PostClassification.EVENTS, PostClassification.classify(event));
        assertEquals(PostClassification.CUSTOM, PostClassification.classify(custom));
        assertEquals(PostClassification.GENERAL, PostClassification.classify(general));
    }
}
