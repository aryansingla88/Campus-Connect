//PostApi.kt mei contracts likhe hue hain, contracts of our app with retrofit,
// Each contract tells retrofit that take this content and generate an http request out of it for the backend.

package com.example.campusconnect.feature.posts.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody

import retrofit2.Response
import retrofit2.http.*

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.PostTag

import com.example.campusconnect.feature.posts.data.remote.request.CreateCommentRequest
import com.example.campusconnect.feature.posts.data.remote.request.UpdateCommentRequest
import com.example.campusconnect.feature.posts.data.remote.request.UpdatePostRequest

interface PostsApi {

    // Feed -------------------------------------------------------------

    @GET("posts")
    suspend fun getPosts():
            Response<ApiResponse<List<Post>>>

    @GET("post-tags")
    suspend fun getTags():

            Response<ApiResponse<List<PostTag>>>
    @GET("posts/{postId}")
    suspend fun getPost(

        @Path("postId")
        postId: Int

    ): Response<ApiResponse<Post>>


    // Posts -------------------------------------------------------------

    @Multipart
    @POST("posts")
    suspend fun createPost(

        @Part("title")
        title: RequestBody,

        @Part("body")
        body: RequestBody,

        @Part
        image: MultipartBody.Part?,

        @Part("tags")
        tags: List<RequestBody>
    ) : Response<ApiResponse<Post>>

    @PUT("posts/{postId}")
    suspend fun updatePost(

        @Path("postId")
        postId: Int,

        @Body
        request: UpdatePostRequest

    ): Response<ApiResponse<Post>>
/*
If:

deletePost(5)

then Retrofit generates:

DELETE /posts/5

Retrofit internally converts it into:

DELETE https://myserver.com/posts/5
 */
    @DELETE("posts/{postId}")
    suspend fun deletePost(

        @Path("postId")
        postId: Int

        ): Response<ApiResponse<Unit>>


    // Comments -------------------------------------------------------------

    @GET("posts/{postId}/comments")
    suspend fun getComments(

        @Path("postId")
        postId: Int

    ): Response<ApiResponse<List<Comment>>>

    @POST("posts/{postId}/comments")
    suspend fun createComment(

        @Path("postId")
        postId: Int,

        @Body
        request: CreateCommentRequest

    ): Response<ApiResponse<Comment>>

    @PUT("comments/{commentId}")
    suspend fun updateComment(

        @Path("commentId")
        commentId: Int,

        @Body
        request: UpdateCommentRequest

    ): Response<ApiResponse<Comment>>

    @DELETE("comments/{commentId}")
    suspend fun deleteComment(

        @Path("commentId")
        commentId: Int

    ): Response<ApiResponse<Unit>>


    // Voting -------------------------------------------------------------

    @POST("posts/{postId}/upvote")
    suspend fun upvotePost(

        @Path("postId")
        postId: Int

    ): Response<ApiResponse<Unit>>

    @POST("posts/{postId}/downvote")
    suspend fun downvotePost(

        @Path("postId")
        postId: Int

    ): Response<ApiResponse<Unit>>

    @DELETE("posts/{postId}/vote")
    suspend fun removePostVote(

        @Path("postId")
        postId: Int

    ): Response<ApiResponse<Unit>>
}