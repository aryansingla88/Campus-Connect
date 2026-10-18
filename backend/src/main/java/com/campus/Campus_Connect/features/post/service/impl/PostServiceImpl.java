package com.campus.Campus_Connect.features.post.service.impl;

import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ForbiddenException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.storage.MediaStorageService;
import com.campus.Campus_Connect.common.storage.MediaType;
import com.campus.Campus_Connect.features.post.dto.request.CreateCommentRequest;
import com.campus.Campus_Connect.features.post.dto.request.CreatePostRequest;
import com.campus.Campus_Connect.features.post.dto.request.UpdateCommentRequest;
import com.campus.Campus_Connect.features.post.dto.request.UpdatePostRequest;
import com.campus.Campus_Connect.features.post.dto.response.CommentResponse;
import com.campus.Campus_Connect.features.post.dto.response.CommentDeleteResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostTagResponse;
import com.campus.Campus_Connect.features.post.dto.response.PostTagCountResponse;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final MediaStorageService mediaStorageService;

    private final PostRepository postRepository;

    private final PostTagRepository postTagRepository;

    private final CommentRepository commentRepository;

    private final PostVoteRepository postVoteRepository;

    private final PostMapper postMapper;


    @Override
    public List<PostResponse> getAllPosts() {

        Integer currentUserId = SecurityUtils.getCurrentUserId();

        List<Post> posts = postRepository.findAllByPostTypeAndDeletedAtIsNullOrderByCreatedAtDesc("GENERAL");
        if (posts.isEmpty()) {
            return List.of();
        }

        List<Integer> postIds = posts.stream().map(Post::getId).toList();
        Map<Integer, Long> commentCounts = commentRepository.countByPostIds(postIds)
                .stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountProjection::getPostId,
                        CommentRepository.CommentCountProjection::getCommentCount
                ));

        return posts
                .stream()
                .map(post -> postMapper.toPostResponse(
                        post,
                currentUserId,
                        commentCounts.getOrDefault(post.getId(), 0L)
                ))
                .toList();
    }

    @Override
    public PostResponse getPostById(Integer postId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );
        return postMapper.toPostResponse(
                post,
                SecurityUtils.getCurrentUserId(),
                commentRepository.countByPost(post)
        );
    }
    @Override
    public List<PostTagResponse> getAllTags() {

        return postMapper.toPostTagResponseList(
                postTagRepository.findAllByOrderByNameAsc()
        );
    }

    @Override
    public List<PostTagCountResponse> getGeneralPostTagCounts() {
        return postTagRepository.findGeneralPostCountsByTag()
                .stream()
                .map(tag -> PostTagCountResponse.builder()
                        .id(tag.getId())
                        .name(tag.getName())
                        .postCount(tag.getPostCount())
                        .build())
                .toList();
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
            throw new BadRequestException("One or more tags are invalid.");
        }

        post.setTags(new HashSet<>(tags));

        // Time
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        // Save Post first
        Post savedPost = postRepository.save(post);

        // Image
        if (request.getImage() != null && !request.getImage().isEmpty()) {

            String contentType = request.getImage().getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed.");
            }

            String imagePath =
                    mediaStorageService.store(
                            request.getImage(),
                            MediaType.POST_IMAGE
                    );

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
                SecurityUtils.getCurrentUserId(),
                commentRepository.countByPost(savedPost)
        );
    }
    @Transactional
    @Override
    public PostResponse updatePost(Integer postId, UpdatePostRequest request) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Deleted posts cannot be edited.");
        }

        // Only creator can update
        if (!post.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenException("You are not allowed to edit this post.");
        }

        // Validate tags
        List<PostTag> tags = postTagRepository.findAllById(request.getTags());

        if (tags.size() != request.getTags().size()) {
            throw new BadRequestException("One or more tags are invalid.");
        }

        // Update fields
        post.setTitle(request.getTitle());
        post.setContentRaw(request.getBody());
        post.setTags(new HashSet<>(tags));
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        return postMapper.toPostResponse(
                updatedPost,
                SecurityUtils.getCurrentUserId(),
                commentRepository.countByPost(updatedPost)
        );
    }
    @Transactional
    @Override
    public void deletePost(Integer postId) {

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        // Only creator can delete
        if (!post.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenException("You are not allowed to delete this post.");
        }

        if (post.getDeletedAt() == null) {
            post.getImages().forEach(image -> mediaStorageService.delete(image.getImageUrl()));
            post.getImages().clear();
            post.setTitle("Post deleted");
            post.setContentRaw("Post deleted");
            post.setContentRendered(null);
            post.setDeletedAt(LocalDateTime.now());
            postRepository.save(post);
        }

    }
    @Override
    public List<CommentResponse> getComments(Integer postId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
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

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (!post.getAllowComments()) {
            throw new IllegalArgumentException("Comments are disabled for this post.");
        }

        Comment parentComment = null;

        if (request.getParentCommentId() != null) {

            parentComment = commentRepository.findByIdForUpdate(request.getParentCommentId())
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Parent comment not found.")
                    );

            if (!parentComment.getPost().getId().equals(postId)) {
                throw new ResourceNotFoundException("Parent comment not found.");
            }
            if (parentComment.getParentComment() != null) {
                throw new BadRequestException("Replies can only be added to top-level comments.");
            }
            if (parentComment.getDeletedAt() != null) {
                throw new BadRequestException("Cannot reply to a deleted comment.");
            }
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

        Comment comment = commentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found.")
                );

        if (comment.getPost().getDeletedAt() != null) {
            throw new PostNotFoundException("Post not found with id: " + comment.getPost().getId());
        }

        if (comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment not found.");
        }

        // Only creator can edit
        if (!comment.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenException("You are not allowed to edit this comment.");
        }

        if (request == null || request.getBody() == null || request.getBody().isBlank()) {
            throw new BadRequestException("Comment body is required.");
        }

        comment.setContent(request.getBody());
        comment.setUpdatedAt(LocalDateTime.now());

        Comment updatedComment = commentRepository.save(comment);

        return postMapper.toCommentResponse(updatedComment);
    }

    @Transactional
    @Override
    public CommentDeleteResponse deleteComment(Integer commentId) {

        Comment initialComment = commentRepository.findById(commentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Comment not found.")
                );

        Comment parentComment = null;
        if (initialComment.getParentComment() != null) {
            Integer parentCommentId = initialComment.getParentComment().getId();
            parentComment = commentRepository.findByIdForUpdate(parentCommentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));
        }

        // Lock parent before reply so reply creation and sibling deletions serialize consistently.
        Comment comment = commentRepository.findByIdForUpdate(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comment not found."));

        if ((comment.getParentComment() == null) != (parentComment == null)
                || (parentComment != null
                && !comment.getParentComment().getId().equals(parentComment.getId()))) {
            throw new ResourceNotFoundException("Comment not found.");
        }

        if (comment.getPost().getDeletedAt() != null) {
            throw new PostNotFoundException("Post not found with id: " + comment.getPost().getId());
        }

        if (comment.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Comment not found.");
        }

        // Only creator can delete
        if (!comment.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new ForbiddenException("You are not allowed to delete this comment.");
        }

        if (comment.getParentComment() == null
                && commentRepository.existsByParentComment_Id(commentId)) {
            comment.setContent("Comment deleted");
            comment.setDeletedAt(LocalDateTime.now());
            Comment savedComment = commentRepository.save(comment);
            return CommentDeleteResponse.builder()
                    .commentId(commentId)
                    .retainedAsTombstone(true)
                    .comment(postMapper.toCommentResponse(savedComment))
                    .build();
        }

        boolean removeDeletedParent = parentComment != null
                && parentComment.getDeletedAt() != null
                && commentRepository.countByParentComment_Id(parentComment.getId()) == 1;

        commentRepository.delete(comment);
        commentRepository.flush();

        List<Integer> removedCommentIds = new ArrayList<>();
        removedCommentIds.add(commentId);
        if (removeDeletedParent) {
            commentRepository.delete(parentComment);
            removedCommentIds.add(parentComment.getId());
        }

        return CommentDeleteResponse.builder()
                .commentId(commentId)
                .retainedAsTombstone(false)
                .removedCommentIds(removedCommentIds)
                .build();
    }
    @Transactional
    @Override
    public void upvotePost(Integer postId) {

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Deleted posts cannot be voted on.");
        }

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

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Deleted posts cannot be voted on.");
        }

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

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Votes cannot be changed on deleted posts.");
        }

        postVoteRepository.deleteByPostAndIdUserId(
                post,
                SecurityUtils.getCurrentUserId()
        );
    }
}
