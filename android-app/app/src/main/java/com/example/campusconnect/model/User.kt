package com.example.campusconnect.model


data class User(
    val id: Int,
    val username: String,
    val email: String? = null
)