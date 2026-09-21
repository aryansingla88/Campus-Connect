package com.example.campusconnect.feature.map.mapengine

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.mapengine.model.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import kotlin.math.pow

private const val MAP_IMAGE_WIDTH = 3000f
private const val MAP_IMAGE_HEIGHT = 3000f
private const val MIN_ZOOM = 2.1f
private const val MAX_ZOOM = 5f

// POI-only priority gating. Priority 4 = always visible, priority 3 shows
// from mid zoom onward, priority 1-2 only show near max zoom. Values sit
// inside the existing [MIN_ZOOM, MAX_ZOOM] range and can be tuned freely.
private const val POI_ZOOM_MID_THRESHOLD = 2.3f
private const val POI_ZOOM_HIGH_THRESHOLD = 4.2f

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    markers: List<MarkerRenderData> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    onMapTap: (Float, Float) -> Unit = { _, _ -> },
    initialFocusMarkerId: String? = null,
    initialZoom: Float = 2.2f
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var initialFocusApplied by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
    ) {
        LaunchedEffect(containerSize, markers, initialFocusMarkerId) {
            if (
                !initialFocusApplied &&
                containerSize.width > 0 &&
                containerSize.height > 0 &&
                markers.isNotEmpty()
            ) {
                val focusMarker = markers.firstOrNull { it.id == initialFocusMarkerId }
                    ?: markers.firstOrNull()

                focusMarker?.let { marker ->
                    val bounds = calculateImageBounds(
                        width = containerSize.width.toFloat(),
                        height = containerSize.height.toFloat()
                    )

                    val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                    val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                    val targetScale = initialZoom.coerceIn(MIN_ZOOM, MAX_ZOOM)

                    val targetOffset = Offset(
                        x = containerSize.width / 2f - mapX * targetScale,
                        y = containerSize.height / 2f - mapY * targetScale
                    )

                    scale = targetScale
                    offset = clampOffsetSmooth(
                        offset = targetOffset,
                        scale = targetScale,
                        containerWidth = containerSize.width.toFloat(),
                        containerHeight = containerSize.height.toFloat(),
                        bounds = bounds
                    )

                    initialFocusApplied = true
                }
            }
        }

        Image(
            painter = painterResource(id = R.drawable.campus_map),
            contentDescription = "Campus Map",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                },
            contentScale = ContentScale.Fit
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val bounds = calculateImageBounds(size.width, size.height)

            // POI-only zoom gating: priority 4 always renders, priority 3
            // renders from mid zoom, priority 1-2 only render near max
            // zoom. All other marker types are unaffected.
            val renderMarkers = markers.filter { marker ->
                marker.type != MarkerType.POI || isPoiVisibleAtZoom(marker.priority, scale)
            }

            // POI-only label collision avoidance: higher-priority (and
            // selected) POI labels claim space first; a colliding label is
            // hidden while its dot still renders.
            val poiLabelVisibility = resolvePoiLabelVisibility(
                poiMarkers = renderMarkers.filter { it.type == MarkerType.POI },
                bounds = bounds,
                scale = scale,
                offset = offset
            )

            renderMarkers.forEach { marker ->
                val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                drawMarker(
                    marker = marker,
                    x = mapX * scale + offset.x,
                    y = mapY * scale + offset.y,
                    zoom = scale,
                    showPoiLabel = poiLabelVisibility[marker.id] ?: true
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { centroid, pan, zoom, _ ->
                        val bounds = calculateImageBounds(
                            width = size.width.toFloat(),
                            height = size.height.toFloat()
                        )

                        val oldScale = scale
                        val newScale = (oldScale * zoom).coerceIn(MIN_ZOOM, MAX_ZOOM)
                        val zoomChange = newScale / oldScale

                        val newOffset = centroid - (centroid - offset) * zoomChange + pan

                        scale = newScale
                        offset = clampOffsetSmooth(
                            offset = newOffset,
                            scale = newScale,
                            containerWidth = size.width.toFloat(),
                            containerHeight = size.height.toFloat(),
                            bounds = bounds
                        )
                    }
                }
                .pointerInput(markers, scale, offset) {
                    detectTapGestures { tap ->
                        val bounds = calculateImageBounds(
                            width = size.width.toFloat(),
                            height = size.height.toFloat()
                        )

                        // Must match the Canvas's render filter exactly —
                        // a POI that's hidden at this zoom level (see
                        // isPoiVisibleAtZoom) should not be tappable either.
                        val tappableMarkers = markers.filter { marker ->
                            marker.type != MarkerType.POI ||
                                    isPoiVisibleAtZoom(marker.priority, scale)
                        }

                        val tappedMarker = tappableMarkers.firstOrNull { marker ->
                            val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                            val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                            val markerScreenX = mapX * scale + offset.x
                            val markerScreenY = mapY * scale + offset.y

                            val dx = markerScreenX - tap.x
                            val dy = markerScreenY - tap.y

                            val distanceSq = dx.pow(2) + dy.pow(2)
                            val hitRadius = marker.radius + 45f

                            distanceSq <= hitRadius.pow(2)
                        }

                        if (tappedMarker != null) {
                            onMarkerClick(tappedMarker.id)
                        } else {
                            val rawX = (tap.x - offset.x) / scale
                            val rawY = (tap.y - offset.y) / scale

                            val imageX = ((rawX - bounds.left) / bounds.width) * MAP_IMAGE_WIDTH
                            val imageY = ((rawY - bounds.top) / bounds.height) * MAP_IMAGE_HEIGHT

                            Log.d("MAP_PIXEL", "Tapped image pixel: x=$imageX, y=$imageY")
                            onMapTap(imageX, imageY)
                        }
                    }
                }
        )
    }
}

