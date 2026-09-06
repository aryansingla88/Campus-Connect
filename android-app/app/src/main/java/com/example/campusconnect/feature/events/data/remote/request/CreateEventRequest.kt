package com.example.campusconnect.feature.events.data.remote.request

data class CreateEventRequest(

    val title: String,

    val description: String? = null,

    val latitude: Double,

    val longitude: Double,

    val startTime: String,

    val endTime: String? = null,

    val clubId: Int? = null,

    val hostName: String? = null,

    val venue: String? = null,

    val visibilityType: String,

    val visibilityValue: String? = null,

    val registrationType: String,

    val registrationLink: String? = null,

    val priority: Int,

    val categoryId: Int
)