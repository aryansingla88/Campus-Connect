package com.example.campusconnect.feature.posts.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Response
import retrofit2.http.*

interface PostsApi {

    // Feed -------------------------------------------------------------

    @GET("posts")
    suspend fun getPosts()

    @GET("posts/{postId}")
    suspend fun getPost(

        @Path("postId")
        postId: Int
    )


    // Posts -------------------------------------------------------------

    @Multipart
    @POST("posts")
    suspend fun createPost(

        @Part("title")
        title: RequestBody,

        @Part("content")
        content: RequestBody,

        @Part
        image: MultipartBody.Part?,

        @Part("tags")
        tags: List<RequestBody>
    )

    @PUT("posts/{postId}")
    suspend fun updatePost(

        @Path("postId")
        postId: Int
    )

    @DELETE("posts/{postId}")
    suspend fun deletePost(

        @Path("postId")
        postId: Int
    )


    // Comments -------------------------------------------------------------

    @GET("posts/{postId}/comments")
    suspend fun getComments(

        @Path("postId")
        postId: Int
    )

    @POST("comments")
    suspend fun createComment()

    @PUT("comments/{commentId}")
    suspend fun updateComment(

        @Path("commentId")
        commentId: Int
    )

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(

        @Path("commentId")
        commentId: Int
    )


    // Voting -------------------------------------------------------------

    @POST("posts/{postId}/upvote")
    suspend fun upvotePost(

        @Path("postId")
        postId: Int
    )

    @POST("posts/{postId}/downvote")
    suspend fun downvotePost(

        @Path("postId")
        postId: Int
    )

    @DELETE("posts/{postId}/vote")
    suspend fun removePostVote(

        @Path("postId")
        postId: Int
    )
}