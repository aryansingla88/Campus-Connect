package com.example.campusconnect.feature.map.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.SportsTennis
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.model.MapPoiInfo
import kotlinx.coroutines.launch

private val OrangePrimary = Color(0xFFFF5F00)
private val ChipBackground = Color(0xFFFFE3D1)
private val TextDark = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF8A929A)
private val HandleGrey = Color(0xFFD6D6D6)

// How far (in dp) the sheet must be dragged down before it counts as a "close" swipe.
private val DismissDragThreshold = 110.dp

@Composable
fun PoiMarkerDialog(
    poi: MapPoiInfo,
    modifier: Modifier = Modifier,
    onNavigateClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val dismissThresholdPx = with(density) { DismissDragThreshold.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = 1.dp,
                end = 1.dp,
                top = 0.dp,
                bottom = 1.dp
            ),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .offset { androidx.compose.ui.unit.IntOffset(0, offsetY.value.toInt()) }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onVerticalDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                            scope.launch { offsetY.snapTo(newOffset) }
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offsetY.value > dismissThresholdPx) {
                                    // Finish the slide-down off screen, then notify caller.
                                    offsetY.animateTo(dismissThresholdPx * 4f)
                                    onCloseClick()
                                } else {
                                    offsetY.animateTo(0f)
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch { offsetY.animateTo(0f) }
                        }
                    )
                },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 0.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 26.dp,
                        end = 26.dp,
                        top = 8.dp,
                        bottom = 24.dp
                    )
            ) {
                PoiTopHandle()

                Spacer(modifier = Modifier.height(20.dp))

                PoiHeader(title = poi.name)

                Spacer(modifier = Modifier.height(10.dp))

                PoiCategoryChip(category = poi.category)

                Spacer(modifier = Modifier.height(16.dp))

                val displayDescription = if (poi.description.isNullOrBlank()) {
                    "Campus Point of Interest located on campus."
                } else {
                    poi.description
                }
                PoiDescription(
                    description = limitWords(
                        text = displayDescription,
                        wordLimit = 24
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                NavigateButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    onClick = onNavigateClick
                )
            }
        }
    }
}

private fun limitWords(
    text: String,
    wordLimit: Int
): String {
    val words = text.trim().split("\\s+".toRegex())

    return if (words.size <= wordLimit) {
        text
    } else {
        words.take(wordLimit).joinToString(" ") + "."
    }
}

@Composable
private fun PoiTopHandle() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(HandleGrey)
        )
    }
}

@Composable
private fun PoiHeader(title: String) {
    Text(
        text = title,
        color = TextDark,
        fontSize = 30.sp,
        lineHeight = 34.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PoiCategoryChip(category: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(ChipBackground)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SportsTennis,
            contentDescription = null,
            tint = OrangePrimary,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = category.replaceFirstChar { it.uppercase() },
            color = OrangePrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PoiDescription(
    description: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = description,
        color = TextMuted,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.Normal,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun NavigateButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(50)
            )
            .clip(RoundedCornerShape(50))
            .background(OrangePrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Navigate",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}