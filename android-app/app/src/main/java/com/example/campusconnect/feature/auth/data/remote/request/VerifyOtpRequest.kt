package com.example.campusconnect.feature.auth.data.remote.request

data class VerifyOtpRequest(

    val email: String,

    val otp: String
)