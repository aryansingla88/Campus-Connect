package com.example.campusconnect.feature.profile.model

enum class ConnectionStatus {
    NOT_CONNECTED,
    PENDING,
    CONNECTED
}

data class Connection(
    val userId: Int,
    val fullName: String,
    val course: String,
    val academicYear: String,
    val avatarUrl: String? = null,
    val status: ConnectionStatus
)