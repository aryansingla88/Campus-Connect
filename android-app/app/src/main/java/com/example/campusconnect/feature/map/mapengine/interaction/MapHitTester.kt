package com.example.campusconnect.feature.map.mapengine.interaction

import androidx.compose.ui.geometry.Offset
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.coordinates.MapCoordinateTransform
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import kotlin.math.pow

object MapHitTester {
    /**
     * Hit tests a screen tap against a list of markers positioned on the map.
     * Returns the topmost marker in draw order whose hit-box contains the tap point.
     */
    fun <T : MapMarkerItem> findTappedMarker(
        tap: Offset,
        tappableMarkers: List<T>,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset,
        prioritySelector: (T) -> Int = { 0 }
    ): T? {
        return tappableMarkers.asReversed().firstOrNull { marker ->
            val markerPosition = MapCoordinateTransform.imageToScreen(
                x = marker.x,
                y = marker.y,
                bounds = bounds,
                scale = scale,
                offset = offset
            )

            val dx = markerPosition.x - tap.x
            val dy = markerPosition.y - tap.y

            val distanceSq = dx.pow(2) + dy.pow(2)
            val hitRadius = MarkerStyleConfig.getHitRadius(
                marker.type, prioritySelector(marker), marker.isSelected
            )
            distanceSq <= hitRadius.pow(2)
        }
    }

    /**
     * Converts a screen coordinate into map image pixel coordinates (0..MAP_IMAGE_WIDTH, 0..MAP_IMAGE_HEIGHT).
     */
    fun screenToImageCoordinates(
        tap: Offset,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset
    ): Offset {
        return MapCoordinateTransform.screenToImage(
            point = tap,
            bounds = bounds,
            scale = scale,
            offset = offset
        )
    }
}
