package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

private val NotifyOrange = Color(0xFFFF8A1C)
private val IconLightOrange = Color(0xFFFFB099)
private val TextDark = Color(0xFF202020)

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
            .height(400.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF090D18)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF171B2A),
                            Color(0xFF090D18),
                            Color(0xFF050812)
                        )
                    )
                )
        ) {
            NotifyButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(start = 26.dp, end = 26.dp, top = 28.dp, bottom = 26.dp)
            ) {
                Text(
                    text = event.title.uppercase(),
                    color = Color.White,
                    fontSize = 34.sp,
                    lineHeight = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = event.hostName.uppercase(),
                        color = Color.White,
                        fontSize = 18.sp,
                        lineHeight = 23.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier.width(112.dp)
                    ) {
                        Text(
                            text = "📅  ${event.date}",
                            color = Color.White,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )

                        Text(
                            text = "⏰  ${event.time}",
                            color = Color.White,
                            fontSize = 18.sp,
                            lineHeight = 24.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = event.description,
                    color = Color.White,
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(28.dp))

                PosterActionButtons(
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
    Card(
        modifier = Modifier
            .fillMaxWidth(0.86f)
            .height(590.dp), // same height rakhi hai
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF090D18)
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
                                Color.Black.copy(alpha = 0.02f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.24f),
                                Color.Black.copy(alpha = 0.48f)
                            )
                        )
                    )
            )

            NotifyButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 18.dp, end = 18.dp)
            )

            PosterActionButtons(
                onNavigateClick = onNavigateClick,
                onRegisterClick = onRegisterClick,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 26.dp, end = 26.dp, bottom = 26.dp)
            )
        }
    }
}

@Composable
private fun PosterActionButtons(
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.26f))
                .clickable { onNavigateClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Navigate",
                    color = NotifyOrange,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Navigate",
                    tint = IconLightOrange,
                    modifier = Modifier
                        .size(22.dp)
                        .graphicsLayer {
                            rotationZ = -12f
                        }
                )
            }
        }

        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White.copy(alpha = 0.26f))
                .clickable { onRegisterClick() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Register",
                    color = NotifyOrange,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "📝",
                    color = IconLightOrange,
                    fontSize = 18.sp
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