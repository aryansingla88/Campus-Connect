package com.example.campus_connect.api;

import com.example.campus_connect.model.Post;
import com.example.campus_connect.model.CreatePostRequest;
import com.example.campus_connect.model.CommentRequest;
import com.example.campus_connect.model.UpvoteRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface PostService {

    @GET("api/posts")
    Call<List<Post>> getPosts(@Query("user_id") int userId);

    @POST("api/posts")
    Call<Void> createPost(@Body CreatePostRequest request);

    @POST("api/comments")
    Call<Void> createComment(@Body CommentRequest request);

    @POST("api/upvote")
    Call<Void> upvotePost(@Body UpvoteRequest request);

}