package com.example.campusconnect.feature.map.mapengine

data class MapMarker(
    val id: Int,                         // Strict Int ID (Matches Backend Entity Primary Key)
    val sourceId: Int? = null,           // Strict Int ID
    val type: MarkerType,
    val latitude: Double,
    val longitude: Double,
    val label: String,

    // Backend Fields
    val userId: Int? = null,             // Strict Int
    val eventId: Int? = null,            // Strict Int
    val insideCampus: Boolean? = null,   // Campus presence badge

    // UI Visual & Render Fields
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