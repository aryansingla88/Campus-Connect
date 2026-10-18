package com.example.campusconnect.feature.posts.models

data class CommentDeleteResponse(
    val commentId: Int,
    val retainedAsTombstone: Boolean,
    val comment: Comment? = null,
    val removedCommentIds: List<Int> = emptyList()
)
