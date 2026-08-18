package com.example.campusconnect.feature.auth.data.remote.response

data class CourseResponse(
    val courseId: Int,
    val degree: String,
    val programname: String?,
    val courseCode: String?
)