private data class ImageBounds(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

private fun calculateImageBounds(
    width: Float,
    height: Float
): ImageBounds {
    val imageAspect = MAP_IMAGE_WIDTH / MAP_IMAGE_HEIGHT
    val canvasAspect = width / height

    return if (canvasAspect > imageAspect) {
        val imageHeight = height
        val imageWidth = imageHeight * imageAspect
        ImageBounds(
            left = (width - imageWidth) / 2f,
            top = 0f,
            width = imageWidth,
            height = imageHeight
        )
    } else {
        val imageWidth = width
        val imageHeight = imageWidth / imageAspect
        ImageBounds(
            left = 0f,
            top = (height - imageHeight) / 2f,
            width = imageWidth,
            height = imageHeight
        )
    }
}

private fun clampOffsetSmooth(
    offset: Offset,
    scale: Float,
    containerWidth: Float,
    containerHeight: Float,
    bounds: ImageBounds
): Offset {
    val mapWidth = bounds.width * scale
    val mapHeight = bounds.height * scale

    val mapLeft = bounds.left * scale
    val mapTop = bounds.top * scale

    val minX = containerWidth - mapLeft - mapWidth
    val maxX = -mapLeft

    val minY = containerHeight - mapTop - mapHeight
    val maxY = -mapTop

    val finalX = if (mapWidth <= containerWidth) {
        (containerWidth - mapWidth) / 2f - mapLeft
    } else {
        offset.x.coerceIn(minX, maxX)
    }

    val finalY = if (mapHeight <= containerHeight) {
        (containerHeight - mapHeight) / 2f - mapTop
    } else {
        offset.y.coerceIn(minY, maxY)
    }

    return Offset(finalX, finalY)
}

// --- POI-only zoom gating -------------------------------------------------

private fun isPoiVisibleAtZoom(priority: Int, zoom: Float): Boolean {
    return when (priority) {
        4 -> true
        3 -> zoom >= POI_ZOOM_MID_THRESHOLD
        2, 1 -> zoom >= POI_ZOOM_HIGH_THRESHOLD
        else -> true
    }
}

// --- POI-only label collision avoidance -----------------------------------

private fun projectMarkerToScreen(
    marker: MarkerRenderData,
    bounds: ImageBounds,
    scale: Float,
    offset: Offset
): Offset {
    val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
    val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height
    return Offset(mapX * scale + offset.x, mapY * scale + offset.y)
}

private fun computeVisualRadius(marker: MarkerRenderData, zoom: Float): Float {
    val zoomBoost = (zoom - 1f).coerceIn(0f, 2f) * 2.2f
    return (marker.radius + zoomBoost).coerceIn(8f, 30f)
}

private data class LabelBounds(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    fun intersects(other: LabelBounds): Boolean =
        left < other.right && right > other.left &&
                top < other.bottom && bottom > other.top
}

/**
 * Decides, per POI marker id, whether its text label should render. The
 * dot itself always renders regardless of this result — only the label is
 * suppressed on collision. Higher priority (then selected, then
 * highlighted) POIs claim label space first.
 */
private fun resolvePoiLabelVisibility(
    poiMarkers: List<MarkerRenderData>,
    bounds: ImageBounds,
    scale: Float,
    offset: Offset
): Map<String, Boolean> {
    if (poiMarkers.isEmpty()) return emptyMap()

    val ordered = poiMarkers.sortedWith(
        compareByDescending<MarkerRenderData> { it.priority }
            .thenByDescending { it.isSelected }
            .thenByDescending { it.isHighlighted }
    )

    val placedBoxes = mutableListOf<LabelBounds>()
    val visibility = mutableMapOf<String, Boolean>()
    val measurePaint = Paint()

    for (marker in ordered) {
        val screenPos = projectMarkerToScreen(marker, bounds, scale, offset)
        val radius = computeVisualRadius(marker, scale)
        val labelTextSize = if (marker.isHighlighted) 27f else 23f

        measurePaint.textSize = labelTextSize
        measurePaint.isFakeBoldText = marker.isHighlighted

        val textWidth = measurePaint.measureText(marker.label)
        val labelWidth = textWidth + 22f
        val labelHeight = if (marker.isHighlighted) 30f else 26f

        val labelLeft = screenPos.x + radius + 8f
        val labelTop = screenPos.y - labelHeight / 2f

        val box = LabelBounds(
            left = labelLeft,
            top = labelTop,
            right = labelLeft + labelWidth,
            bottom = labelTop + labelHeight
        )

        val collides = placedBoxes.any { it.intersects(box) }

        if (!collides) {
            placedBoxes += box
            visibility[marker.id] = true
        } else {
            // Selected marker keeps its label even if it overlaps; every
            // other loser just hides its text and keeps its dot.
            visibility[marker.id] = marker.isSelected
        }
    }

    return visibility
}

private fun DrawScope.drawMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    zoom: Float,
    showPoiLabel: Boolean = true
) {
    val zoomBoost = (zoom - 1f).coerceIn(0f, 2f) * 2.2f
    val visualRadius = (marker.radius + zoomBoost).coerceIn(8f, 30f)
    val selectedRadius = visualRadius + 8f
    val labelTextSize = if (marker.isHighlighted) 27f else 23f

    when (marker.type) {
        MarkerType.POI -> {
            drawPoiMarker(
                marker = marker,
                x = x,
                y = y,
                radius = visualRadius,
                labelTextSize = labelTextSize,
                showLabel = showPoiLabel
            )
        }

        MarkerType.USER -> {
            val userRadius = if (marker.isSelected) {
                visualRadius * 1.18f
            } else {
                visualRadius
            }

            drawUserAvatarMarker(
                marker = marker,
                x = x,
                y = y,
                radius = userRadius
            )
        }

        MarkerType.SHOP -> {
            drawShopMarker(
                marker = marker,
                x = x,
                y = y,
                radius = visualRadius
            )
        }

        MarkerType.EVENT -> {
            val eventRadius = if (marker.isSelected) {
                visualRadius * 1.08f
            } else {
                visualRadius
            }

            if (marker.priority >= 2) {
                drawHighPriorityEventPin(
                    x = x,
                    y = y,
                    radius = eventRadius,
                    isSelected = marker.isSelected,
                    priority = marker.priority
                )
            } else {
                drawLowPriorityEventPin(
                    x = x,
                    y = y,
                    radius = eventRadius,
                    isSelected = marker.isSelected,
                    priority = marker.priority
                )
            }
        }
    }

    if (
        marker.isSelected &&
        marker.type != MarkerType.SHOP &&
        marker.type != MarkerType.USER &&
        marker.type != MarkerType.EVENT
    ) {
        drawCircle(
            color = Color.White,
            radius = selectedRadius,
            center = Offset(x, y),
            style = Stroke(width = 3f)
        )

        drawCircle(
            color = Color(0xFFFFA726).copy(alpha = 0.35f),
            radius = selectedRadius + 5f,
            center = Offset(x, y),
            style = Stroke(width = 5f)
        )
    }
}

