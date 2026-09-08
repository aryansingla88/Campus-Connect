package com.example.campusconnect.feature.posts.models

data class Comment(

    val id: Int,

    val postId: Int,

    val parentCommentId: Int?,

    val username: String,

    val body: String,

    val createdAt: String,

    val replies: List<Comment> = emptyList()
)