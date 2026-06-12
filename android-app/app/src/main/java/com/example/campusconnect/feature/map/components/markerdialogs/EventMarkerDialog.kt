package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.model.MapEventInfo

private val OrangePrimary = Color(0xFFFF6F00)
private val DarkOrange = Color(0xFFE65100)
private val LightOrange = Color(0xFFFFF3E0)
private val TextDark = Color(0xFF202020)
private val TextMuted = Color(0xFF6F7682)
private val BorderOrange = Color(0xFFFFCC80)

private val WhiteOrangeGradient = Brush.verticalGradient(
    colors = listOf(
        Color.White,
        Color(0xFFFFFBF7),
        LightOrange
    )
)

@Composable
fun EventMarkerDialog(
    event: MapEventInfo,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onNavigateClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.04f))
                .clickable { onDismiss() }
        )

        if (event.posterResId != null) {
            EventPosterCard(
                event = event,
                onNavigateClick = onNavigateClick,
                onRegisterClick = onRegisterClick
            )
        } else {
            EventDescriptionCard(
                event = event,
                onNavigateClick = onNavigateClick,
                onRegisterClick = onRegisterClick
            )
        }
    }
}

@Composable
private fun EventDescriptionCard(
    event: MapEventInfo,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .height(455.dp) // changed: button cut fix
            .border(
                width = 1.dp,
                color = BorderOrange.copy(alpha = 0.85f),
                shape = RoundedCornerShape(28.dp)
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(WhiteOrangeGradient)
        ) {
            NotifyButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        start = 26.dp,
                        end = 26.dp,
                        top = 32.dp,
                        bottom = 24.dp // changed
                    )
            ) {
                Text(
                    text = event.title.uppercase(),
                    color = DarkOrange,
                    fontSize = 31.sp, // changed
                    lineHeight = 35.sp, // changed
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = 64.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = event.hostName.uppercase(),
                    color = TextDark,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EventInfoPill(
                        text = "📅  ${event.date}",
                        modifier = Modifier.weight(1f)
                    )

                    EventInfoPill(
                        text = "⏰  ${event.time}",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = event.description,
                    color = TextMuted,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 5, // changed: button ke liye space fix
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                EventActionButtons(
                    onNavigateClick = onNavigateClick,
                    onRegisterClick = onRegisterClick
                )
            }
        }
    }
}

@Composable
private fun EventPosterCard(
    event: MapEventInfo,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(500.dp) // changed: poster only card
                .border(
                    width = 1.dp,
                    color = BorderOrange.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(28.dp)
                ),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = painterResource(id = event.posterResId!!),
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.04f),
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.08f)
                                )
                            )
                        )
                )

                NotifyButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp, end = 18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp)) // changed: buttons poster ke niche gap

        // changed: buttons direct blurred map ke upar, koi background nahi
        EventActionButtons(
            onNavigateClick = onNavigateClick,
            onRegisterClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun EventInfoPill(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.90f))
            .border(
                width = 1.dp,
                color = BorderOrange.copy(alpha = 0.75f),
                shape = RoundedCornerShape(20.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = DarkOrange,
            fontSize = 15.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

@Composable
private fun EventActionButtons(
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp), // changed: fixed button row height
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(OrangePrimary)
                .clickable { onNavigateClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Navigate",
                    color = Color.White,
                    fontSize = 16.sp, // changed
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Navigate",
                    tint = Color.White,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer {
                            rotationZ = -12f
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = BorderOrange,
                    shape = RoundedCornerShape(24.dp)
                )
                .clickable { onRegisterClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = "Register",
                    color = DarkOrange,
                    fontSize = 16.sp, // changed
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "📝",
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
private fun NotifyButton(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(45.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.96f))
            .border(
                width = 1.dp,
                color = BorderOrange,
                shape = RoundedCornerShape(14.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification",
            tint = DarkOrange,
            modifier = Modifier.size(24.dp)
        )
    }
}