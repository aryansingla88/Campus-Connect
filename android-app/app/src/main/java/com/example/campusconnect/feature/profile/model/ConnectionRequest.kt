package com.example.campusconnect.feature.profile.model

enum class RequestType {
    INCOMING,
    OUTGOING
}

data class ConnectionRequest(
    val userId: String,
    val fullName: String,
    val course: String,
    val academicYear: Int,
    val avatarUrl: String? = null,
    val type: RequestType
)