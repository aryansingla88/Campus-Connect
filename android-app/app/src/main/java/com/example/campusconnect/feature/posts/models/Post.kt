package com.example.campusconnect.feature.posts.models

data class Post(

    val id: Int,

    val username: String,

    val title: String,

    val body: String,

    val tag: String,

    val imageUrl: String? = null,

    val upvotes: Int,

    val downvotes: Int,

    val commentCount: Int,

    val createdAt: String
)