private fun DrawScope.drawPoiMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    radius: Float,
    labelTextSize: Float,
    showLabel: Boolean = true
) {
    val dotColor = if (marker.isHighlighted) {
        Color(0xFF00C853)
    } else {
        Color(0xFF66BB6A)
    }

    val ringColor = if (marker.isHighlighted) {
        Color(0xFFB9F6CA)
    } else {
        Color.White.copy(alpha = 0.75f)
    }

    if (marker.isHighlighted) {
        drawCircle(
            color = dotColor.copy(alpha = 0.28f),
            radius = radius + 8f,
            center = Offset(x, y)
        )
    }

    drawCircle(
        color = ringColor,
        radius = radius + 3f,
        center = Offset(x, y)
    )

    drawCircle(
        color = dotColor,
        radius = radius,
        center = Offset(x, y)
    )

    if (!showLabel) return

    val paint = Paint().apply {
        color = android.graphics.Color.WHITE
        textSize = labelTextSize
        isFakeBoldText = marker.isHighlighted
    }

    val textWidth = paint.measureText(marker.label)
    val labelWidth = textWidth + 22f
    val labelHeight = if (marker.isHighlighted) 30f else 26f

    val labelLeft = x + radius + 8f
    val labelTop = y - labelHeight / 2f

    drawRoundRect(
        color = Color.Black.copy(alpha = if (marker.isHighlighted) 0.58f else 0.42f),
        topLeft = Offset(labelLeft, labelTop),
        size = Size(labelWidth, labelHeight),
        cornerRadius = CornerRadius(13f, 13f)
    )

    drawContext.canvas.nativeCanvas.drawText(
        marker.label,
        labelLeft + 11f,
        y + labelTextSize / 3f,
        paint
    )
}

