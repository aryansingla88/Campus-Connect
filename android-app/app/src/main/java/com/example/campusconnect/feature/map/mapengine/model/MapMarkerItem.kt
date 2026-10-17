package com.example.campusconnect.feature.map.mapengine.model

/**
 * Minimal contract consumed by the map engine. Feature-specific appearance
 * and ordering inputs stay with feature marker models and their adapters.
 */
interface MapMarkerItem {
    val id: String
    val x: Float
    val y: Float
    val type: MarkerType
    val label: String get() = ""
    val isSelected: Boolean get() = false
}
