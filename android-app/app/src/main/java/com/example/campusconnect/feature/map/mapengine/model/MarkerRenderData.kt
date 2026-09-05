package com.example.campusconnect.feature.map.mapengine.model

import com.example.campusconnect.feature.map.mapengine.model.MarkerType

data class MarkerRenderData(
    // Internal UI/map identifier
    val id: String,

    // Actual backend/database identifier
    val sourceId: Int,
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