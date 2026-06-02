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
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    transformOrigin = TransformOrigin(0f, 0f)
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
        ) {
            val bounds = calculateImageBounds(size.width, size.height)

            markers.forEach { marker ->
                val drawX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                val drawY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                drawMarker(
                    marker = marker,
                    x = drawX,
                    y = drawY
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
                            val drawX = bounds.left + (marker.x / MAP_IMAGE_WIDTH) * bounds.width
                            val drawY = bounds.top + (marker.y / MAP_IMAGE_HEIGHT) * bounds.height

                            val markerScreenX = drawX * scale + offset.x
                            val markerScreenY = drawY * scale + offset.y

                            val dx = markerScreenX - tap.x
                            val dy = markerScreenY - tap.y

                            val distanceSq = dx.pow(2) + dy.pow(2)
                            val hitRadius = marker.radius * scale + 50f

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
    y: Float
) {
    when (marker.type) {

        MarkerType.POI -> {
            drawCircle(
                color = Color(marker.color),
                radius = marker.radius,
                center = Offset(x, y)
            )

            drawContext.canvas.nativeCanvas.drawText(
                marker.label,
                x + 12f,
                y + 6f,
                android.graphics.Paint().apply {
                    color = android.graphics.Color.WHITE
                    textSize = 28f
                    isFakeBoldText = true
                }
            )
        }

        MarkerType.USER -> {
            val isFemale = marker.gender == "female"

            drawCircle(
                color = Color(marker.color),
                radius = marker.radius,
                center = Offset(x, y)
            )

            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(x - 5f, y - 3f)
            )

            drawCircle(
                color = Color.White,
                radius = 5f,
                center = Offset(x + 5f, y - 3f)
            )

            if (isFemale) {
                drawCircle(
                    color = Color(0xFFFFC1E3),
                    radius = marker.radius + 5f,
                    center = Offset(x, y),
                    style = Stroke(width = 4f)
                )
            } else {
                drawLine(
                    color = Color.White,
                    start = Offset(x - 8f, y + 8f),
                    end = Offset(x + 8f, y + 8f),
                    strokeWidth = 3f
                )
            }
        }

        MarkerType.EVENT -> {
            val eventPath = Path().apply {
                moveTo(x, y - marker.radius)
                cubicTo(
                    x + marker.radius,
                    y - marker.radius,
                    x + marker.radius,
                    y + marker.radius / 2f,
                    x,
                    y + marker.radius * 1.5f
                )
                cubicTo(
                    x - marker.radius,
                    y + marker.radius / 2f,
                    x - marker.radius,
                    y - marker.radius,
                    x,
                    y - marker.radius
                )
                close()
            }

            drawPath(
                path = eventPath,
                color = Color(0xFFFF9800)
            )

            drawCircle(
                color = Color.White,
                radius = marker.radius / 3f,
                center = Offset(x, y)
            )
        }

        MarkerType.SHOP -> {
            val hutPath = Path().apply {
                moveTo(x, y - marker.radius)
                lineTo(x - marker.radius, y)
                lineTo(x - marker.radius, y + marker.radius)
                lineTo(x + marker.radius, y + marker.radius)
                lineTo(x + marker.radius, y)
                close()
            }

            drawPath(
                path = hutPath,
                color = Color(marker.color)
            )

            drawRect(
                color = Color(0xFFFFF3E0),
                topLeft = Offset(x - 5f, y + 4f),
                size = Size(10f, 12f)
            )
        }
    }

    if (marker.isSelected) {
        drawCircle(
            color = Color.White,
            radius = marker.radius + 8f,
            center = Offset(x, y),
            style = Stroke(width = 3f)
        )
    }
}