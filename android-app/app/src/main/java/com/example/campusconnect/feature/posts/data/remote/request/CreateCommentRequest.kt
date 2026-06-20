package com.example.campusconnect.feature.posts.data.remote.request

data class CreateCommentRequest(

    val body: String,

    val parentCommentId: Int?
)