package com.example.campusconnect.feature.auth.data.remote.response

data class CurrentUserResponse(
    val id: Int,
    val username: String,
    val email: String,
    val role: String
)