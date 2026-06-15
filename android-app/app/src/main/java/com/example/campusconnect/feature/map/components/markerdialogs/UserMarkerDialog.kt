package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.model.MapUserProfile

private val OrangePrimary = Color(0xFFFF6F00)
private val AvatarBg = Color(0xFFF4ECD9)
private val TextDark = Color(0xFF222222)
private val TextMuted = Color(0xFF6F6F6F)
private val DividerColor = Color(0xFFBDBDBD)

@Composable
fun UserMarkerDialog(
    profile: MapUserProfile,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit = {},
    onAddFriendClick: () -> Unit = {}
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Light dim layer. Blur is handled in MapScreen.kt
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.05f))
                .clickable { onDismiss() }
        )

        GrainOverlay(
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            MutualsPill(
                count = profile.mutualFriendsCount,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 52.dp)
                    .offset(y = (-32).dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(AvatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.fullName
                                    .split(" ")
                                    .take(2)
                                    .mapNotNull { it.firstOrNull()?.toString() }
                                    .joinToString(""),
                                color = OrangePrimary,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(top = 10.dp)
                        ) {
                            Text(
                                text = profile.fullName,
                                color = TextDark,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "${profile.course} • ${profile.startYear}-${profile.endYear}",
                                color = TextDark,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .padding(top = 8.dp)
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(OrangePrimary)
                                .clickable { onAddFriendClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = "Add Friend",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = profile.description,
                        color = TextDark,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    AchievementSection(
                        badges = profile.badges,
                        medals = profile.medals
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementSection(
    badges: List<String>,
    medals: List<Int>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AchievementColumn(
            title = "Badges",
            modifier = Modifier.width(126.dp)
        ) {
            badges.forEach { badge ->
                BadgeChip(text = badge)
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Box(
            modifier = Modifier
                .width(1.dp)
                .height(44.dp)
                .background(DividerColor.copy(alpha = 0.65f))
        )

        Spacer(modifier = Modifier.width(16.dp))

        AchievementColumn(
            title = "Medals",
            modifier = Modifier.width(126.dp)
        ) {
            medals.forEach { medal ->
                MedalChip(number = medal)
            }
        }
    }
}

@Composable
private fun AchievementColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(7.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(
                space = 10.dp,
                alignment = Alignment.CenterHorizontally
            ),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun MutualsPill(
    count: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(132.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(topStart = 13.dp, topEnd = 13.dp))
            .background(Color.White.copy(alpha = 0.94f)),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = "$count Mutuals",
            color = TextDark,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun BadgeChip(text: String) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(Color(0xFFF1EBFF)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun MedalChip(number: Int) {
    val medalColor = when (number) {
        1 -> Color(0xFFF2C23A)
        2 -> Color(0xFFB8C2C8)
        else -> Color(0xFFC9873D)
    }

    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape)
            .background(medalColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = number.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Composable
private fun GrainOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val step = 18f

        var y = 0f
        while (y < size.height) {
            var x = 0f
            while (x < size.width) {
                val seed = ((x.toInt() * 31 + y.toInt() * 17) % 7)

                if (seed == 0) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.045f),
                        radius = 0.9f,
                        center = Offset(x, y)
                    )
                }

                x += step
            }
            y += step
        }
    }
}