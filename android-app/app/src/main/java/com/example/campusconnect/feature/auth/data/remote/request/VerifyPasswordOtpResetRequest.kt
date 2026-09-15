package com.example.campusconnect.feature.auth.data.remote.request

data class VerifyPasswordResetOtpRequest(
    val email: String,
    val otp: String
)