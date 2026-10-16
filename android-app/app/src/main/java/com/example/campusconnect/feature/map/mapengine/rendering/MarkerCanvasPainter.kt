package com.example.campusconnect.feature.map.mapengine.rendering

import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem

/**
 * Generic rendering capability for markers in the Base Map Engine.
 * Implementing painters define how custom marker models are visually
 * drawn onto the map's DrawScope.
 */
fun interface MarkerCanvasPainter<in T : MapMarkerItem> {
    fun DrawScope.drawMarker(
        marker: T,
        screenX: Float,
        screenY: Float,
        zoom: Float,
        showLabel: Boolean
    )
}
