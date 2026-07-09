package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.common.service.FileStorageService;
import com.campus.Campus_Connect.features.post.dto.request.CreateCommentRequest;
import com.campus.Campus_Connect.features.post.dto.request.CreatePostRequest;
import com.campus.Campus_Connect.features.post.dto.request.UpdateCommentRequest;
import com.campus.Campus_Connect.features.post.dto.request.UpdatePostRequest;
import com.campus.Campus_Connect.features.post.dto.response.CommentResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostTagResponse;
import com.campus.Campus_Connect.features.post.entity.Post;
import com.campus.Campus_Connect.features.post.entity.PostImage;
import com.campus.Campus_Connect.features.post.entity.PostTag;
import com.campus.Campus_Connect.features.post.exception.PostNotFoundException;
import com.campus.Campus_Connect.features.post.mapper.PostMapper;
import com.campus.Campus_Connect.features.post.repository.CommentRepository;
import com.campus.Campus_Connect.features.post.repository.PostRepository;
import com.campus.Campus_Connect.features.post.repository.PostTagRepository;
import com.campus.Campus_Connect.features.post.repository.PostVoteRepository;
import com.campus.Campus_Connect.features.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final FileStorageService fileStorageService;

    private final PostRepository postRepository;

    private final PostTagRepository postTagRepository;

    private final CommentRepository commentRepository;

    private final PostVoteRepository postVoteRepository;

    private final PostMapper postMapper;

    @Override
    public List<PostResponse> getAllPosts() {

        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> postMapper.toPostResponse(post, null))
                .toList();
    }

    @Override
    public PostResponse getPostById(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        return postMapper.toPostResponse(post, null);
    }
    @Override
    public List<PostTagResponse> getAllTags() {

        return postMapper.toPostTagResponseList(
                postTagRepository.findAllByOrderByNameAsc()
        );
    }

    @Override
    public PostResponse createPost(CreatePostRequest request) {

        Post post = new Post();

        // Basic fields
        post.setTitle(request.getTitle());
        post.setContentRaw(request.getBody());
        post.setPostType(request.getPostType());

        // Defaults
        post.setVisibilityType("PUBLIC");
        post.setVisibilityValue(null);
        post.setAllowComments(true);

        // Logged-in user
        post.setCreatorId(SecurityUtils.getCurrentUserId());

        // Tags
        List<PostTag> tags = postTagRepository.findAllById(request.getTags());

        if (tags.size() != request.getTags().size()) {
            throw new IllegalArgumentException("One or more tags are invalid.");
        }

        post.setTags(new HashSet<>(tags));

        // Time
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Save Post first
        Post savedPost = postRepository.save(post);

        // Image
        if (request.getImage() != null && !request.getImage().isEmpty()) {

            String imagePath =
                    fileStorageService.storePostImage(request.getImage());

            PostImage postImage = PostImage.builder()
                    .post(savedPost)
                    .imageUrl(imagePath)
                    .imageOrder(1)
                    .createdAt(LocalDateTime.now())
                    .build();

            savedPost.getImages().add(postImage);

            savedPost = postRepository.save(savedPost);
        }

        return postMapper.toPostResponse(
                savedPost,
                SecurityUtils.getCurrentUserId()
        );
    }

    @Override
    public PostResponse updatePost(Integer postId, UpdatePostRequest request) {
        return null;
    }

    @Override
    public void deletePost(Integer postId) {

    }

    @Override
    public List<CommentResponse> getComments(Integer postId) {
        return null;
    }

    @Override
    public CommentResponse createComment(Integer postId, CreateCommentRequest request) {
        return null;
    }

    @Override
    public CommentResponse updateComment(Integer commentId, UpdateCommentRequest request) {
        return null;
    }

    @Override
    public void deleteComment(Integer commentId) {

    }

    @Override
    public void upvotePost(Integer postId) {

    }

    @Override
    public void downvotePost(Integer postId) {

    }

    @Override
    public void removeVote(Integer postId) {

    }
}