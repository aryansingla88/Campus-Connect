package com.example.campusconnect.network;

import com.example.campusconnect.model.CommentRequest;
import com.example.campusconnect.model.CreatePostRequest;
import com.example.campusconnect.model.Post;
import com.example.campusconnect.model.UpvoteRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostService {

    @GET("api/posts")
    Call<List<Post>> getPosts(@Query("userId") int userId);

    @POST("api/posts")
    Call<Void> createPost(@Body CreatePostRequest request);

    @POST("api/comments")
    Call<Void> createComment(@Body CommentRequest request);

    @POST("api/upvote")
    Call<Void> upvotePost(@Body UpvoteRequest request);

}