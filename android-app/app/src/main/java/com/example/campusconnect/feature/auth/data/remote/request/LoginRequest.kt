package com.example.campusconnect.feature.auth.data.remote.request

data class LoginRequest(
    val identifier: String,
    val password: String
)