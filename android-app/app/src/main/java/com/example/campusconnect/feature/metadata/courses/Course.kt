package com.example.campusconnect.feature.metadata.courses

data class Course(
    val courseId: Int,
    val degree: String,
    val programName: String,
    val courseCode: String?,
    val degreeLevel: String,
    val durationYears: Int
)