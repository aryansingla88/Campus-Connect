package com.example.campusconnect.feature.map.components.markerdialogs

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.data.fake.FakeMapUserProfileService
import com.example.campusconnect.feature.map.model.MapMedal

private val OrangePrimary = Color(0xFFFF6F00)
private val OrangeLight = Color(0xFFFFF3E0)
private val TextDark = Color(0xFF202124)
private val TextMuted = Color(0xFF8A8A8A)
private val PurpleAccent = Color(0xFF7E57FF)

@Composable
fun UserMarkerDialog(
    userId: String,
    onDismiss: () -> Unit,
    onAddFriend: (String) -> Unit
) {
    val profile = remember(userId) {
        FakeMapUserProfileService.getUserProfile(userId)
    }

    val noRipple = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.04f))
            .clickable(
                interactionSource = noRipple,
                indication = null
            ) {
                onDismiss()
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .height(214.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) {
                    // card ke andar click se dismiss nahi hoga
                }
        ) {
            // Reference jaisa top bubble.
            // Ye Card se pehle draw ho raha hai, isliye card ke peeche/attached feel aayega.
            InterestHintBubble(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 88.dp)
                    .offset(y = (-48).dp)
            )

            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.97f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 14.dp)
                ) {
                    AddFriendIconButton(
                        modifier = Modifier.align(Alignment.TopEnd),
                        onClick = { onAddFriend(profile.userId) }
                    )

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // LEFT: avatar + badges/medals
                        Column(
                            modifier = Modifier.width(118.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            ProfileAvatar(
                                name = profile.fullName,
                                modifier = Modifier.size(86.dp)
                            )

                            Spacer(modifier = Modifier.height(9.dp))

                            CompactAchievementRow(
                                title = "Badges",
                                content = {
                                    profile.badges.take(3).forEach { badge ->
                                        BadgeChip(emoji = badge.emoji)
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(5.dp))

                            CompactAchievementRow(
                                title = "Medals",
                                content = {
                                    profile.medals.take(3).forEach { medal ->
                                        MedalChip(medal = medal)
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // RIGHT: user details
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 38.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = profile.fullName,
                                color = TextDark,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = profile.username,
                                color = PurpleAccent,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = "${profile.course} • ${profile.batch}",
                                color = TextDark.copy(alpha = 0.70f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(7.dp))

                            Text(
                                text = profile.description,
                                color = TextMuted,
                                fontSize = 13.sp,
                                lineHeight = 17.sp,
                                fontWeight = FontWeight.Medium,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InterestHintBubble(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(118.dp)
            .height(46.dp)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp),
            shape = RoundedCornerShape(12.dp),
            color = Color.White.copy(alpha = 0.97f),
            shadowElevation = 3.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Interest",
                    color = TextDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Canvas(
            modifier = Modifier
                .size(width = 18.dp, height = 10.dp)
                .align(Alignment.BottomCenter)
        ) {
            val path = Path().apply {
                moveTo(size.width / 2f, size.height)
                lineTo(0f, 0f)
                lineTo(size.width, 0f)
                close()
            }

            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.97f)
            )
        }
    }
}

@Composable
private fun CompactAchievementRow(
    title: String,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = TextMuted,
            fontSize = 8.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(38.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
private fun ProfileAvatar(
    name: String,
    modifier: Modifier = Modifier
) {
    val initials = remember(name) {
        name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
    }

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(OrangeLight)
            .border(
                width = 3.dp,
                color = OrangePrimary.copy(alpha = 0.35f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = OrangePrimary,
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun AddFriendIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .size(39.dp)
            .clip(CircleShape)
            .background(OrangePrimary)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.PersonAdd,
            contentDescription = "Add friend",
            tint = Color.White,
            modifier = Modifier.size(21.dp)
        )
    }
}

@Composable
private fun BadgeChip(
    emoji: String
) {
    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFEDE7F6), Color(0xFFD1C4E9))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun MedalChip(
    medal: MapMedal
) {
    val color = when (medal.rank) {
        1 -> Color(0xFFFFC107)
        2 -> Color(0xFFB0BEC5)
        3 -> Color(0xFFD9822B)
        else -> Color(0xFFE0E0E0)
    }

    Box(
        modifier = Modifier
            .size(21.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.95f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = medal.rank.toString(),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}