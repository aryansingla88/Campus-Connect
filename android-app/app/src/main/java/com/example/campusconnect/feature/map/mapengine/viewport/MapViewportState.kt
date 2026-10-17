package com.example.campusconnect.feature.map.mapengine.viewport

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

/**
 * Viewport state controller for the Base Map Engine.
 * Supports reactive zoom & pan state management along with explicit viewport commands
 * such as focusOn(position, zoom) and resetViewport().
 */
@Stable
class MapViewportState(
    initialZoom: Float = DEFAULT_ZOOM,
    initialOffset: Offset = Offset.Zero
) {
    var scale by mutableFloatStateOf(initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM))
        internal set

    var offset by mutableStateOf(initialOffset)
        internal set

    var containerSize by mutableStateOf(IntSize.Zero)

    val bounds: ImageBounds
        get() = MapViewport.calculateImageBounds(
            width = containerSize.width.toFloat(),
            height = containerSize.height.toFloat()
        )

    /**
     * Focuses the viewport on a specific map image coordinate (x, y) with the specified zoom level.
     */
    fun focusOn(position: Offset, zoom: Float = scale) {
        val targetScale = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        scale = targetScale

        if (containerSize.width > 0 && containerSize.height > 0) {
            offset = MapViewport.calculateFocusOffset(
                markerX = position.x,
                markerY = position.y,
                targetScale = targetScale,
                containerWidth = containerSize.width.toFloat(),
                containerHeight = containerSize.height.toFloat(),
                bounds = bounds
            )
        }
    }

    /**
     * Resets the viewport to the default zoom and centers the map within the container.
     */
    fun resetViewport(zoom: Float = DEFAULT_ZOOM) {
        val targetScale = zoom.coerceIn(MIN_ZOOM, MAX_ZOOM)
        scale = targetScale

        if (containerSize.width > 0 && containerSize.height > 0) {
            offset = MapViewport.calculateCenterOffset(
                scale = targetScale,
                containerWidth = containerSize.width.toFloat(),
                containerHeight = containerSize.height.toFloat(),
                bounds = bounds
            )
        } else {
            offset = Offset.Zero
        }
    }

    /**
     * Updates viewport scale and offset based on transform gesture input (pinch-zoom and pan).
     */
    fun onTransform(centroid: Offset, pan: Offset, zoom: Float) {
        if (containerSize.width <= 0 || containerSize.height <= 0) return

        val oldScale = scale
        val newScale = (oldScale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
        val zoomChange = newScale / oldScale
        val newOffset = centroid - (centroid - offset) * zoomChange + pan

        scale = newScale
        offset = MapViewport.clampOffsetSmooth(
            offset = newOffset,
            scale = newScale,
            containerWidth = containerSize.width.toFloat(),
            containerHeight = containerSize.height.toFloat(),
            bounds = bounds
        )
    }
}

@Composable
fun rememberMapViewportState(
    initialZoom: Float = DEFAULT_ZOOM,
    initialOffset: Offset = Offset.Zero
): MapViewportState {
    return remember {
        MapViewportState(
            initialZoom = initialZoom,
            initialOffset = initialOffset
        )
    }
}
