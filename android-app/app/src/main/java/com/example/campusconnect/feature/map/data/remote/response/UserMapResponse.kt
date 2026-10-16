package com.example.campusconnect.feature.map.data.remote.response

data class UserMapResponse(
    val userId: Int,
    val username: String,
    val latitude: Double,
    val longitude: Double,
    val insideCampus: Boolean? = null,
    val gender: String? = null
)
