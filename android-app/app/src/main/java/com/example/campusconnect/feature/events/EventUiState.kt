package com.example.campusconnect.feature.events

data class EventUiState(
    val title: String = "",
    val description: String = "",

    val isPoster: Boolean = false,
    val posterUrl: String = "",

    val selectedLocation: Pair<Double, Double>? = null,

    val date: String = "",
    val venue: String = "",

    val startTime: String = "",
    val endTime: String = "",

    val clubName: String = "",
    val category: String = "",

    val visibilityType: String = "",
    val visibilityValue: String = "",

    val registrationRequired: Boolean = false,
    val registrationLink: String = "",
    val inAppRegistration: Boolean = false,

    val error: String? = null,
    val success: Boolean = false
)