package com.example.campusconnect.feature.posts.data.repo

import java.io.File

import com.example.campusconnect.core.network.RetrofitClient

import com.example.campusconnect.feature.posts.data.remote.PostsApi
import com.example.campusconnect.feature.posts.data.remote.request.CreateCommentRequest
import com.example.campusconnect.feature.posts.data.remote.request.UpdateCommentRequest
import com.example.campusconnect.feature.posts.data.remote.request.UpdatePostRequest

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class ApiPostsRepository(

    private val api: PostsApi = RetrofitClient.postsApi

) : PostsRepository {

    // Feed -------------------------------------------------------------

    override suspend fun getPosts(): Result<List<Post>> {

        return Result.success(

            listOf(

                Post(
                    id = 999,

                    username = "api_test",

                    title = "ApiPostsRepository Test",

                    body = "This post came from ApiPostsRepository",

                    tags = listOf(
                        PostTag(
                            id = 1,
                            name = "Study Help"
                        )
                    ),

                    imageUrl = null,

                    upvotes = 10,

                    downvotes = 0,

                    userVote = null,

                    createdAt = "1m"
                )
            )
        )
    }

    override suspend fun getTags(): Result<List<PostTag>> {

        return try {

            val response = api.getTags()

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(

                    response.body()!!.data!!
                )

            } else {

                Result.failure(

                    Exception(

                        response.body()?.message
                            ?: "Failed to fetch tags"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun getPost(
        postId: Int
    ): Result<Post> {

        return try {

            val response = api.getPost(postId)

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(

                    response.body()!!.data!!
                )

            } else {

                Result.failure(

                    Exception(

                        response.body()?.message
                            ?: "Failed to fetch post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // Posts -------------------------------------------------------------

    override suspend fun createPost(

        title: String,

        body: String,

        tags: List<PostTag>,

        image: File?

    ): Result<Post> {

        return try {

            val titleBody = title.toRequestBody(

                "text/plain".toMediaType()
            )

            val bodyBody = body.toRequestBody(

                "text/plain".toMediaType()
            )

            val tagBodies = tags.map {

                it.id.toString().toRequestBody(

                    "text/plain".toMediaType()
                )
            }

            val imagePart = image?.let {

                MultipartBody.Part.createFormData(

                    name = "image",

                    filename = it.name,

                    body = it.asRequestBody(
                        "image/*".toMediaType()
                    )
                )
            }

            val response = api.createPost(

                title = titleBody,

                body = bodyBody,

                image = imagePart,

                tags = tagBodies
            )

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(

                    response.body()!!.data!!
                )

            } else {

                Result.failure(

                    Exception(

                        response.body()?.message
                            ?: "Failed to create post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun updatePost(

        postId: Int,

        title: String,

        body: String,

        tags: List<PostTag>

    ): Result<Post> {

        return try {

            val response = api.updatePost(

                postId = postId,

                request = UpdatePostRequest(

                    title = title,

                    body = body,

                    tags = tags.map { it.name }
                )
            )

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(
                    response.body()!!.data!!
                )

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to update post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun deletePost(
        postId: Int
    ): Result<Unit> {

        return try {

            val response = api.deletePost(postId)

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to delete post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // Comments -------------------------------------------------------------

    override suspend fun getComments(
        postId: Int
    ): Result<List<Comment>> {

        return try {

            val response = api.getComments(postId)

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(
                    response.body()!!.data!!
                )

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to fetch comments"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun createComment(

        postId: Int,

        body: String

    ): Result<Comment> {

        return try {

            val response = api.createComment(

                postId = postId,

                request = CreateCommentRequest(

                    body = body,

                    parentCommentId = null
                )
            )

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(

                    response.body()!!.data!!
                )

            } else {

                Result.failure(

                    Exception(

                        response.body()?.message
                            ?: "Failed to create comment"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun createReply(

        postId: Int,

        parentCommentId: Int,

        body: String

    ): Result<Comment> {

        return try {

            val response = api.createComment(

                postId = postId,

                request = CreateCommentRequest(

                    body = body,

                    parentCommentId = parentCommentId
                )
            )

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(

                    response.body()!!.data!!
                )

            } else {

                Result.failure(

                    Exception(

                        response.body()?.message
                            ?: "Failed to create reply"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun updateComment(

        commentId: Int,

        body: String

    ): Result<Comment> {

        return try {

            val response = api.updateComment(

                commentId = commentId,

                request = UpdateCommentRequest(
                    body = body
                )
            )

            if (
                response.isSuccessful &&
                response.body()?.data != null
            ) {

                Result.success(
                    response.body()!!.data!!
                )

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to update comment"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun deleteComment(
        commentId: Int
    ): Result<Unit> {

        return try {

            val response = api.deleteComment(commentId)

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to delete comment"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }


    // Voting -------------------------------------------------------------

    override suspend fun upvotePost(
        postId: Int
    ): Result<Unit> {

        return try {

            val response = api.upvotePost(postId)

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to upvote post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun downvotePost(
        postId: Int
    ): Result<Unit> {

        return try {

            val response = api.downvotePost(postId)

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to downvote post"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    override suspend fun removePostVote(
        postId: Int
    ): Result<Unit> {

        return try {

            val response = api.removePostVote(postId)

            if (response.isSuccessful) {

                Result.success(Unit)

            } else {

                Result.failure(
                    Exception(
                        response.body()?.message
                            ?: "Failed to remove vote"
                    )
                )
            }

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}