private fun DrawScope.drawUserAvatarMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    radius: Float
) {
    val isFemale = marker.gender == "female"

    val markerColor = if (isFemale) {
        Color(0xFFE83E8C)
    } else {
        Color(0xFF4285F4)
    }

    val pinRadius = radius * 1.30f
    val circleCenter = Offset(x, y - pinRadius * 0.15f)
    val circleRadius = pinRadius
    val tipY = y + pinRadius * 1.60f

    fun createPinPath(
        centerX: Float,
        centerY: Float,
        r: Float,
        tip: Float
    ): Path {
        return Path().apply {
            moveTo(centerX, tip)

            cubicTo(
                centerX - r * 0.45f,
                centerY + r * 0.65f,
                centerX - r,
                centerY + r * 0.45f,
                centerX - r,
                centerY
            )

            cubicTo(
                centerX - r,
                centerY - r * 0.62f,
                centerX - r * 0.55f,
                centerY - r,
                centerX,
                centerY - r
            )

            cubicTo(
                centerX + r * 0.55f,
                centerY - r,
                centerX + r,
                centerY - r * 0.62f,
                centerX + r,
                centerY
            )

            cubicTo(
                centerX + r,
                centerY + r * 0.45f,
                centerX + r * 0.45f,
                centerY + r * 0.65f,
                centerX,
                tip
            )

            close()
        }
    }

    val borderPath = createPinPath(
        centerX = circleCenter.x,
        centerY = circleCenter.y,
        r = circleRadius + 3.5f,
        tip = tipY + 3f
    )

    val pinPath = createPinPath(
        centerX = circleCenter.x,
        centerY = circleCenter.y,
        r = circleRadius,
        tip = tipY
    )

    if (marker.isSelected) {
        drawPath(
            path = borderPath,
            color = Color.White.copy(alpha = 0.42f),
            style = Stroke(width = radius * 0.65f)
        )

        drawCircle(
            color = Color.White.copy(alpha = 0.22f),
            radius = radius * 1.45f,
            center = Offset(x, y)
        )
    }

    drawCircle(
        color = Color.Black.copy(alpha = 0.18f),
        radius = pinRadius * 0.55f,
        center = Offset(x + 1.5f, tipY + 2f)
    )

    drawPath(
        path = borderPath,
        color = Color.White
    )

    drawPath(
        path = pinPath,
        color = markerColor
    )

    drawCircle(
        color = Color.White,
        radius = pinRadius * 0.25f,
        center = Offset(
            x,
            circleCenter.y - pinRadius * 0.18f
        )
    )

    drawRoundRect(
        color = Color.White,
        topLeft = Offset(
            x - pinRadius * 0.45f,
            circleCenter.y + pinRadius * 0.18f
        ),
        size = Size(
            pinRadius * 0.9f,
            pinRadius * 0.38f
        ),
        cornerRadius = CornerRadius(
            pinRadius * 0.18f,
            pinRadius * 0.18f
        )
    )
}

