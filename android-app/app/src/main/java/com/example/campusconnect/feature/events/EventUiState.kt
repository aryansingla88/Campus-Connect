package com.example.campusconnect.feature.events

data class EventUiState(
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val selectedLocation: Pair<Double, Double>? = null,

    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)