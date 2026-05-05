package com.example.campusconnect.model

data class Post(
    val id: Int,
    val userId: Int,
    val content: String,
    val createdAt: String,
    val likesCount: Int = 0,
    val commentsCount: Int = 0
)