private fun DrawScope.drawShopMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    radius: Float
) {
    val shopColor = Color(0xFFC9992466)
    val windowColor = Color(0xFFFFEFE6)

    val iconWidth = radius * 3.3f
    val iconHeight = radius * 2.9f

    val left = x - iconWidth / 2f
    val top = y - iconHeight / 2f

    val roofPeakY = top
    val roofShoulderY = top + iconHeight * 0.26f
    val roofBaseY = top + iconHeight * 0.43f
    val scallopDepth = iconHeight * 0.16f

    val bodyTop = roofBaseY + scallopDepth * 0.75f
    val bodyLeft = left + iconWidth * 0.18f
    val bodyWidth = iconWidth * 0.64f
    val bodyHeight = iconHeight * 0.50f
    val bodyBottom = bodyTop + bodyHeight

    val roofPath = Path().apply {
        moveTo(x, roofPeakY)

        lineTo(left + iconWidth * 0.18f, roofShoulderY)

        quadraticBezierTo(
            left + iconWidth * 0.06f,
            roofShoulderY + iconHeight * 0.04f,
            left + iconWidth * 0.08f,
            roofBaseY
        )

        val startX = left + iconWidth * 0.08f
        val scallopWidth = iconWidth * 0.21f

        repeat(4) { index ->
            val sx = startX + index * scallopWidth
            val ex = sx + scallopWidth
            val cx = sx + scallopWidth / 2f

            quadraticBezierTo(
                cx,
                roofBaseY + scallopDepth,
                ex,
                roofBaseY
            )
        }

        quadraticBezierTo(
            left + iconWidth * 0.94f,
            roofShoulderY + iconHeight * 0.04f,
            left + iconWidth * 0.82f,
            roofShoulderY
        )

        lineTo(x, roofPeakY)
        close()
    }

    drawPath(
        path = roofPath,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = radius * 1.25f)
    )

    drawRoundRect(
        color = Color.White.copy(alpha = 0.28f),
        topLeft = Offset(
            bodyLeft - radius * 0.45f,
            bodyTop - radius * 0.2f
        ),
        size = Size(
            bodyWidth + radius * 0.9f,
            bodyHeight + radius * 0.55f
        ),
        cornerRadius = CornerRadius(radius * 0.3f, radius * 0.3f)
    )

    drawPath(
        path = roofPath,
        color = Color.Black.copy(alpha = 0.16f),
        style = Stroke(width = radius * 0.35f)
    )

    drawRect(
        color = Color.Black.copy(alpha = 0.14f),
        topLeft = Offset(bodyLeft + 2f, bodyTop + 3f),
        size = Size(bodyWidth, bodyHeight)
    )

    drawPath(
        path = roofPath,
        color = shopColor
    )

    drawRect(
        color = shopColor,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight)
    )

    drawRect(
        color = windowColor,
        topLeft = Offset(
            bodyLeft + bodyWidth * 0.58f,
            bodyTop + bodyHeight * 0.28f
        ),
        size = Size(
            bodyWidth * 0.24f,
            bodyHeight * 0.27f
        )
    )

    drawShopLabel(
        label = marker.label,
        centerX = x,
        topY = bodyBottom + 10f
    )
}

private fun DrawScope.drawShopLabel(
    label: String,
    centerX: Float,
    topY: Float
) {
    val textPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        textSize = 22f
        textAlign = Paint.Align.LEFT
        isFakeBoldText = true
    }

    val textWidth = textPaint.measureText(label)

    val horizontalPadding = 10f
    val bgHeight = 28f
    val bgWidth = textWidth + horizontalPadding * 2f

    val bgLeft = centerX - bgWidth / 2f
    val bgTop = topY
    val bgCorner = 10f

    drawRoundRect(
        color = Color.Black.copy(alpha = 0.48f),
        topLeft = Offset(bgLeft, bgTop),
        size = Size(bgWidth, bgHeight),
        cornerRadius = CornerRadius(bgCorner, bgCorner)
    )

    val textX = centerX - textWidth / 2f
    val textY = bgTop + bgHeight / 2f - (textPaint.descent() + textPaint.ascent()) / 2f

    drawContext.canvas.nativeCanvas.drawText(
        label,
        textX,
        textY,
        textPaint
    )
}

