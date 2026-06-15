package com.example.campusconnect.feature.posts.data.repo

import java.io.File

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.dummyComments
import com.example.campusconnect.feature.posts.models.dummyPosts

class FakePostsRepository : PostsRepository {

    // Feed -------------------------------------------------------------

    override suspend fun getPosts(): Result<List<Post>> {

        return Result.success(dummyPosts)
    }

    override suspend fun getPost(
        postId: Int
    ): Result<Post> {

        return Result.success(

            dummyPosts.first {

                it.id == postId
            }
        )
    }


    // Posts -------------------------------------------------------------

    override suspend fun createPost(

        title: String,

        content: String,

        tags: List<String>,

        image: File?
    ): Result<Post> {

        TODO("Will implement after Create Post UI")
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

        return Result.success(

            dummyComments.filter {

                it.postId == postId
            }
        )
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