package com.example.campusconnect.feature.map.styling

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.campusconnect.feature.map.mapengine.rendering.MarkerCanvasPainter
import com.example.campusconnect.feature.map.mapengine.rendering.StandardMarkerPainter
import com.example.campusconnect.feature.map.model.*

/** Keeps Home's sealed marker type intact through MapView and dispatches to its visual family. */
object HomeMarkerPainter : MarkerCanvasPainter<MapMarker> {
    override fun DrawScope.drawMarker(
        marker: MapMarker,
        screenX: Float,
        screenY: Float,
        zoom: Float,
        showLabel: Boolean
    ) {
        with(StandardMarkerPainter) {
            when (marker) {
                is UserMarkerData -> drawUserVisual(marker, screenX, screenY, zoom, gender = marker.userGender)
                is LocalUserMarkerData -> drawLocalUserMarker(marker, screenX, screenY)
                is PoiMarkerData -> drawPoiVisual(marker, screenX, screenY, zoom, showLabel, marker.priority, marker.isHighlighted)
                is EventMarkerData -> drawEventVisual(marker, screenX, screenY, zoom, marker.priority)
                is ShopMarkerData -> drawShopVisual(marker, screenX, screenY, zoom)
            }
        }
    }

    private fun DrawScope.drawLocalUserMarker(marker: LocalUserMarkerData, x: Float, y: Float) {
        val alpha = if (marker.isStale) 0.55f else 1f
        drawCircle(Color(0x334285F4).copy(alpha = alpha), radius = 27f, center = Offset(x, y))
        drawCircle(Color.White.copy(alpha = alpha), radius = 13f, center = Offset(x, y))
        drawCircle(
            color = Color(0xFF1A73E8).copy(alpha = alpha),
            radius = 10f,
            center = Offset(x, y),
            style = Stroke(width = 4f)
        )
    }
}
