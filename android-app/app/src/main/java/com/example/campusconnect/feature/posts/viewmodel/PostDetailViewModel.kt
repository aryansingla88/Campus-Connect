package com.example.campusconnect.feature.posts.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.posts.data.repo.ApiPostsRepository

import com.example.campusconnect.feature.posts.data.repo.PostsRepository

import com.example.campusconnect.feature.posts.models.Comment
import com.example.campusconnect.feature.posts.models.CommentDeleteResponse
import com.example.campusconnect.feature.posts.models.Post


/*
This kotlin code-
class PostDetailViewModel(

    private val repository: PostsRepository = FakePostsRepository()

) : ViewModel()


is roughly equivalent to this Java code:

public class FeedViewModel extends ViewModel {

    private PostsRepository repository;

    public FeedViewModel() {
        this.repository = new FakePostsRepository();
    }
}

 */

class PostDetailViewModel(

    private val repository: PostsRepository = ApiPostsRepository()

) : ViewModel() {

    suspend fun getPost(
        postId: Int
    ): Result<Post> {

        return repository.getPost(postId)
    }

    suspend fun getComments(
        postId: Int
    ): Result<List<Comment>> {

        return repository.getComments(postId)
    }

    suspend fun createComment(
        postId: Int,
        body: String
    ): Result<Comment> {
        return repository.createComment(postId, body)
    }

    suspend fun createReply(
        postId: Int,
        parentCommentId: Int,
        body: String
    ): Result<Comment> {
        return repository.createReply(postId, parentCommentId, body)
    }

    suspend fun updateComment(commentId: Int, body: String): Result<Comment> =
        repository.updateComment(commentId, body)

    suspend fun deleteComment(commentId: Int): Result<CommentDeleteResponse> =
        repository.deleteComment(commentId)

    suspend fun deletePost(postId: Int): Result<Unit> = repository.deletePost(postId)

    suspend fun upvotePost(postId: Int): Result<Unit> = repository.upvotePost(postId)

    suspend fun downvotePost(postId: Int): Result<Unit> = repository.downvotePost(postId)

    suspend fun removePostVote(postId: Int): Result<Unit> = repository.removePostVote(postId)
}
