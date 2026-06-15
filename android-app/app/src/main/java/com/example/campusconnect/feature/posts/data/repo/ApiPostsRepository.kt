package com.example.campusconnect.feature.posts.data.repo

import java.io.File

import com.example.campusconnect.core.network.RetrofitClient

import com.example.campusconnect.feature.posts.data.remote.PostsApi

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post

class ApiPostsRepository(

    private val api: PostsApi = RetrofitClient.postsApi

) : PostsRepository {

    // Feed -------------------------------------------------------------

    override suspend fun getPosts(): Result<List<Post>> {

        TODO()
    }

    override suspend fun getPost(
        postId: Int
    ): Result<Post> {

        TODO()
    }


    // Posts -------------------------------------------------------------

    override suspend fun createPost(

        title: String,

        content: String,

        tags: List<String>,

        image: File?

    ): Result<Post> {

        TODO()
    }

    override suspend fun updatePost(

        postId: Int,

        title: String,

        content: String,

        tags: List<String>

    ): Result<Post> {

        TODO()
    }

    override suspend fun deletePost(
        postId: Int
    ): Result<Unit> {

        TODO()
    }


    // Comments -------------------------------------------------------------

    override suspend fun getComments(
        postId: Int
    ): Result<List<Comment>> {

        TODO()
    }

    override suspend fun createComment(

        postId: Int,

        content: String

    ): Result<Comment> {

        TODO()
    }

    override suspend fun createReply(

        postId: Int,

        parentCommentId: Int,

        content: String

    ): Result<Comment> {

        TODO()
    }

    override suspend fun updateComment(

        commentId: Int,

        content: String

    ): Result<Comment> {

        TODO()
    }

    override suspend fun deleteComment(
        commentId: Int
    ): Result<Unit> {

        TODO()
    }


    // Voting -------------------------------------------------------------

    override suspend fun upvotePost(
        postId: Int
    ): Result<Unit> {

        TODO()
    }

    override suspend fun downvotePost(
        postId: Int
    ): Result<Unit> {

        TODO()
    }

    override suspend fun removePostVote(
        postId: Int
    ): Result<Unit> {

        TODO()
    }
}