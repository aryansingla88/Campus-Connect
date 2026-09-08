package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ConnectionResponse(
    val userId: Int,
    val username: String,
    val fullName: String,
    val avatarUrl: String?,
    val courseId: Int?,
    val admissionYear: Int?,
    val status: String
)