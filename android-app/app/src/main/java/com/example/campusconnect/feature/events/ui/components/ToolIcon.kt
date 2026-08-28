package com.example.campusconnect.feature.events.ui.components


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ToolIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFFFF4D00), Color(0xFFFF4D00))
                ),
                shape = CircleShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
fun MapViewButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = Color.Black.copy(alpha = 0.20f),
                spotColor = Color.Black.copy(alpha = 0.20f)
            )
            .clip(RoundedCornerShape(30.dp))
            .background(Color(0xFFFF4D00))
            .clickable { onClick() }
            .padding(
                horizontal = 18.dp,
                vertical = 11.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "Poster View",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Poster View",
            color = Color.White,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}


@Composable
fun CreateEventButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(
                elevation = 7.dp,
                shape = CircleShape,
                ambientColor = Color.Black.copy(alpha = 0.25f),
                spotColor = Color.Black.copy(alpha = 0.30f)
            )
            .clip(CircleShape)
            .background(Color(0xFFFF4D00))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {

        Canvas(
            modifier = Modifier.size(42.dp)
        ) {
            val white = Color.White
            val stroke = 4.dp.toPx()

            // Calendar outer body
            drawRoundRect(
                color = white,
                topLeft = Offset(
                    3.dp.toPx(),
                    9.dp.toPx()
                ),
                size = Size(
                    36.dp.toPx(),
                    29.dp.toPx()
                ),
                cornerRadius = CornerRadius(
                    5.dp.toPx(),
                    5.dp.toPx()
                )
            )

            // Orange inner area
            drawRoundRect(
                color = Color(0xFFFF4D00),
                topLeft = Offset(
                    7.dp.toPx(),
                    17.dp.toPx()
                ),
                size = Size(
                    28.dp.toPx(),
                    17.dp.toPx()
                ),
                cornerRadius = CornerRadius(
                    1.dp.toPx(),
                    1.dp.toPx()
                )
            )

            // Left binding
            drawRoundRect(
                color = white,
                topLeft = Offset(
                    10.dp.toPx(),
                    3.dp.toPx()
                ),
                size = Size(
                    6.dp.toPx(),
                    12.dp.toPx()
                ),
                cornerRadius = CornerRadius(
                    3.dp.toPx(),
                    3.dp.toPx()
                )
            )

            // Right binding
            drawRoundRect(
                color = white,
                topLeft = Offset(
                    26.dp.toPx(),
                    3.dp.toPx()
                ),
                size = Size(
                    6.dp.toPx(),
                    12.dp.toPx()
                ),
                cornerRadius = CornerRadius(
                    3.dp.toPx(),
                    3.dp.toPx()
                )
            )

            // Four white squares
            drawRoundRect(
                color = white,
                topLeft = Offset(9.dp.toPx(), 20.dp.toPx()),
                size = Size(5.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(0.8.dp.toPx())
            )

            drawRoundRect(
                color = white,
                topLeft = Offset(16.dp.toPx(), 20.dp.toPx()),
                size = Size(5.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(0.8.dp.toPx())
            )

            drawRoundRect(
                color = white,
                topLeft = Offset(9.dp.toPx(), 27.dp.toPx()),
                size = Size(5.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(0.8.dp.toPx())
            )

            drawRoundRect(
                color = white,
                topLeft = Offset(16.dp.toPx(), 27.dp.toPx()),
                size = Size(5.dp.toPx(), 5.dp.toPx()),
                cornerRadius = CornerRadius(0.8.dp.toPx())
            )

            // PLUS - vertical
            drawLine(
                color = white,
                start = Offset(
                    28.dp.toPx(),
                    21.5.dp.toPx()
                ),
                end = Offset(
                    28.dp.toPx(),
                    29.5.dp.toPx()
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )

// PLUS - horizontal
            drawLine(
                color = white,
                start = Offset(
                    24.dp.toPx(),
                    25.5.dp.toPx()
                ),
                end = Offset(
                    32.dp.toPx(),
                    25.5.dp.toPx()
                ),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}