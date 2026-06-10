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
import androidx.compose.ui.draw.shadow
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

private val OrangePrimary = Color(0xFFFF5F00)
private val NotifyOrange = Color(0xFFFF5F00)
private val TextDark = Color(0xFF202020)
private val TextMuted = Color(0xFF8A929A)
private val BorderGrey = Color(0xFFB4BAC0)

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
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(330.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 8.dp)
            ) {
                NotifyButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 0.dp, end = 0.dp)
                )

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = event.title,
                        color = TextDark,
                        fontSize = 25.sp, // changed: 32 se chota
                        lineHeight = 28.sp, // changed
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(end = 58.dp) // changed: notification ke liye space
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = event.hostName,
                            color = TextDark,
                            fontSize = 17.sp,
                            lineHeight = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier
                                .width(65.dp)
                                .padding(top = 10.dp) // changed: date ke upar space
                        ) {
                            Text(
                                text = event.date,
                                color = TextDark,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )

                            Text(
                                text = event.time,
                                color = TextDark,
                                fontSize = 14.sp,
                                lineHeight = 18.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text(
                        text = event.description,
                        color = TextDark,
                        fontSize = 15.sp, // changed: 20 se chota
                        lineHeight = 21.sp, // changed
                        fontFamily = FontFamily.Monospace, // changed: same UI feel
                        fontWeight = FontWeight.Bold, // changed
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            EventActionBar(
                onNavigateClick = onNavigateClick,
                onRegisterClick = onRegisterClick
            )
        }
    }
}

@Composable
private fun EventPosterCard(
    event: MapEventInfo,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .height(620.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Image(
                    painter = painterResource(id = event.posterResId!!),
                    contentDescription = event.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Fit
                )

                NotifyButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 18.dp, end = 20.dp)
                )
            }

            EventActionBar(
                onNavigateClick = onNavigateClick,
                onRegisterClick = onRegisterClick
            )
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
            .background(NotifyOrange),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun EventActionBar(
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .border(
                width = 1.dp,
                color = BorderGrey,
                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)
            )
            .padding(horizontal = 18.dp), // changed: 28 se 18
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        EventNavigateButton(
            modifier = Modifier
                .width(150.dp) // changed: fixed width
                .height(50.dp),
            onClick = onNavigateClick
        )

        Spacer(modifier = Modifier.width(26.dp)) // changed: 60 se 26

        EventRegisterButton(
            modifier = Modifier
                .width(150.dp) // changed: fixed width
                .height(50.dp),
            onClick = onRegisterClick
        )
    }
}

@Composable
private fun EventNavigateButton(
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
                .padding(start = 14.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Navigate",
                color = OrangePrimary,
                fontSize = 20.sp,
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
private fun EventRegisterButton(
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
                .padding(start = 14.dp, end = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Register",
                color = OrangePrimary,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "📝",
                color = OrangePrimary,
                fontSize = 20.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}