package com.example.campusconnect.feature.profile.model

data class Course(
    val courseId   : String,
    val courseName : String,
    val hasBranch  : Boolean = false,
)