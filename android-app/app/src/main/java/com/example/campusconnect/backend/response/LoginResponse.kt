package com.example.campusconnect.backend.response

data class LoginResponse(
    val success: Boolean,
    val message: String,
    val user_id: Int
)