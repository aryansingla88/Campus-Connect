package com.example.campusconnect.feature.posts.models

data class PostEvent(
    val id: Int,
    val title: String,
    val hostName: String? = null,
    val organizerName: String? = null,
    val description: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val startTime: String,
    val venue: String?,
    val posterUrl: String?,
    val categoryName: String?,
    val categoryId: Int? = null,
    val postId: Int? = null,
    val commentCount: Int = 0,
    val isEdited: Boolean = false,
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val userVote: VoteType? = null,
    val canDelete: Boolean = false
)
