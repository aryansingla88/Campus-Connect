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
import com.campus.Campus_Connect.features.post.entity.*;
import com.campus.Campus_Connect.features.post.exception.PostNotFoundException;
import com.campus.Campus_Connect.features.post.mapper.PostMapper;
import com.campus.Campus_Connect.features.post.repository.CommentRepository;
import com.campus.Campus_Connect.features.post.repository.PostRepository;
import com.campus.Campus_Connect.features.post.repository.PostTagRepository;
import com.campus.Campus_Connect.features.post.repository.PostVoteRepository;
import com.campus.Campus_Connect.features.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        Integer currentUserId = SecurityUtils.getCurrentUserId();

        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(post -> postMapper.toPostResponse(post, currentUserId))
                .toList();
    }

    @Override
    public PostResponse getPostById(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        return postMapper.toPostResponse(post, SecurityUtils.getCurrentUserId());
    }
    @Override
    public List<PostTagResponse> getAllTags() {

        return postMapper.toPostTagResponseList(
                postTagRepository.findAllByOrderByNameAsc()
        );
    }
    @Transactional
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
        post.setCreator(SecurityUtils.getCurrentUser());

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
    @Transactional
    @Override
    public PostResponse updatePost(Integer postId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        // Only creator can update
        if (!post.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("You are not allowed to edit this post.");
        }

        // Validate tags
        List<PostTag> tags = postTagRepository.findAllById(request.getTags());

        if (tags.size() != request.getTags().size()) {
            throw new IllegalArgumentException("One or more tags are invalid.");
        }

        // Update fields
        post.setTitle(request.getTitle());
        post.setContentRaw(request.getBody());
        post.setTags(new HashSet<>(tags));
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        return postMapper.toPostResponse(
                updatedPost,
                SecurityUtils.getCurrentUserId()
        );
    }
    @Transactional
    @Override
    public void deletePost(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        // Only creator can delete
        if (!post.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("You are not allowed to delete this post.");
        }

        postRepository.delete(post);
    }
    @Override
    public List<CommentResponse> getComments(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        return commentRepository.findByPostOrderByCreatedAtAsc(post)
                .stream()
                .map(postMapper::toCommentResponse)
                .toList();
    }
    @Transactional
    @Override
    public CommentResponse createComment(Integer postId, CreateCommentRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (!post.getAllowComments()) {
            throw new IllegalArgumentException("Comments are disabled for this post.");
        }

        Comment parentComment = null;

        if (request.getParentCommentId() != null) {

            parentComment = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Parent comment not found.")
                    );
        }

        Comment comment = Comment.builder()
                .post(post)
                .creator(SecurityUtils.getCurrentUser())
                .parentComment(parentComment)
                .content(request.getBody())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Comment savedComment = commentRepository.save(comment);

        return postMapper.toCommentResponse(savedComment);
    }
    @Transactional
    @Override
    public CommentResponse updateComment(Integer commentId, UpdateCommentRequest request) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found.")
                );

        // Only creator can edit
        if (!comment.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("You are not allowed to edit this comment.");
        }

        comment.setContent(request.getBody());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return postMapper.toCommentResponse(updatedComment);
    }

    @Transactional
    @Override
    public void deleteComment(Integer commentId) {

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Comment not found.")
                );

        // Only creator can delete
        if (!comment.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new IllegalArgumentException("You are not allowed to delete this comment.");
        }

        commentRepository.delete(comment);
    }
    @Transactional
    @Override
    public void upvotePost(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        Integer currentUserId = SecurityUtils.getCurrentUserId();

        PostVote vote = postVoteRepository
                .findByPostAndIdUserId(post, currentUserId)
                .orElse(null);

        if (vote == null) {

            vote = PostVote.builder()
                    .id(new PostVoteId(
                            post.getId(),
                            currentUserId
                    ))
                    .post(post)
                    .voteType(VoteType.UPVOTE)
                    .createdAt(LocalDateTime.now())
                    .build();

        } else {

            vote.setVoteType(VoteType.UPVOTE);
        }

        postVoteRepository.save(vote);
    }
    @Transactional
    @Override
    public void downvotePost(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        Integer currentUserId = SecurityUtils.getCurrentUserId();

        PostVote vote = postVoteRepository
                .findByPostAndIdUserId(post, currentUserId)
                .orElse(null);

        if (vote == null) {

            vote = PostVote.builder()
                    .id(new PostVoteId(
                            post.getId(),
                            currentUserId
                    ))
                    .post(post)
                    .voteType(VoteType.DOWNVOTE)
                    .createdAt(LocalDateTime.now())
                    .build();

        } else {

            vote.setVoteType(VoteType.DOWNVOTE);
        }

        postVoteRepository.save(vote);
    }
    @Transactional
    @Override
    public void removeVote(Integer postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        postVoteRepository.deleteByPostAndIdUserId(
                post,
                SecurityUtils.getCurrentUserId()
        );
    }
}