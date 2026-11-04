package com.example.campusconnect.feature.auth.data.remote.request

data class ChangePasswordRequest(
    val currentPassword: String,
    val newPassword: String
)
