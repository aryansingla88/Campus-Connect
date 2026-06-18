package com.example.campusconnect.feature.posts.models
import com.example.campusconnect.feature.posts.models.VoteType

data class Post(

    val id: Int,

    val username: String,

    val title: String,

    val body: String,

    val tags: List<PostTag>,

    val imageUrl: String? = null,

    val upvotes: Int,

    val downvotes: Int,

    val userVote: VoteType? = null,

    val createdAt: String
)