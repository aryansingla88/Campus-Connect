package com.example.campusconnect.feature.posts.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.posts.data.repo.ApiPostsRepository

import com.example.campusconnect.feature.posts.data.repo.FakePostsRepository
import com.example.campusconnect.feature.posts.data.repo.PostsRepository
import com.example.campusconnect.feature.posts.models.Post
import com.example.campusconnect.feature.posts.models.PostTag
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
}