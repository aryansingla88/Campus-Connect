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
import com.campus.Campus_Connect.features.post.dto.response.EventPostResponse;
import com.campus.Campus_Connect.features.post.entity.*;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventCategory;
import com.campus.Campus_Connect.features.event.repository.EventCategoryRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
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
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;

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

    private final EventCategoryRepository eventCategoryRepository;

    private final ClubRepository clubRepository;

    private final PostMapper postMapper;

    @PersistenceContext
    private EntityManager entityManager;


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

    @Transactional(readOnly = true)
    @Override
    public PostResponse getPostById(Integer postId) {

        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );
        PostResponse response = postMapper.toPostResponse(
                post,
                SecurityUtils.getCurrentUserId(),
                commentRepository.countByPost(post)
        );
        if ("EVENTS".equalsIgnoreCase(post.getPostType()) && post.getEventId() != null) {
            Event event = entityManager.find(Event.class, post.getEventId());
            if (event != null) populateEventDetails(response, event);
        }
        return response;
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

    @Transactional(readOnly = true)
    @Override
    public List<EventPostResponse> getEventPosts() {
        List<Post> eventPosts = postRepository
                .findAllByPostTypeAndEventIdIsNotNullAndDeletedAtIsNullOrderByCreatedAtDesc("EVENTS");
        if (eventPosts.isEmpty()) return List.of();

        List<Integer> eventIds = eventPosts.stream().map(Post::getEventId).distinct().toList();
        List<Event> events = entityManager.createQuery(
                        "select distinct e from Event e left join fetch e.categories left join fetch e.creator where e.id in :ids",
                        Event.class)
                .setParameter("ids", eventIds)
                .getResultList();
        Map<Integer, Event> eventsById = events.stream().collect(Collectors.toMap(Event::getId, event -> event));

        List<Integer> postIds = eventPosts.stream().map(Post::getId).toList();
        Map<Integer, String> postImages = entityManager.createQuery(
                        "select pi from PostImage pi where pi.post.id in :ids order by pi.imageOrder",
                        PostImage.class)
                .setParameter("ids", postIds)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(
                        image -> image.getPost().getId(),
                        PostImage::getImageUrl,
                        (first, ignored) -> first
                ));

        List<Integer> clubIds = events.stream()
                .map(Event::getClubId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        Map<Integer, String> clubNames = clubRepository.findAllById(clubIds).stream()
                .collect(Collectors.toMap(com.campus.Campus_Connect.features.club.entity.Club::getId,
                        com.campus.Campus_Connect.features.club.entity.Club::getName));

        Map<Integer, Long> commentCounts = commentRepository.countByPostIds(postIds).stream()
                .collect(Collectors.toMap(
                        CommentRepository.CommentCountProjection::getPostId,
                        CommentRepository.CommentCountProjection::getCommentCount));
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        Map<Integer, int[]> voteCounts = new java.util.HashMap<>();
        for (PostVoteRepository.VoteCountProjection voteCount : postVoteRepository.countVotesByPostIds(postIds)) {
            int[] counts = voteCounts.computeIfAbsent(voteCount.getPostId(), ignored -> new int[2]);
            if (voteCount.getVoteType() == VoteType.UPVOTE) {
                counts[0] = Math.toIntExact(voteCount.getVoteCount());
            } else {
                counts[1] = Math.toIntExact(voteCount.getVoteCount());
            }
        }
        Map<Integer, VoteType> userVotes = currentUserId == null
                ? Map.of()
                : postVoteRepository.findUserVotesForPosts(postIds, currentUserId)
                        .stream()
                        .collect(Collectors.toMap(vote -> vote.getId().getPostId(), PostVote::getVoteType));

        return eventPosts.stream()
                .map(post -> {
                    Event event = eventsById.get(post.getEventId());
                    if (event == null) return null;
                    int[] counts = voteCounts.getOrDefault(post.getId(), new int[2]);
                    return EventPostResponse.builder()
                            .id(event.getId())
                            .postId(post.getId())
                            .title(event.getTitle())
                            .hostName(event.getHostName())
                            .organizerName(event.getClubId() != null
                                    ? clubNames.getOrDefault(event.getClubId(), event.getCreator().getUsername())
                                    : event.getCreator().getUsername())
                            .categoryId(event.getCategories().stream().findFirst().map(EventCategory::getId).orElse(null))
                            .categoryName(event.getCategories().stream().findFirst().map(category -> category.getName()).orElse(null))
                            .description(post.getContentRaw())
                            .posterUrl(postImages.get(post.getId()))
                            .startTime(event.getStartTime())
                            .venue(event.getVenue())
                            .latitude(event.getLatitude())
                            .longitude(event.getLongitude())
                            .commentCount(commentCounts.getOrDefault(post.getId(), 0L))
                            .isEdited(post.getUpdatedAt() != null && post.getCreatedAt() != null
                                    && post.getUpdatedAt().isAfter(post.getCreatedAt().plusSeconds(1)))
                            .upvotes(counts[0])
                            .downvotes(counts[1])
                            .userVote(userVotes.get(post.getId()))
                            .canDelete(currentUserId != null
                                    && currentUserId.equals(event.getCreator().getId()))
                            .build();
                })
                .filter(java.util.Objects::nonNull)
                .toList();
    }
    @Transactional
    @Override
    public PostResponse createPost(CreatePostRequest request) {

        Post post = new Post();

        Event selectedEvent = null;
        boolean eventPost = "EVENTS".equalsIgnoreCase(request.getPostType());
        if (eventPost) {
            if (request.getEventId() == null) {
                throw new BadRequestException("Select an event to publish.");
            }
            selectedEvent = entityManager.find(Event.class, request.getEventId(), LockModeType.PESSIMISTIC_WRITE);
            if (selectedEvent == null) {
                throw new ResourceNotFoundException("Event not found.");
            }
            if (!selectedEvent.getCreator().getId().equals(SecurityUtils.getCurrentUserId())) {
                throw new ForbiddenException("You can only publish an event post for an event you created.");
            }
            if (postRepository.existsByEventIdAndPostTypeAndDeletedAtIsNull(selectedEvent.getId(), "EVENTS")) {
                throw new BadRequestException("This event already has a post.");
            }
            if (request.getBody() == null || request.getBody().isBlank()) {
                throw new BadRequestException("Event post description is required.");
            }
            if (request.getEventCategoryId() == null) {
                throw new BadRequestException("Select an event category.");
            }
            EventCategory category = eventCategoryRepository.findById(request.getEventCategoryId())
                    .orElseThrow(() -> new BadRequestException("The selected event category is invalid."));
            selectedEvent.getCategories().clear();
            selectedEvent.getCategories().add(category);
        } else if (request.getEventId() != null) {
            throw new BadRequestException("An event can only be attached to an EVENTS post.");
        } else if (request.getBody() == null || request.getBody().isBlank()) {
            throw new BadRequestException("Post body is required.");
        }

        // Basic fields
        post.setTitle(eventPost ? selectedEvent.getTitle() : request.getTitle());
        post.setContentRaw(request.getBody().trim());
        post.setPostType(eventPost ? "EVENTS" : request.getPostType());
        post.setEventId(eventPost ? selectedEvent.getId() : null);

        // Defaults
        post.setVisibilityType("PUBLIC");
        post.setVisibilityValue(null);
        post.setAllowComments(true);

        // Logged-in user
        post.setCreator(SecurityUtils.getCurrentUser());

        // Tags
        if (!eventPost && (request.getTags() == null || request.getTags().isEmpty())) {
            throw new BadRequestException("At least one tag must be selected.");
        }
        List<PostTag> tags = eventPost ? List.of() : postTagRepository.findAllById(request.getTags());

        if (!eventPost && tags.size() != request.getTags().size()) {
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
        } else if (eventPost && selectedEvent != null && !selectedEvent.getPosters().isEmpty()) {
            String imagePath = mediaStorageService.copy(
                    selectedEvent.getPosters().get(0).getPosterUrl(),
                    MediaType.POST_IMAGE
            );
            savedPost.getImages().add(PostImage.builder()
                    .post(savedPost)
                    .imageUrl(imagePath)
                    .imageOrder(1)
                    .createdAt(LocalDateTime.now())
                    .build());
            savedPost = postRepository.save(savedPost);
        }

        return postMapper.toPostResponse(
                savedPost,
                SecurityUtils.getCurrentUserId(),
                commentRepository.countByPost(savedPost)
        );
    }

    private void populateEventDetails(PostResponse response, Event event) {
        EventCategory category = event.getCategories().stream().findFirst().orElse(null);
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        response.setCanDelete(currentUserId != null && currentUserId.equals(event.getCreator().getId()));
        response.setEventCategoryId(category != null ? category.getId() : null);
        response.setEventCategoryName(category != null ? category.getName() : null);
        response.setEventVenue(event.getVenue());
        response.setEventLatitude(event.getLatitude());
        response.setEventLongitude(event.getLongitude());
        response.setEventStartTime(event.getStartTime());
        if (event.getClubId() != null) {
            response.setEventOrganizerName(clubRepository.findById(event.getClubId())
                    .map(com.campus.Campus_Connect.features.club.entity.Club::getName)
                    .orElse(event.getCreator().getUsername()));
        } else {
            response.setEventOrganizerName(event.getCreator().getUsername());
        }
    }
    @Transactional
    @Override
    public PostResponse updatePost(Integer postId, UpdatePostRequest request, org.springframework.web.multipart.MultipartFile image) {

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        if (post.getDeletedAt() != null) {
            throw new BadRequestException("Deleted posts cannot be edited.");
        }

        if (request == null || request.getBody() == null || request.getBody().isBlank()) {
            throw new BadRequestException("Post description is required.");
        }

        boolean eventPost = "EVENTS".equalsIgnoreCase(post.getPostType());
        Event linkedEvent = null;
        Integer currentUserId = SecurityUtils.getCurrentUserId();
        if (eventPost) {
            if (post.getEventId() == null) {
                throw new BadRequestException("This event post is not linked to an event.");
            }
            linkedEvent = entityManager.find(Event.class, post.getEventId(), LockModeType.PESSIMISTIC_WRITE);
            if (linkedEvent == null) {
                throw new PostNotFoundException("Event not found for post id: " + postId);
            }
            if (!linkedEvent.getCreator().getId().equals(currentUserId)) {
                throw new ForbiddenException("Only the event creator can edit this event post.");
            }
            if (request.getEventCategoryId() == null) {
                throw new BadRequestException("Select an event category.");
            }
            EventCategory category = eventCategoryRepository.findById(request.getEventCategoryId())
                    .orElseThrow(() -> new BadRequestException("The selected event category is invalid."));
            linkedEvent.getCategories().clear();
            linkedEvent.getCategories().add(category);
        } else {
            if (!post.getCreator().getId().equals(currentUserId)) {
                throw new ForbiddenException("You are not allowed to edit this post.");
            }
            if (request.getTags() == null || request.getTags().isEmpty()) {
                throw new BadRequestException("At least one tag must be selected.");
            }
            List<PostTag> tags = postTagRepository.findAllById(request.getTags());
            if (tags.size() != request.getTags().size()) {
                throw new BadRequestException("One or more tags are invalid.");
            }
            post.setTags(new HashSet<>(tags));
        }

        if (request.isRemoveImage() && image != null && !image.isEmpty()) {
            throw new BadRequestException("Choose either a replacement image or image removal.");
        }
        if (request.isRemoveImage() && !eventPost) {
            throw new BadRequestException("General post images cannot be replaced or removed.");
        }

        // The title, event link, author, and creation time remain unchanged.
        post.setContentRaw(request.getBody().trim());

        if (eventPost && (request.isRemoveImage() || (image != null && !image.isEmpty()))) {
            String newImagePath = null;
            if (image != null && !image.isEmpty()) {
                String contentType = image.getContentType();
                if (contentType == null || !contentType.startsWith("image/")) {
                    throw new BadRequestException("Only image files are allowed.");
                }
                newImagePath = mediaStorageService.store(image, MediaType.POST_IMAGE);
            }

            List<String> oldImagePaths = post.getImages().stream().map(PostImage::getImageUrl).toList();
            post.getImages().clear();
            if (newImagePath != null) {
                post.getImages().add(PostImage.builder()
                        .post(post)
                        .imageUrl(newImagePath)
                        .imageOrder(1)
                        .createdAt(LocalDateTime.now())
                        .build());
            }
            registerPostImageReplacementCleanup(oldImagePaths, newImagePath);
        } else if (!eventPost && image != null && !image.isEmpty()) {
            if (!post.getImages().isEmpty()) {
                throw new BadRequestException("A post image cannot be replaced once added.");
            }
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BadRequestException("Only image files are allowed.");
            }
            String imagePath = mediaStorageService.store(image, MediaType.POST_IMAGE);
            post.getImages().add(PostImage.builder()
                    .post(post)
                    .imageUrl(imagePath)
                    .imageOrder(1)
                    .createdAt(LocalDateTime.now())
                    .build());
        }
        post.setUpdatedAt(LocalDateTime.now());

        Post updatedPost = postRepository.save(post);

        PostResponse response = postMapper.toPostResponse(
                updatedPost,
                currentUserId,
                commentRepository.countByPost(updatedPost)
        );
        if (eventPost && linkedEvent != null) populateEventDetails(response, linkedEvent);
        return response;
    }

    private void registerPostImageReplacementCleanup(List<String> oldImagePaths, String newImagePath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_COMMITTED) {
                    oldImagePaths.forEach(mediaStorageService::delete);
                } else if (newImagePath != null) {
                    mediaStorageService.delete(newImagePath);
                }
            }
        });
    }
    @Transactional
    @Override
    public void deletePost(Integer postId) {

        Post post = postRepository.findByIdForUpdate(postId)
                .orElseThrow(() ->
                        new PostNotFoundException("Post not found with id: " + postId)
                );

        Integer currentUserId = SecurityUtils.getCurrentUserId();
        boolean isEventPost = "EVENTS".equalsIgnoreCase(post.getPostType())
                && post.getEventId() != null;
        boolean isOwner;
        if (isEventPost) {
            Event event = entityManager.find(Event.class, post.getEventId());
            if (event == null) {
                throw new PostNotFoundException("Event not found for post id: " + postId);
            }
            isOwner = currentUserId != null && currentUserId.equals(event.getCreator().getId());
        } else {
            isOwner = currentUserId != null && currentUserId.equals(post.getCreator().getId());
        }

        if (!isOwner) {
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
