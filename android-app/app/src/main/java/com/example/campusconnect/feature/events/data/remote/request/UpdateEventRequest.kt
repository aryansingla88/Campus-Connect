package com.example.campusconnect.feature.events.data.remote.request

data class UpdateEventRequest(
    val description: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val startTime: String? = null,

    val endTime: String? = null,

    val venue: String? = null,

    val visibilityType: String? = null,

    val visibilityValue: String? = null,

    val registrationLink: String? = null
)