private fun DrawScope.drawLowPriorityEventPin(
    x: Float,
    y: Float,
    radius: Float,
    isSelected: Boolean,
    priority: Int
) {
    val eventColor = Color(0xFFFF6F00)

    val pinRadius = radius * 1.05f
    val circleCenterY = y - pinRadius * 0.18f
    val tipY = y + pinRadius * 1.32f

    val pinPath = createEventPinPath(
        centerX = x,
        centerY = circleCenterY,
        radius = pinRadius,
        tipY = tipY
    )

    if (isSelected) {
        drawEventAreaHalo(
            x = x,
            y = circleCenterY,
            radius = pinRadius,
            priority = priority
        )
    }

    drawCircle(
        color = Color.Black.copy(alpha = 0.18f),
        radius = pinRadius * 0.75f,
        center = Offset(x + 2f, tipY - pinRadius * 0.05f)
    )

    drawPath(
        path = createEventPinPath(
            centerX = x,
            centerY = circleCenterY,
            radius = pinRadius + 3.2f,
            tipY = tipY + 3f
        ),
        color = Color.White
    )

    drawPath(
        path = pinPath,
        color = eventColor
    )

    drawCircle(
        color = Color.White,
        radius = pinRadius * 0.36f,
        center = Offset(x, circleCenterY)
    )
}

private fun DrawScope.drawHighPriorityEventPin(
    x: Float,
    y: Float,
    radius: Float,
    isSelected: Boolean,
    priority: Int
) {
    val eventColor = Color(0xFFE63100)
    val innerCircleColor = Color.White

    val pinRadius = radius * 1.12f
    val circleCenterY = y - pinRadius * 0.18f
    val tipY = y + pinRadius * 1.35f

    val pinPath = createEventPinPath(
        centerX = x,
        centerY = circleCenterY,
        radius = pinRadius,
        tipY = tipY
    )

    if (isSelected) {
        drawEventAreaHalo(
            x = x,
            y = circleCenterY,
            radius = pinRadius,
            priority = priority
        )
    }

    drawCircle(
        color = Color.Black.copy(alpha = 0.20f),
        radius = pinRadius * 0.85f,
        center = Offset(x + 2f, tipY - pinRadius * 0.03f)
    )

    drawPath(
        path = createEventPinPath(
            centerX = x,
            centerY = circleCenterY,
            radius = pinRadius + 3.5f,
            tipY = tipY + 3f
        ),
        color = Color.White
    )

    drawPath(
        path = pinPath,
        color = eventColor
    )

    drawCircle(
        color = innerCircleColor,
        radius = pinRadius * 0.36f,
        center = Offset(x, circleCenterY)
    )
}

private fun createEventPinPath(
    centerX: Float,
    centerY: Float,
    radius: Float,
    tipY: Float
): Path {
    return Path().apply {
        moveTo(centerX, tipY)

        cubicTo(
            centerX - radius * 0.45f,
            centerY + radius * 0.72f,
            centerX - radius,
            centerY + radius * 0.45f,
            centerX - radius,
            centerY
        )

        cubicTo(
            centerX - radius,
            centerY - radius * 0.62f,
            centerX - radius * 0.55f,
            centerY - radius,
            centerX,
            centerY - radius
        )

        cubicTo(
            centerX + radius * 0.55f,
            centerY - radius,
            centerX + radius,
            centerY - radius * 0.62f,
            centerX + radius,
            centerY
        )

        cubicTo(
            centerX + radius,
            centerY + radius * 0.45f,
            centerX + radius * 0.45f,
            centerY + radius * 0.72f,
            centerX,
            tipY
        )

        close()
    }
}

private fun DrawScope.drawEventAreaHalo(
    x: Float,
    y: Float,
    radius: Float,
    priority: Int
) {
    val priorityScale = when {
        priority >= 3 -> 2.65f
        priority == 2 -> 2.25f
        else -> 1.90f
    }

    val ringRadius = radius * priorityScale

    drawCircle(
        color = Color.White.copy(alpha = 0.88f),
        radius = ringRadius,
        center = Offset(x, y),
        style = Stroke(width = 4.5f)
    )
}