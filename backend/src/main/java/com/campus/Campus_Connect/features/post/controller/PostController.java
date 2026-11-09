package com.campus.Campus_Connect.features.post.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
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
import com.campus.Campus_Connect.features.post.service.PostService;
import com.campus.Campus_Connect.common.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> createPost(

            @RequestParam("title")
            String title,

            @RequestParam("body")
            String body,

            @RequestParam("postType")
            String postType,

            @RequestParam(value = "tags", required = false)
            List<Integer> tags,

            @RequestParam(value = "image", required = false)
            MultipartFile image,

            @RequestParam(value = "eventId", required = false)
            Integer eventId,

            @RequestParam(value = "eventCategoryId", required = false)
            Integer eventCategoryId
    ) {

        if ((postType == null || !postType.equalsIgnoreCase("EVENTS"))
                && (tags == null || tags.isEmpty())) {
            throw new BadRequestException("At least one tag must be selected.");
        }

        CreatePostRequest request = CreatePostRequest.builder()
                .title(title)
                .body(body)
                .postType(postType)
                .tags(tags)
                .image(image)
                .eventId(eventId)
                .eventCategoryId(eventCategoryId)
                .build();

        PostResponse response = postService.createPost(request);

        return ApiResponse.success(
                response,
                "Post created successfully."
        );
    }

    @GetMapping("/events")
    public ApiResponse<List<EventPostResponse>> getEventPosts() {
        return ApiResponse.success(postService.getEventPosts(), null);
    }
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @PathVariable Integer postId
    ) {

        postService.deletePost(postId);

        return ApiResponse.success(
                null,
                "Post deleted successfully."
        );
    }

    @GetMapping
    public ApiResponse<List<PostResponse>> getAllPosts() {

        List<PostResponse> posts = postService.getAllPosts();

        return ApiResponse.success(
                posts,
                null
        );
    }

    @GetMapping("/{postId}")
    public ApiResponse<PostResponse> getPostById(
            @PathVariable Integer postId
    ) {

        PostResponse post = postService.getPostById(postId);

        return ApiResponse.success(
                post,
                null
        );
    }

    @PutMapping(value = "/{postId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PostResponse> updatePost(
            @PathVariable Integer postId,
            @RequestParam("body") String body,
            @RequestParam(value = "tags", required = false) List<Integer> tags,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "eventCategoryId", required = false) Integer eventCategoryId,
            @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage
    ) {

        PostResponse response = postService.updatePost(
                postId,
                UpdatePostRequest.builder()
                        .body(body)
                        .tags(tags)
                        .eventCategoryId(eventCategoryId)
                        .removeImage(removeImage)
                        .build(),
                image
        );

        return ApiResponse.success(
                response,
                "Post updated successfully."
        );
    }

    @PostMapping("/{postId}/comments")
    public ApiResponse<CommentResponse> createComment(
            @PathVariable Integer postId,
            @RequestBody CreateCommentRequest request
    ) {

        CommentResponse response =
                postService.createComment(postId, request);

        return ApiResponse.success(
                response,
                "Comment created successfully."
        );
    }
    @PutMapping("/comments/{commentId}")
    public ApiResponse<CommentResponse> updateComment(
            @PathVariable Integer commentId,
            @RequestBody UpdateCommentRequest request
    ) {

        CommentResponse response =
                postService.updateComment(commentId, request);

        return ApiResponse.success(
                response,
                "Comment updated successfully."
        );
    }

    @DeleteMapping("/comments/{commentId}")
    public ApiResponse<CommentDeleteResponse> deleteComment(
            @PathVariable Integer commentId
    ) {

        CommentDeleteResponse response = postService.deleteComment(commentId);

        return ApiResponse.success(
                response,
                "Comment deleted successfully."
        );
    }

    @GetMapping("/{postId}/comments")
    public ApiResponse<List<CommentResponse>> getComments(
            @PathVariable Integer postId
    ) {

        List<CommentResponse> comments =
                postService.getComments(postId);

        return ApiResponse.success(
                comments,
                null
        );
    }

    @GetMapping("/tags")
    public ApiResponse<List<PostTagResponse>> getAllTags() {

        List<PostTagResponse> tags = postService.getAllTags();

        return ApiResponse.success(
                tags,
                null
        );
    }

    @GetMapping("/tags/counts")
    public ApiResponse<List<PostTagCountResponse>> getGeneralPostTagCounts() {

        List<PostTagCountResponse> counts = postService.getGeneralPostTagCounts();

        return ApiResponse.success(
                counts,
                null
        );
    }

    @PostMapping("/{postId}/upvote")
    public ApiResponse<Void> upvotePost(
            @PathVariable Integer postId
    ) {

        postService.upvotePost(postId);

        return ApiResponse.success(
                null,
                "Post upvoted successfully."
        );
    }

    @PostMapping("/{postId}/downvote")
    public ApiResponse<Void> downvotePost(
            @PathVariable Integer postId
    ) {

        postService.downvotePost(postId);

        return ApiResponse.success(
                null,
                "Post downvoted successfully."
        );
    }

    @DeleteMapping("/{postId}/vote")
    public ApiResponse<Void> removeVote(
            @PathVariable Integer postId
    ) {

        postService.removeVote(postId);

        return ApiResponse.success(
                null,
                "Vote removed successfully."
        );
    }
}
