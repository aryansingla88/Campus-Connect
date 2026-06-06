package com.example.campusconnect.feature.map

import androidx.compose.ui.geometry.CornerRadius
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.res.painterResource
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.mapengine.MarkerRenderData
import com.example.campusconnect.feature.map.mapengine.MarkerType
import kotlin.math.pow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
private const val MAP_IMAGE_WIDTH = 3000f
private const val MAP_IMAGE_HEIGHT = 3000f
private const val MIN_ZOOM = 2.1f
private const val MAX_ZOOM = 5f
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

            markers.forEach { marker ->
                val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                drawMarker(
                    marker = marker,
                    x = mapX * scale + offset.x,
                    y = mapY * scale + offset.y,
                    zoom = scale
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

                        val newOffset = if (newScale <= 1.01f) {
                            Offset.Zero
                        } else {
                            centroid - (centroid - offset) * zoomChange + pan
                        }

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

                        val tappedMarker = markers.firstOrNull { marker ->
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

private fun DrawScope.drawMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    zoom: Float
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
                labelTextSize = labelTextSize
            )
        }

        MarkerType.USER -> {
            drawUserAvatarMarker(
                marker = marker,
                x = x,
                y = y,
                radius = visualRadius
            )
        }

        MarkerType.SHOP -> {
            drawShopMarker(
                x = x,
                y = y,
                radius = visualRadius
            )
        }

        MarkerType.EVENT -> {
            if (marker.priority >= 2) {
                drawHighPriorityEventPin(
                    x = x,
                    y = y,
                    radius = visualRadius
                )
            } else {
                drawLowPriorityEventDiamond(
                    x = x,
                    y = y,
                    radius = visualRadius
                )
            }
        }
    }

    if (marker.isSelected) {
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
    labelTextSize: Float
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

    val paint = android.graphics.Paint().apply {
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

    val bgColor = if (isFemale) {
        Color(0xFFE91E63)
    } else {
        Color(0xFF2196F3)
    }

    val borderColor = if (isFemale) {
        Color(0xFFFFC1E3)
    } else {
        Color(0xFFBBDEFB)
    }

    drawCircle(
        color = borderColor,
        radius = radius + 4f,
        center = Offset(x, y)
    )

    drawCircle(
        color = bgColor,
        radius = radius,
        center = Offset(x, y)
    )

    // head
    drawCircle(
        color = Color.White,
        radius = radius * 0.28f,
        center = Offset(x, y - radius * 0.25f)
    )

    // body
    drawRoundRect(
        color = Color.White,
        topLeft = Offset(
            x - radius * 0.45f,
            y + radius * 0.1f
        ),
        size = Size(
            radius * 0.9f,
            radius * 0.52f
        ),
        cornerRadius = CornerRadius(radius * 0.28f, radius * 0.28f)
    )

    if (isFemale) {
        // small hair/bow accent
        drawCircle(
            color = Color(0xFFFFC1E3),
            radius = radius * 0.18f,
            center = Offset(x - radius * 0.38f, y - radius * 0.48f)
        )

        drawCircle(
            color = Color(0xFFFFC1E3),
            radius = radius * 0.18f,
            center = Offset(x + radius * 0.38f, y - radius * 0.48f)
        )
    } else {
        // small shoulder line
        drawLine(
            color = Color(0xFFBBDEFB),
            start = Offset(x - radius * 0.42f, y + radius * 0.42f),
            end = Offset(x + radius * 0.42f, y + radius * 0.42f),
            strokeWidth = 2.5f
        )
    }
}

private fun DrawScope.drawShopMarker(
    x: Float,
    y: Float,
    radius: Float
) {
    val roofColor = Color(0xFFD84315)
    val bodyColor = Color(0xFFFFCCBC)
    val doorColor = Color(0xFF6D4C41)

    // roof
    val roofPath = Path().apply {
        moveTo(x, y - radius)
        lineTo(x - radius * 1.1f, y - radius * 0.15f)
        lineTo(x + radius * 1.1f, y - radius * 0.15f)
        close()
    }

    drawPath(
        path = roofPath,
        color = roofColor
    )

    // body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(
            x - radius * 0.85f,
            y - radius * 0.12f
        ),
        size = Size(
            radius * 1.7f,
            radius * 1.25f
        ),
        cornerRadius = CornerRadius(5f, 5f)
    )

    // shop base border
    drawRoundRect(
        color = roofColor.copy(alpha = 0.18f),
        topLeft = Offset(
            x - radius * 0.85f,
            y - radius * 0.12f
        ),
        size = Size(
            radius * 1.7f,
            radius * 1.25f
        ),
        cornerRadius = CornerRadius(5f, 5f),
        style = Stroke(width = 2f)
    )

    // door
    drawRoundRect(
        color = doorColor,
        topLeft = Offset(
            x - radius * 0.18f,
            y + radius * 0.35f
        ),
        size = Size(
            radius * 0.36f,
            radius * 0.55f
        ),
        cornerRadius = CornerRadius(3f, 3f)
    )

    // window
    drawCircle(
        color = Color.White.copy(alpha = 0.95f),
        radius = radius * 0.14f,
        center = Offset(x - radius * 0.5f, y + radius * 0.32f)
    )

    drawCircle(
        color = Color.White.copy(alpha = 0.95f),
        radius = radius * 0.14f,
        center = Offset(x + radius * 0.5f, y + radius * 0.32f)
    )
}

private fun DrawScope.drawLowPriorityEventDiamond(
    x: Float,
    y: Float,
    radius: Float
) {
    val diamondPath = Path().apply {
        moveTo(x, y - radius)
        lineTo(x + radius, y)
        lineTo(x, y + radius)
        lineTo(x - radius, y)
        close()
    }

    drawPath(
        path = diamondPath,
        color = Color(0xFFFFA726)
    )

    drawPath(
        path = diamondPath,
        color = Color.White.copy(alpha = 0.45f),
        style = Stroke(width = 2.2f)
    )

    drawCircle(
        color = Color.White,
        radius = radius * 0.25f,
        center = Offset(x, y)
    )
}

private fun DrawScope.drawHighPriorityEventPin(
    x: Float,
    y: Float,
    radius: Float
) {
    val pinPath = Path().apply {
        moveTo(x, y - radius)

        cubicTo(
            x + radius,
            y - radius,
            x + radius * 1.05f,
            y + radius * 0.35f,
            x,
            y + radius * 1.55f
        )

        cubicTo(
            x - radius * 1.05f,
            y + radius * 0.35f,
            x - radius,
            y - radius,
            x,
            y - radius
        )

        close()
    }

    drawPath(
        path = pinPath,
        color = Color(0xFFFF6F00)
    )

    drawPath(
        path = pinPath,
        color = Color.White.copy(alpha = 0.35f),
        style = Stroke(width = 2.2f)
    )

    drawCircle(
        color = Color.White,
        radius = radius * 0.34f,
        center = Offset(x, y - radius * 0.05f)
    )
}