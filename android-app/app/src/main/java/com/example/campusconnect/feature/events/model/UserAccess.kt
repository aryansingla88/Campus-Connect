package com.example.campusconnect.feature.events.model

data class UserAccess(
    val id: Int,
    val name: String,
    val courseId: Int?,
    val admissionYear: Int?,
    val initials: String = name
        .split(" ")
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
)