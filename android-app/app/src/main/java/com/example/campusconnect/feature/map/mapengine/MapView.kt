package com.example.campusconnect.feature.map.mapengine

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.mapengine.coordinates.MapCoordinateTransform
import com.example.campusconnect.feature.map.mapengine.gestures.mapGestureHandler
import com.example.campusconnect.feature.map.mapengine.model.MapMarkerItem
import com.example.campusconnect.feature.map.mapengine.rendering.MarkerCanvasPainter
import com.example.campusconnect.feature.map.mapengine.rendering.StandardMarkerPainter
import com.example.campusconnect.feature.map.mapengine.styling.MarkerStyleConfig
import com.example.campusconnect.feature.map.mapengine.viewport.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.ceil

/**
 * Reusable Base Map Engine Composable.
 *
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
    onMapLoadComplete: () -> Unit = {},
    labelVisibilityResolver: ((
        visibleMarkers: List<T>,
        bounds: ImageBounds,
        scale: Float,
        offset: Offset
    ) -> Map<String, Boolean>)? = null
) {
    var initialFocusApplied by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val mapBitmap by produceState<Bitmap?>(
        initialValue = CampusMapBitmapCache.bitmap,
        context,
        viewportState
    ) {
        snapshotFlow { viewportState.containerSize }
            .first { it.width > 0 && it.height > 0 }

        // Reuse the cached bitmap across navigation and recomposition.
        value = withContext(Dispatchers.IO) {
            CampusMapBitmapCache.bitmap
                ?: synchronized(CampusMapBitmapCache.lock) {
                    CampusMapBitmapCache.bitmap ?: run {
                        val decoded = runCatching {
                            decodeCampusMap {
                                context.assets.open("campus_map__v2.png")
                            }
                        }.onFailure {
                            Log.e(
                                "MapView",
                                "Could not decode assets/campus_map__v2.png; " +
                                        "trying drawable fallback",
                                it
                            )
                        }.getOrNull() ?: runCatching {
                            decodeCampusMap {
                                context.resources.openRawResource(
                                    R.drawable.campus_map
                                )
                            }
                        }.onFailure {
                            Log.e(
                                "MapView",
                                "Could not decode drawable/campus_map as fallback",
                                it
                            )
                        }.getOrNull()

                        decoded?.let { result ->
                            Log.i(
                                "MapView",
                                "Loaded campus map: " +
                                        "source=${result.sourceWidth}x${result.sourceHeight}, " +
                                        "decoded=${result.bitmap.width}x${result.bitmap.height}, " +
                                        "sampleSize=${result.sampleSize}, " +
                                        "config=${result.bitmap.config}, " +
                                        "approxMemoryMiB=" +
                                        "${result.bitmap.allocationByteCount / (1024 * 1024)}"
                            )

                            result.bitmap.also {
                                CampusMapBitmapCache.bitmap = it
                            }
                        }
                    }
                }
        }

        // Notify the transition logic when loading has finished.
        // This also completes if both decoding attempts fail.
        onMapLoadComplete()
    }

    // Do not manually recycle this bitmap during composition disposal.
    // Compose may still reference it through BitmapPainter or a display list.

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged {
                viewportState.containerSize = it
            }
    ) {
        LaunchedEffect(
            viewportState.containerSize,
            initialFocusPosition
        ) {
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

        // Render the map only when its bitmap is available.
        val mapPainter = remember(mapBitmap) {
            mapBitmap?.let {
                BitmapPainter(it.asImageBitmap())
            }
        }

        mapPainter?.let { painter ->
            Image(
                painter = painter,
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
        }

        val bounds = viewportState.bounds
        val currentScale = viewportState.scale
        val currentOffset = viewportState.offset

        // FIX: Do not expose markers until the base map bitmap is ready.
        // Include mapBitmap in remember keys so this recomputes when loading finishes.
        val visibleMarkers = remember(
            mapBitmap,
            markers,
            currentScale,
            markerPriority
        ) {
            if (mapBitmap == null) {
                emptyList()
            } else {
                markers
                    .filter { marker ->
                        currentScale >=
                                MarkerStyleConfig.getMinZoomForMarker(
                                    marker.type,
                                    markerPriority(marker)
                                )
                    }
                    .sortedBy { marker ->
                        MarkerStyleConfig.getDrawOrder(
                            marker.type,
                            markerPriority(marker),
                            marker.isSelected
                        )
                    }
            }
        }

        // Labels also remain hidden until the map is ready.
        val labelVisibility = remember(
            mapBitmap,
            visibleMarkers,
            bounds,
            currentScale,
            currentOffset
        ) {
            if (mapBitmap == null) {
                emptyMap()
            } else {
                labelVisibilityResolver?.invoke(
                    visibleMarkers,
                    bounds,
                    currentScale,
                    currentOffset
                ) ?: emptyMap()
            }
        }

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            // Extra guard against rendering markers before bitmap readiness.
            if (mapBitmap != null) {
                visibleMarkers.forEach { marker ->
                    val screenPosition =
                        MapCoordinateTransform.imageToScreen(
                            x = marker.x,
                            y = marker.y,
                            bounds = bounds,
                            scale = currentScale,
                            offset = currentOffset
                        )

                    val showLabel =
                        labelVisibility[marker.id] ?: true

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

private const val MAX_BITMAP_DIMENSION = 2048

private data class DecodedCampusMap(
    val bitmap: Bitmap,
    val sourceWidth: Int,
    val sourceHeight: Int,
    val sampleSize: Int
)

private fun decodeCampusMap(
    openStream: () -> InputStream
): DecodedCampusMap {
    val bounds = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }

    openStream().use {
        BitmapFactory.decodeStream(it, null, bounds)
    }

    require(
        bounds.outWidth > 0 && bounds.outHeight > 0
    ) {
        "Unable to read campus map dimensions"
    }

    var sampleSize = 1

    while (
        ceil(bounds.outWidth.toDouble() / sampleSize).toInt() >
        MAX_BITMAP_DIMENSION ||
        ceil(bounds.outHeight.toDouble() / sampleSize).toInt() >
        MAX_BITMAP_DIMENSION
    ) {
        sampleSize *= 2
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565
        inScaled = false
    }

    val bitmap = openStream().use {
        BitmapFactory.decodeStream(it, null, options)
    } ?: error("BitmapFactory could not decode campus map")

    if (
        bitmap.width > MAX_BITMAP_DIMENSION ||
        bitmap.height > MAX_BITMAP_DIMENSION
    ) {
        val decodedWidth = bitmap.width
        val decodedHeight = bitmap.height

        // Safe to recycle: this bitmap has not been handed to Compose.
        bitmap.recycle()

        error(
            "Decoded campus map exceeded size cap: " +
                    "${decodedWidth}x${decodedHeight}"
        )
    }

    return DecodedCampusMap(
        bitmap = bitmap,
        sourceWidth = bounds.outWidth,
        sourceHeight = bounds.outHeight,
        sampleSize = sampleSize
    )
}

/**
 * Process-level cache.
 *
 * The bitmap is deliberately not recycled while Compose may still draw it.
 */
private object CampusMapBitmapCache {
    val lock = Any()

    @Volatile
    var bitmap: Bitmap? = null
}