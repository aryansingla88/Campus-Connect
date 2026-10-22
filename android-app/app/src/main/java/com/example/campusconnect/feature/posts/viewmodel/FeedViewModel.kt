package com.example.campusconnect.feature.posts.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.posts.data.repo.ApiPostsRepository

import com.example.campusconnect.feature.posts.data.repo.PostsRepository
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
import com.example.campusconnect.feature.posts.models.PostEventCategory
import java.io.File
class FeedViewModel(
    private val repository: PostsRepository = ApiPostsRepository()
) : ViewModel() {

    suspend fun getPosts(): Result<List<Post>> {

        return repository.getPosts()
    }

    suspend fun getPost(
        postId: Int
    ): Result<Post> {

        return repository.getPost(postId)
    }

    suspend fun getTags(): Result<List<PostTag>> {

        return repository.getTags()
    }

    suspend fun getGeneralTagCounts(): Result<List<PostTagCount>> {
        return repository.getGeneralTagCounts()
    }

    suspend fun getMyEvents() = repository.getMyEvents()

    suspend fun getEventCategories(): Result<List<PostEventCategory>> = repository.getEventCategories()

    suspend fun getEventPosts() = repository.getEventPosts()

    suspend fun createPost(
        postType: String,
        title: String,
        body: String,
        tags: List<PostTag>,
        image: Uri?,
        eventId: Int? = null,
        eventCategoryId: Int? = null
    ): Result<Post> {
        return repository.createPost(postType, title, body, tags, image, eventId, eventCategoryId)

    }

    suspend fun updatePost(
        postId: Int,
        body: String,
        tags: List<PostTag>,
        image: Uri?,
        eventCategoryId: Int? = null,
        removeImage: Boolean = false
    ): Result<Post> = repository.updatePost(postId, body, tags, image, eventCategoryId, removeImage)

    suspend fun upvotePost(
        postId: Int
    ): Result<Unit> {

        return repository.upvotePost(postId)
    }

    suspend fun downvotePost(
        postId: Int
    ): Result<Unit> {

        return repository.downvotePost(postId)
    }

    suspend fun removePostVote(
        postId: Int
    ): Result<Unit> {

        return repository.removePostVote(postId)
    }

    suspend fun deletePost(postId: Int): Result<Unit> = repository.deletePost(postId)
}
