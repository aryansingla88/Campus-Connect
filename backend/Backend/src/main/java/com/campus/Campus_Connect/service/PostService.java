package com.campus.Campus_Connect.service;

import com.campus.Campus_Connect.model.Post;
import com.campus.Campus_Connect.repository.PostRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    //  GET POSTS (FEED)
    public List<Post> getPosts(int currentUserId) {
        return postRepository.getAllPosts(currentUserId);
    }

    //  CREATE POST
    public Post createPost(int userId, String content) {
        return postRepository.createPost(userId, content);
    }

    //  ADD COMMENT
    public void addComment(int postId, int userId, String content) {
        postRepository.addComment(postId, userId, content);
    }

    // TOGGLE UPVOTE
    public void toggleUpvote(int postId, int userId) {
        postRepository.toggleUpvote(postId, userId);
    }
}