package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun CreatePostHeaderArt(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val d = { value: Float -> value.dp.toPx() }
        val skyline = Color(0xFFB94300).copy(alpha = 0.30f)
        val window = Color(0xFFFFC078).copy(alpha = 0.55f)

        fun building(left: Float, top: Float, width: Float, height: Float, floors: Int) {
            val leftPx = d(left)
            val topPx = size.height - d(top) - d(height)
            val widthPx = d(width)
            val heightPx = d(height)
            drawRect(skyline, Offset(leftPx, topPx), Size(widthPx, heightPx))
            val windowWidth = d(3f)
            val windowHeight = d(4f)
            val gapX = d(5f)
            val gapY = d(8f)
            repeat(floors) { floor ->
                var x = leftPx + d(3f)
                val y = topPx + d(5f) + floor * gapY
                while (x + windowWidth < leftPx + widthPx - d(2f)) {
                    drawRect(window, Offset(x, y), Size(windowWidth, windowHeight))
                    x += gapX
                }
            }
        }

        // Campus skyline tucked into the lower right of the header.
        building(size.width / density - 55f, 0f, 23f, 39f, 3)
        building(size.width / density - 31f, 0f, 27f, 55f, 5)
        building(size.width / density - 4f, 0f, 25f, 31f, 2)
        drawRect(
            skyline,
            topLeft = Offset(size.width - d(34f), size.height - d(58f)),
            size = Size(d(2f), d(8f))
        )
        drawRect(
            skyline,
            topLeft = Offset(size.width - d(39f), size.height - d(62f)),
            size = Size(d(12f), d(3f))
        )

        val trail = Path().apply {
            moveTo(d(5f), d(51f))
            cubicTo(d(27f), d(21f), d(42f), d(61f), d(65f), d(40f))
            cubicTo(d(72f), d(28f), d(80f), d(42f), d(87f), d(29f))
        }
        drawPath(
            path = trail,
            color = Color.White.copy(alpha = 0.68f),
            style = Stroke(width = d(1.2f), pathEffect = PathEffect.dashPathEffect(floatArrayOf(d(2f), d(4f))))
        )

        val plane = Path().apply {
            moveTo(d(87f), d(19f))
            lineTo(d(111f), d(11f))
            lineTo(d(101f), d(35f))
            lineTo(d(96f), d(26f))
            lineTo(d(87f), d(19f))
            close()
            moveTo(d(96f), d(26f))
            lineTo(d(111f), d(11f))
            lineTo(d(98f), d(30f))
        }
        drawPath(path = plane, color = Color.White.copy(alpha = 0.86f), style = Stroke(width = d(1.4f)))
    }
}

