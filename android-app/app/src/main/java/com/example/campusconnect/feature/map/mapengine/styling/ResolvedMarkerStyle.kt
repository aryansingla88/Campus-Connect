package com.example.campusconnect.feature.map.mapengine.styling

import androidx.compose.ui.graphics.Color

/** Visual values resolved once for a marker before its shape is drawn. */
data class ResolvedMarkerStyle(
    val color: Color,
    val radius: Float,
    val labelTextSize: Float,
    val selectionScale: Float
)
