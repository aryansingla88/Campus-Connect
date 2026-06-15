package com.example.campusconnect.feature.map.mapengine

data class MarkerRenderData(
    val id: String,
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Long,
    val label: String,
    val type: MarkerType,
    val gender: String? = null,
    val priority: Int = 0,
    val size: MarkerSize = MarkerSize.MEDIUM,
    val isHighlighted: Boolean = false,
    val isSelected: Boolean
)