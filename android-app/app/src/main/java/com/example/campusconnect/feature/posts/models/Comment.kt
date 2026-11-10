package com.example.campusconnect.feature.posts.models

data class Comment(

    val id: Int,

    val postId: Int,

    val parentCommentId: Int?,

    val username: String,

    val body: String,

    val createdAt: String,

    val replies: List<Comment> = emptyList(),

    val isDeleted: Boolean = false,

    val canDelete: Boolean = false,

    val canEdit: Boolean = false,

    val authorId: Int? = null,

    val authorDisplayName: String? = null,

    val authorAvatarUrl: String? = null
)
