package com.example.campusconnect.feature.map.mapengine.gestures

import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import com.example.campusconnect.feature.map.mapengine.interaction.MapHitTester
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.viewport.ImageBounds
import com.example.campusconnect.feature.map.mapengine.viewport.MapViewportState

/**
 * Installs map pan/zoom and tap handling on a full-size interaction layer.
 * Rendering stays in MapView; this class owns only gesture interpretation.
 */
fun <T : MapMarkerItem> Modifier.mapGestureHandler(
    viewportState: MapViewportState,
    tappableMarkers: List<T>,
    bounds: ImageBounds,
    scale: Float,
    offset: Offset,
    prioritySelector: (T) -> Int = { 0 },
    onMarkerClick: (String) -> Unit,
    onMapTap: (Float, Float) -> Unit
): Modifier = this
    .pointerInput(viewportState) {
        detectTransformGestures { centroid, pan, zoom, _ ->
            viewportState.onTransform(centroid, pan, zoom)
        }
    }
    .pointerInput(tappableMarkers, bounds, scale, offset) {
        detectTapGestures { tap ->
            val marker = MapHitTester.findTappedMarker(
                tap = tap,
                tappableMarkers = tappableMarkers,
                bounds = bounds,
                scale = scale,
                offset = offset,
                prioritySelector = prioritySelector
            )

            if (marker != null) {
                onMarkerClick(marker.id)
            } else {
                val imagePosition = MapHitTester.screenToImageCoordinates(
                    tap = tap,
                    bounds = bounds,
                    scale = scale,
                    offset = offset
                )
                onMapTap(imagePosition.x, imagePosition.y)
            }
        }
    }
