package com.example.campusconnect.feature.map.styling

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
                is PoiMarkerData -> drawPoiVisual(marker, screenX, screenY, zoom, showLabel, marker.priority, marker.isHighlighted)
                is EventMarkerData -> drawEventVisual(marker, screenX, screenY, zoom, marker.priority)
                is ShopMarkerData -> drawShopVisual(marker, screenX, screenY, zoom)
            }
        }
    }
}
