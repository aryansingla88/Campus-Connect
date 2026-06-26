package com.example.campusconnect.feature.auth.data.remote.request

data class RegisterRequest(

    val username: String,

    val email: String,

    val password: String,

    val realName: String,

    val course: String,

    val year: String,

    val gender: String,

    val dob: String,

    val rollNumber: String
)