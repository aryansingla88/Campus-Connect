package com.campus.Campus_Connect.controller;

import com.campus.Campus_Connect.model.Post;
import com.campus.Campus_Connect.service.PostService;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.List;

@RestController
@RequestMapping("/api")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getPosts() {

        int currentUserId = 1;

        List<Post> posts = postService.getPosts(currentUserId);

        return ResponseEntity.ok(posts);
    }

    @PostMapping("/posts")
    public ResponseEntity<Post> createPost(
            @RequestBody Map<String, String> body
    ) {
        String content = body.get("content");

        int userId = 1; // TEMP

        Post post = postService.createPost(userId, content);

        return ResponseEntity.status(HttpStatus.CREATED).body(post);
    }

    @PostMapping("/comments")
    public ResponseEntity<String> addComment(@RequestBody Map<String, String> body) {
        try {
            System.out.println("BODY: " + body);

            int postId = Integer.parseInt(body.get("post_id"));
            int userId = Integer.parseInt(body.get("user_id"));
            String content = body.get("content");

            postService.addComment(postId, userId, content);

            return ResponseEntity.ok("Comment added successfully");

        } catch (Exception e) {
            e.printStackTrace(); // 👈 ADD THIS
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }

    @PostMapping("/upvote")
    public ResponseEntity<String> toggleUpvote(@RequestBody Map<String, String> body) {
        try {
            int postId = Integer.parseInt(body.get("post_id"));
            int userId = Integer.parseInt(body.get("user_id"));

            postService.toggleUpvote(postId, userId);

            return ResponseEntity.ok("Upvote toggled");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid request");
        }
    }
}