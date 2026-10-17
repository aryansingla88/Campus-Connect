package com.example.campusconnect.feature.map.mapengine
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.mapengine.coordinates.MapCoordinateTransform
import com.example.campusconnect.feature.map.mapengine.gestures.mapGestureHandler
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.rendering.MarkerCanvasPainter
import com.example.campusconnect.feature.map.mapengine.rendering.StandardMarkerPainter
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.viewport.*

/**
 * Reusable Base Map Engine Composable.
 * Handles campus map image rendering, pan/zoom gestures, viewport calculations,
 * marker positioning, generic zoom-based visibility, and hit testing.
 */
@Composable
fun <T : MapMarkerItem> MapView(
    modifier: Modifier = Modifier,
    viewportState: MapViewportState = rememberMapViewportState(),
    markers: List<T> = emptyList(),
    markerPriority: (T) -> Int = { 0 },
    markerPainter: MarkerCanvasPainter<T> = StandardMarkerPainter,
    onMarkerClick: (String) -> Unit = {},
    onMapTap: (Float, Float) -> Unit = { _, _ -> },
    initialFocusPosition: Offset? = null,
    initialZoom: Float = DEFAULT_ZOOM,
    labelVisibilityResolver: ((visibleMarkers: List<T>, bounds: ImageBounds, scale: Float, offset: Offset) -> Map<String, Boolean>)? = null
) {
    var initialFocusApplied by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { viewportState.containerSize = it }
    ) {
        LaunchedEffect(viewportState.containerSize, initialFocusPosition) {
            if (
                !initialFocusApplied &&
                viewportState.containerSize.width > 0 &&
                viewportState.containerSize.height > 0 &&
                initialFocusPosition != null
            ) {
                viewportState.focusOn(
                    position = initialFocusPosition,
                    zoom = initialZoom
                )
                initialFocusApplied = true
            }
        }

        Image(
            painter = painterResource(id = R.drawable.campus_map),
            contentDescription = "Campus Map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = viewportState.scale
                    scaleY = viewportState.scale
                    translationX = viewportState.offset.x
                    translationY = viewportState.offset.y
                },
            contentScale = ContentScale.Fit
        )

        val bounds = viewportState.bounds
        val currentScale = viewportState.scale
        val currentOffset = viewportState.offset

        val visibleMarkers = remember(markers, currentScale, markerPriority) {
            markers.filter { marker ->
                currentScale >= MarkerStyleConfig.getMinZoomForMarker(marker.type, markerPriority(marker))
            }.sortedBy { marker ->
                MarkerStyleConfig.getDrawOrder(marker.type, markerPriority(marker), marker.isSelected)
            }
        }

        val labelVisibility = remember(visibleMarkers, bounds, currentScale, currentOffset) {
            labelVisibilityResolver?.invoke(visibleMarkers, bounds, currentScale, currentOffset)
                ?: emptyMap()
        }

        Canvas(modifier = Modifier.fillMaxSize()) {
            visibleMarkers.forEach { marker ->
                val screenPosition = MapCoordinateTransform.imageToScreen(
                    x = marker.x,
                    y = marker.y,
                    bounds = bounds,
                    scale = currentScale,
                    offset = currentOffset
                )

                val showLabel = labelVisibility[marker.id] ?: true

                with(markerPainter) {
                    drawMarker(
                        marker = marker,
                        screenX = screenPosition.x,
                        screenY = screenPosition.y,
                        zoom = currentScale,
                        showLabel = showLabel
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .mapGestureHandler(
                    viewportState = viewportState,
                    tappableMarkers = visibleMarkers,
                    bounds = bounds,
                    scale = currentScale,
                    offset = currentOffset,
                    prioritySelector = markerPriority,
                    onMarkerClick = onMarkerClick,
                    onMapTap = onMapTap
                )
        )
    }
}
