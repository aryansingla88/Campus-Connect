package com.example.campusconnect.model

data class Event(

    val id: Int,

    // 🔹 BASIC
    val title: String,
    val description: String? = null,

    // 🔹 LOCATION
    val latitude: Double,
    val longitude: Double,

    // UI positioning (temporary for map)
    val xRatio: Float = 0.5f,
    val yRatio: Float = 0.5f,

    // 🔹 TIME
    val date: String = "",              // added separately
    val startTime: String = "",
    val endTime: String? = null,

    // 🔹 ORGANIZATION
    val createdBy: Int = 0,
    val clubName: String = "",

    // 🔹 MEDIA
    val isPoster: Boolean = false,
    val posterUrl: String? = null,

    // 🔹 CLASSIFICATION
    val category: String = "",

    // 🔹 VISIBILITY
    val visibilityType: String = "",
    val visibilityValue: String = "",

    // 🔹 REGISTRATION
    val registrationRequired: Boolean = false,
    val registrationLink: String = "",
    val inAppRegistration: Boolean = false,

    // 🔹 EXTRA
    val venue: String = ""
)