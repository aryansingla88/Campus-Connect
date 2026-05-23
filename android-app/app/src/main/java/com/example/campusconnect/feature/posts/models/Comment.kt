package com.example.campusconnect.feature.posts.models

data class Comment(

    val id: Int,

    val username: String,

    val text: String,

    val createdAt: String,

    val replies: List<Comment> = emptyList()
)