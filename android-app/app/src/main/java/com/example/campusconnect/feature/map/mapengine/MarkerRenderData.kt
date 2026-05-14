package com.example.campusconnect.feature.map.mapengine

data class MarkerRenderData(
    val x: Float,
    val y: Float,
    val radius: Float,
    val color: Long,
    val label: String,
    val isSelected: Boolean
)