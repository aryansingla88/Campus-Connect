package com.example.campusconnect.feature.auth.data.remote.request

data class ResetPasswordRequest(
    val resetToken: String,
    val newPassword: String
)