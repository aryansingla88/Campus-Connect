package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.feature.map.model.HostInfo
import com.example.campusconnect.feature.map.model.MapEventInfo

private val OrangePrimary = Color(0xFFFF6F00)
private val DarkOrange = Color(0xFFE65100)
private val TextDark = Color(0xFF202020)
private val BorderOrange = Color(0xFFFFCC80)

@Composable
fun EventMarkerDialog(
    event: MapEventInfo,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onNavigateClick: (eventId: Int) -> Unit = {},  // Strictly Int
    onRegisterClick: (eventId: Int) -> Unit = {}   // Strictly Int
) {
    println("DEBUG_LOG: Event Poster URL = ${event.posterUrl}")
    println("DEBUG_LOG: Event Poster ResId = ${event.posterResId}")

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

        if (event.posterResId != null || !event.posterUrl.isNullOrBlank()) {
            EventPosterCard(
                event = event,
                onNavigateClick = { onNavigateClick(event.id) },
                onRegisterClick = { onRegisterClick(event.id) }
            )
        } else {
            EventDescriptionCard(
                event = event,
                onNavigateClick = { onNavigateClick(event.id) },
                onRegisterClick = { onRegisterClick(event.id) }
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
            .height(455.dp)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = event.title,
                    color = DarkOrange,
                    fontSize = 31.sp,
                    lineHeight = 35.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(10.dp))

                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SmallNotifyButton()

                    NavigateTopButton(
                        onClick = onNavigateClick
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                EventTimelineSection(
                    time = event.time,
                    date = event.date,
                    modifier = Modifier.width(128.dp)
                )

                Spacer(modifier = Modifier.width(14.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.description,
                        color = TextDark,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Location:",
                        color = TextDark,
                        fontSize = 15.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = event.venue ?: getEventLocation(event.id),
                        color = TextDark,
                        fontSize = 14.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HostsSection(hosts = event.hosts)

            Spacer(modifier = Modifier.height(14.dp))

            RegisterNowButton(
                isJoined = event.isJoined,
                onClick = onRegisterClick
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
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .height(500.dp)
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
                if (event.posterResId != null) {
                    Image(
                        painter = painterResource(id = event.posterResId),
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else if (!event.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }

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

        Spacer(modifier = Modifier.height(18.dp))

        EventActionButtons(
            isJoined = event.isJoined,
            onNavigateClick = onNavigateClick,
            onRegisterClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth(0.86f)
                .padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun SmallNotifyButton() {
    Box(
        modifier = Modifier
            .padding(top = 4.dp)
            .size(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(Color.White)
            .border(
                width = 1.dp,
                color = BorderOrange,
                shape = RoundedCornerShape(13.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = "Notification",
            tint = DarkOrange,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun NavigateTopButton(
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(OrangePrimary),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .border(
                        width = 2.dp,
                        color = Color.White,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "Navigate",
                    tint = Color.White,
                    modifier = Modifier
                        .size(27.dp)
                        .graphicsLayer {
                            rotationZ = -12f
                        }
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Navigate",
            color = DarkOrange,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun EventTimelineSection(
    time: String,
    date: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.height(128.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TimelineIcon(text = "⏰")

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(48.dp)
                    .background(OrangePrimary)
            )

            TimelineIcon(text = "📅")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Box(
                modifier = Modifier.height(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = time,
                    color = TextDark,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Box(
                modifier = Modifier.height(44.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = date,
                    color = TextDark,
                    fontSize = 18.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun TimelineIcon(text: String) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(OrangePrimary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HostsSection(hosts: List<HostInfo>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        Text(
            text = "Hosts",
            color = DarkOrange,
            fontSize = 22.sp,
            lineHeight = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(end = 100.dp)
        )

        Spacer(modifier = Modifier.height(9.dp))

        val bgColors = listOf(
            Color(0xFFB3E5FC),
            Color(0xFFC8E6C9),
            Color(0xFFFFECB3),
            Color(0xFFF8BBD0)
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(0.55f),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (hosts.isNotEmpty()) {
                    itemsIndexed(hosts) { index, host ->
                        ParticipantAvatar(
                            name = host.name,
                            initials = host.name.take(1).uppercase(),
                            avatarUrl = host.avatarUrl,
                            bg = bgColors[index % bgColors.size]
                        )
                    }
                } else {
                    val defaultHosts = listOf(
                        "Alex" to "A",
                        "Maria" to "M",
                        "Chen" to "C",
                        "Fatima" to "F"
                    )
                    itemsIndexed(defaultHosts) { index, (name, initial) ->
                        ParticipantAvatar(
                            name = name,
                            initials = initial,
                            avatarUrl = null,
                            bg = bgColors[index % bgColors.size]
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantAvatar(
    name: String,
    initials: String,
    avatarUrl: String? = null,
    bg: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!avatarUrl.isNullOrBlank()) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = TextDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            color = TextDark,
            fontSize = 11.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun RegisterNowButton(
    isJoined: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(OrangePrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isJoined) "Registered ✓" else "Register Now",
            color = Color.White,
            fontSize = 25.sp,
            lineHeight = 29.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun EventActionButtons(
    isJoined: Boolean,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
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
                    fontSize = 16.sp,
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
                .clip(RoundedCornerShape(25.dp))
                .background(Color.White.copy(alpha = 0.95f))
                .border(
                    width = 1.dp,
                    color = BorderOrange,
                    shape = RoundedCornerShape(25.dp)
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
                    text = if (isJoined) "Registered" else "Register",
                    color = DarkOrange,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (isJoined) "✓" else "📝",
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

// Fixed: Modified parameter type to Int
private fun getEventLocation(eventId: Int): String {
    return when (eventId) {
        1 -> "Student Center Ballrooms"
        2 -> "Coding Lab"
        else -> "Campus Auditorium"
    }
}