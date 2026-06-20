package com.example.campusconnect.feature.posts.data.repo

import java.io.File

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.dummyComments
import com.example.campusconnect.feature.posts.models.dummyPosts
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.dummyTags

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

    override suspend fun getTags(): Result<List<PostTag>> {

        return Result.success(dummyTags)
    }


    // Posts -------------------------------------------------------------

    override suspend fun createPost(

        title: String,

        body : String,

        tags: List<PostTag>,

        image: File?
    ): Result<Post> {

        TODO("Will implement after Create Post UI")
    }

    override suspend fun updatePost(

        postId: Int,

        title: String,

        body : String,

        tags: List<PostTag>
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

        body : String
    ): Result<Comment> {

        TODO()
    }

    override suspend fun createReply(

        postId: Int,

        parentCommentId: Int,

        body : String
    ): Result<Comment> {

        TODO()
    }

    override suspend fun updateComment(

        commentId: Int,

        body : String
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