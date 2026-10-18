package com.example.campusconnect.feature.posts.data.remote

class PostApiException(
    val statusCode: Int,
    override val message: String
) : Exception(message)
