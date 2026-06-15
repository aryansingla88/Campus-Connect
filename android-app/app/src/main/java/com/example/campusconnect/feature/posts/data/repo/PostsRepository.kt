package com.example.campusconnect.feature.posts.data.repo

import java.io.File

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post

interface PostsRepository {

    // Feed -------------------------------------------------------------

    suspend fun getPosts(): Result<List<Post>>

    suspend fun getPost(
        postId: Int
    ): Result<Post>


    // Posts -------------------------------------------------------------

    suspend fun createPost(

        title: String,

        content: String,

        tags: List<String>,

        image: File?
    ): Result<Post>

    suspend fun updatePost(

        postId: Int,

        title: String,

        content: String,

        tags: List<String>
    ): Result<Post>

    suspend fun deletePost(
        postId: Int
    ): Result<Unit>


    // Comments -------------------------------------------------------------

    suspend fun getComments(
        postId: Int
    ): Result<List<Comment>>

    suspend fun createComment(

        postId: Int,

        content: String
    ): Result<Comment>

    suspend fun createReply(

        postId: Int,

        parentCommentId: Int,

        content: String
    ): Result<Comment>

    suspend fun updateComment(

        commentId: Int,

        content: String
    ): Result<Comment>

    suspend fun deleteComment(
        commentId: Int
    ): Result<Unit>


    // Voting -------------------------------------------------------------

    suspend fun upvotePost(
        postId: Int
    ): Result<Unit>

    suspend fun downvotePost(
        postId: Int
    ): Result<Unit>

    suspend fun removePostVote(
        postId: Int
    ): Result<Unit>
}