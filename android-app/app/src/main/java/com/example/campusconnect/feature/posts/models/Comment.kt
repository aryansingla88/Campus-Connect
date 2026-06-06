package com.example.campusconnect.feature.posts.models

data class Comment(

    val id: Int,

    val postId: Int,

    val parentCommentId: Int?,

    val username: String,

    val text: String,

    val createdAt: String
)