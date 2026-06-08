package com.example.campusconnect.feature.map

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

private const val MAP_IMAGE_WIDTH = 3000f
private const val MAP_IMAGE_HEIGHT = 3000f

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    markers: List<MarkerRenderData> = emptyList(),
    onMarkerClick: (String) -> Unit = {},
    onMapTap: (Float, Float) -> Unit = { _, _ -> }
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = modifier.fillMaxSize()) {

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

        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val bounds = calculateImageBounds(size.width, size.height)

            markers.forEach { marker ->
                val mapX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                val mapY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                val screenX = mapX * scale + offset.x
                val screenY = mapY * scale + offset.y

                drawMarker(
                    marker = marker,
                    x = screenX,
                    y = screenY,
                    zoom = scale
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                        offset += pan
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

private fun DrawScope.drawMarker(
    marker: MarkerRenderData,
    x: Float,
    y: Float,
    zoom: Float
) {
    val zoomBoost = (zoom - 1f).coerceIn(0f, 2f) * 2.5f
    val visualRadius = (marker.radius + zoomBoost).coerceIn(10f, 26f)
    val selectedRadius = visualRadius + 7f
    val labelTextSize = (24f + zoomBoost).coerceIn(24f, 30f)

    when (marker.type) {

        MarkerType.POI -> {
            drawCircle(
                color = Color(marker.color),
                radius = visualRadius,
                center = Offset(x, y)
            )

            drawContext.canvas.nativeCanvas.drawText(
                marker.label,
                x + visualRadius + 6f,
                y + 6f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = labelTextSize
                    isFakeBoldText = true
                }
            )
        }

        MarkerType.USER -> {
            val isFemale = marker.gender == "female"

            drawCircle(
                color = Color(marker.color),
                radius = visualRadius,
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x - 5f, y - 3f)
            )

            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(x + 5f, y - 3f)
            )

            if (isFemale) {
                drawCircle(
                    color = Color(0xFFFFC1E3),
                    radius = visualRadius + 4f,
                    center = Offset(x, y),
                    style = Stroke(width = 3f)
                )
            } else {
                drawLine(
                    color = Color.White,
                    start = Offset(x - 7f, y + 7f),
                    end = Offset(x + 7f, y + 7f),
                    strokeWidth = 3f
                )
            }
        }

        MarkerType.EVENT -> {
            val eventPath = Path().apply {
                moveTo(x, y - visualRadius)
                cubicTo(
                    x + visualRadius,
                    y - visualRadius,
                    x + visualRadius,
                    y + visualRadius / 2f,
                    x,
                    y + visualRadius * 1.5f
                )
                cubicTo(
                    x - visualRadius,
                    y + visualRadius / 2f,
                    x - visualRadius,
                    y - visualRadius,
                    x,
                    y - visualRadius
                )
                close()
            }

            drawPath(
                path = eventPath,
                color = Color(0xFFFF9800)
            )

            drawCircle(
                color = Color.White,
                radius = visualRadius / 3f,
                center = Offset(x, y)
            )
        }

        MarkerType.SHOP -> {
            val hutPath = Path().apply {
                moveTo(x, y - visualRadius)
                lineTo(x - visualRadius, y)
                lineTo(x - visualRadius, y + visualRadius)
                lineTo(x + visualRadius, y + visualRadius)
                lineTo(x + visualRadius, y)
                close()
            }

            drawPath(
                path = hutPath,
                color = Color(marker.color)
            )

            drawRect(
                color = Color(0xFFFFF3E0),
                topLeft = Offset(x - 4f, y + 4f),
                size = Size(8f, 10f)
            )
        }
    }

    if (marker.isSelected) {
        drawCircle(
            color = Color.White,
            radius = selectedRadius,
            center = Offset(x, y),
            style = Stroke(width = 3f)
        )
    }
}