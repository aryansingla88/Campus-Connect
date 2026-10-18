package com.example.campusconnect.feature.posts.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.posts.data.repo.ApiPostsRepository

import com.example.campusconnect.feature.posts.data.repo.PostsRepository
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostTagCount
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

    suspend fun createPost(
        postType: String,
        title: String,
        body: String,
        tags: List<PostTag>,
        image: Uri?
    ): Result<Post> {
        return repository.createPost(postType, title, body, tags, image)

    }

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
