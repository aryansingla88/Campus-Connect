package com.example.campusconnect.model

data class Comment(
    val id: Int,
    val postId: Int,
    val userId: Int,
    val content: String,
    val createdAt: String
)