package com.campus.Campus_Connect.features.post.service;

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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostService {

    List<PostResponse> getAllPosts();

    PostResponse getPostById(Integer postId);

    List<PostTagResponse> getAllTags();

    List<PostTagCountResponse> getGeneralPostTagCounts();

    List<EventPostResponse> getEventPosts();

    PostResponse createPost(CreatePostRequest request);

    PostResponse updatePost(Integer postId, UpdatePostRequest request, MultipartFile image);

    void deletePost(Integer postId);

    List<CommentResponse> getComments(Integer postId);

    CommentResponse createComment(Integer postId, CreateCommentRequest request);

    CommentResponse updateComment(Integer commentId, UpdateCommentRequest request);

    CommentDeleteResponse deleteComment(Integer commentId);

    void upvotePost(Integer postId);

    void downvotePost(Integer postId);

    void removeVote(Integer postId);
}
