package com.example.campusconnect.feature.auth.data.remote.request

data class RegisterRequest(

    val username: String,

    val email: String,

    val password: String,

    val fullName: String,

    val courseId: Int,

    val admissionYear: Int,

    val gender: String,

    val dob: String,

    val rollNumber: String,

    val googleIdToken: String
)