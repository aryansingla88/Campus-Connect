package com.example.campusconnect.feature.map.mapengine

data class MapMarker(
    val id: String,
    val sourceId: String? = null,
    val type: MarkerType,
    val latitude: Double,
    val longitude: Double,
    val label: String,

    // New Backend Fields
    val userId: Int? = null,             // Added for Backend VisibleUserResponse
    val insideCampus: Boolean? = null,   // Added for campus presence badge

    // UI Visual & Render Fields (Preserved)
    val gender: String? = null,
    val size: MarkerSize = MarkerSize.MEDIUM,
    val isHighlighted: Boolean = false,
    var x: Float = 0f,
    var y: Float = 0f,
    val state: MarkerState = MarkerState.DEFAULT,
    val priority: Int = 0,
    val isActive: Boolean = true
)

enum class MarkerState {
    DEFAULT,
    SELECTED,
    ACTIVE,
    DISABLED
}

enum class MarkerSize {
    SMALL,
    MEDIUM,
    LARGE
}