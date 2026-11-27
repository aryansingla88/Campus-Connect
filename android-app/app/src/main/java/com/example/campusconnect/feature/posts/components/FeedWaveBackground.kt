package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

internal val FeedWarmSurfaceColor = Color(0xFFFFF9F7)
private val FeedPeachWave = Color(0xFFFBE7DC)
private val FeedLightWave = Color(0xFFFFF0E9)

@Composable
fun FeedWaveBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(FeedWarmSurfaceColor)

        val width = size.width
        val waveHeight = 170.dp.toPx().coerceAtMost(size.height)

        val upperWave = Path().apply {
            moveTo(0f, 0f)
            lineTo(width, 0f)
            lineTo(width, waveHeight * 0.34f)
            cubicTo(
                width * 0.82f, waveHeight * 0.43f,
                width * 0.74f, waveHeight * 0.17f,
                width * 0.57f, waveHeight * 0.31f
            )
            cubicTo(
                width * 0.39f, waveHeight * 0.46f,
                width * 0.18f, waveHeight * 0.22f,
                0f, waveHeight * 0.51f
            )
            close()
        }
        drawPath(
            path = upperWave,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.44f to FeedLightWave.copy(alpha = 0.16f),
                    1.0f to FeedPeachWave.copy(alpha = 0.38f)
                ),
                start = Offset(0f, 0f),
                end = Offset(width, waveHeight * 0.66f)
            )
        )

        val lowerWave = Path().apply {
            moveTo(0f, waveHeight * 0.56f)
            cubicTo(
                width * 0.20f, waveHeight * 0.42f,
                width * 0.31f, waveHeight * 0.69f,
                width * 0.53f, waveHeight * 0.52f
            )
            cubicTo(
                width * 0.75f, waveHeight * 0.36f,
                width * 0.87f, waveHeight * 0.64f,
                width, waveHeight * 0.49f
            )
            lineTo(width, waveHeight * 0.61f)
            cubicTo(
                width * 0.82f, waveHeight * 0.77f,
                width * 0.68f, waveHeight * 0.54f,
                width * 0.52f, waveHeight * 0.67f
            )
            cubicTo(
                width * 0.30f, waveHeight * 0.83f,
                width * 0.17f, waveHeight * 0.57f,
                0f, waveHeight * 0.74f
            )
            close()
        }
        drawPath(
            path = lowerWave,
            brush = Brush.linearGradient(
                colorStops = arrayOf(
                    0.0f to Color.Transparent,
                    0.55f to FeedLightWave.copy(alpha = 0.10f),
                    1.0f to FeedPeachWave.copy(alpha = 0.20f)
                ),
                start = Offset(0f, waveHeight * 0.45f),
                end = Offset(width, waveHeight * 0.70f)
            )
        )
    }
}
