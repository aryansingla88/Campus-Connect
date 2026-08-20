package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.model.MapPoiInfo

private val OrangePrimary = Color(0xFFFF5F00)
private val TextDark = Color(0xFF202020)
private val TextMuted = Color(0xFF8A929A)
private val BorderGrey = Color(0xFFB4BAC0)
private val HandleGrey = Color(0xFFB7B7B7)



@Composable
fun PoiMarkerDialog(
    poi: MapPoiInfo,
    modifier: Modifier = Modifier,
    onNavigateClick: () -> Unit = {},
    onCloseClick: () -> Unit = {}
) {
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
                .height(285.dp),
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
                    .fillMaxSize()
                    .padding(
                        start = 26.dp,
                        end = 24.dp,
                        top = 8.dp,
                        bottom = 16.dp
                    )
            ) {
                PoiTopHandle()

                Spacer(modifier = Modifier.height(16.dp))

                PoiHeader(
                    title = poi.name,
                    category = poi.category // Modified: Replaced type with category
                )

                Spacer(modifier = Modifier.height(16.dp))

                PoiDescription(
                    description = limitWords(
                        text = poi.description.orEmpty(), // Modified: Handled nullable description
                        wordLimit = 24
                    ),
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                PoiActions(
                    onNavigateClick = onNavigateClick,
                    onCloseClick = onCloseClick
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
                .width(64.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(HandleGrey)
        )
    }
}

@Composable
private fun PoiHeader(
    title: String,
    category: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextDark,
            fontSize = 30.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = "Type:${category.lowercase()}", // Uses category while keeping UI text format
            color = TextMuted,
            fontSize = 17.sp,
            lineHeight = 19.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            modifier = Modifier.padding(top = 3.dp)
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
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        maxLines = 4,
        overflow = TextOverflow.Clip,
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PoiActions(
    onNavigateClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        NavigateButton(
            modifier = Modifier
                .width(220.dp)
                .height(50.dp),
            onClick = onNavigateClick
        )

        Spacer(modifier = Modifier.width(34.dp))

        CloseButton(
            modifier = Modifier.size(46.dp),
            onClick = onCloseClick
        )
    }
}

@Composable
private fun NavigateButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(14.dp)
            )
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = RoundedCornerShape(14.dp)
            )
            .background(Color.White)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 26.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Navigate",
                color = OrangePrimary,
                fontSize = 22.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Default.NearMe,
                contentDescription = "Navigate",
                tint = OrangePrimary,
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer {
                        rotationZ = -12f
                    }
            )
        }
    }
}

@Composable
private fun CloseButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = CircleShape
            )
            .clip(CircleShape)
            .background(OrangePrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}