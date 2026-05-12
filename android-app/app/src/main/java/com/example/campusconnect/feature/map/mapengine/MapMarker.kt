package com.example.campusconnect.feature.map.mapengine

data class MapMarker(
    val id: String,
    val sourceId: String,
    val type: MarkerType,
    val latitude: Double,
    val longitude: Double,
    val label: String,
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