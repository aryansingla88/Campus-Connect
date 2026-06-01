package com.example.campusconnect.feature.map

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

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    markers: List<MarkerRenderData> = emptyList(),
    onMarkerClick: (String) -> Unit = {}
) {
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
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
            markers.forEach { marker ->

                when (marker.type) {

                    MarkerType.POI -> {
                        drawCircle(
                            color = Color(marker.color),
                            radius = marker.radius,
                            center = Offset(marker.x, marker.y)
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            marker.label,
                            marker.x + 12f,
                            marker.y + 6f,
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
                            center = Offset(marker.x, marker.y)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(marker.x - 5f, marker.y - 3f)
                        )

                        drawCircle(
                            color = Color.White,
                            radius = 5f,
                            center = Offset(marker.x + 5f, marker.y - 3f)
                        )

                        if (isFemale) {
                            drawCircle(
                                color = Color(0xFFFFC1E3),
                                radius = marker.radius + 5f,
                                center = Offset(marker.x, marker.y),
                                style = Stroke(width = 4f)
                            )
                        } else {
                            drawLine(
                                color = Color.White,
                                start = Offset(marker.x - 8f, marker.y + 8f),
                                end = Offset(marker.x + 8f, marker.y + 8f),
                                strokeWidth = 3f
                            )
                        }
                    }

                    MarkerType.EVENT -> {
                        val eventPath = Path().apply {
                            moveTo(marker.x, marker.y - marker.radius)

                            cubicTo(
                                marker.x + marker.radius,
                                marker.y - marker.radius,
                                marker.x + marker.radius,
                                marker.y + marker.radius / 2f,
                                marker.x,
                                marker.y + marker.radius * 1.5f
                            )

                            cubicTo(
                                marker.x - marker.radius,
                                marker.y + marker.radius / 2f,
                                marker.x - marker.radius,
                                marker.y - marker.radius,
                                marker.x,
                                marker.y - marker.radius
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
                            center = Offset(marker.x, marker.y)
                        )
                    }

                    MarkerType.SHOP -> {
                        val hutPath = Path().apply {
                            moveTo(marker.x, marker.y - marker.radius)
                            lineTo(marker.x - marker.radius, marker.y)
                            lineTo(marker.x - marker.radius, marker.y + marker.radius)
                            lineTo(marker.x + marker.radius, marker.y + marker.radius)
                            lineTo(marker.x + marker.radius, marker.y)
                            close()
                        }

                        drawPath(
                            path = hutPath,
                            color = Color(marker.color)
                        )

                        drawRect(
                            color = Color(0xFFFFF3E0),
                            topLeft = Offset(marker.x - 5f, marker.y + 4f),
                            size = Size(10f, 12f)
                        )
                    }
                }

                if (marker.isSelected) {
                    drawCircle(
                        color = Color.White,
                        radius = marker.radius + 8f,
                        center = Offset(marker.x, marker.y),
                        style = Stroke(width = 3f)
                    )
                }
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

                        val tappedMarker = markers.firstOrNull { marker ->

                            val markerScreenX = marker.x * scale + offset.x
                            val markerScreenY = marker.y * scale + offset.y

                            val dx = markerScreenX - tap.x
                            val dy = markerScreenY - tap.y

                            val distanceSq = dx.pow(2) + dy.pow(2)
                            val hitRadius = marker.radius * scale + 50f

                            distanceSq <= hitRadius.pow(2)
                        }

                        tappedMarker?.let {
                            onMarkerClick(it.id)
                        }
                    }
                }
        )
    }
}