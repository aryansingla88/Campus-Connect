package com.example.campusconnect.feature.posts.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
private fun rememberGhostColor(): Color {
    val transition = rememberInfiniteTransition(label = "feed-skeleton")
    val color by transition.animateColor(
        initialValue = Color(0xFFF0F1F3),
        targetValue = Color(0xFFE5E7EA),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "feed-skeleton-pulse"
    )
    return color
}

@Composable
fun TopicChipGhost(
    modifier: Modifier = Modifier,
    width: Dp = 112.dp
) {
    val ghostColor = rememberGhostColor()
    Box(
        modifier = modifier
            .width(width)
            .height(40.dp)
            .clip(CircleShape)
            .background(ghostColor)
    )
}

@Composable
fun PostCardGhost(
    modifier: Modifier = Modifier,
    showImage: Boolean = true
) {
    val ghostColor = rememberGhostColor()
    val mutedGhost = ghostColor.copy(alpha = 0.68f)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE6E7EA), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    GhostBar(ghostColor, width = 92.dp, height = 14.dp)
                    GhostBar(mutedGhost, width = 66.dp, height = 12.dp)
                }
                Box(
                    Modifier
                        .width(84.dp)
                        .height(32.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
                Spacer(Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    repeat(3) {
                        Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(ghostColor)
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            GhostBar(ghostColor, width = 176.dp, height = 19.dp)
            Spacer(Modifier.height(12.dp))
            GhostBar(mutedGhost, Modifier.fillMaxWidth(), height = 15.dp)
            Spacer(Modifier.height(8.dp))
            GhostBar(mutedGhost, Modifier.fillMaxWidth(0.78f), height = 15.dp)

            if (showImage) {
                Spacer(Modifier.height(14.dp))
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(178.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(mutedGhost)
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .width(72.dp)
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    Modifier
                        .width(72.dp)
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .width(60.dp)
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
            }
        }
    }
}

@Composable
fun PostDetailCardGhost(
    isEventPost: Boolean,
    modifier: Modifier = Modifier
) {
    val ghostColor = rememberGhostColor()
    val mutedGhost = ghostColor.copy(alpha = 0.68f)

    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(1.dp, Color(0xFFE6E7EA), RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 12.dp)
        ) {
            DetailHeaderGhost(isEventPost, ghostColor, mutedGhost)

            if (!isEventPost) {
                Spacer(Modifier.height(16.dp))
                GhostBar(ghostColor, width = 176.dp, height = 20.dp)
            }

            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(mutedGhost)
            )

            if (isEventPost) {
                Spacer(Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    repeat(3) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(74.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFFFF7F1))
                                .padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            GhostBar(ghostColor, width = 36.dp, height = 10.dp)
                            GhostBar(mutedGhost, Modifier.fillMaxWidth(), height = 14.dp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            GhostBar(mutedGhost, Modifier.fillMaxWidth(), height = 15.dp)
            Spacer(Modifier.height(8.dp))
            GhostBar(mutedGhost, Modifier.fillMaxWidth(0.82f), height = 15.dp)
            Spacer(Modifier.height(6.dp))
            GhostBar(mutedGhost, Modifier.fillMaxWidth(0.56f), height = 15.dp)
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFECEDEF)))
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(2) {
                    Box(
                        Modifier
                            .width(72.dp)
                            .height(38.dp)
                            .clip(CircleShape)
                            .background(mutedGhost)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.weight(1f))
                Box(
                    Modifier
                        .width(60.dp)
                        .height(38.dp)
                        .clip(CircleShape)
                        .background(mutedGhost)
                )
            }
        }
    }
}

@Composable
private fun DetailHeaderGhost(
    isEventPost: Boolean,
    ghostColor: Color,
    mutedGhost: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(if (isEventPost) 42.dp else 48.dp)
                .clip(CircleShape)
                .background(mutedGhost)
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp, end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            GhostBar(ghostColor, width = if (isEventPost) 142.dp else 96.dp, height = 15.dp)
            GhostBar(mutedGhost, width = 72.dp, height = 12.dp)
        }
        Box(
            Modifier
                .width(76.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(mutedGhost)
        )
        Spacer(Modifier.width(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) {
                Box(
                    Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(ghostColor)
                )
            }
        }
    }
}

@Composable
fun CommentRowGhost(modifier: Modifier = Modifier) {
    val ghostColor = rememberGhostColor()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(ghostColor.copy(alpha = 0.68f))
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GhostBar(ghostColor, width = 94.dp, height = 13.dp)
                GhostBar(ghostColor.copy(alpha = 0.68f), width = 58.dp, height = 11.dp)
            }
            GhostBar(ghostColor.copy(alpha = 0.68f), Modifier.fillMaxWidth(), height = 14.dp)
            GhostBar(ghostColor.copy(alpha = 0.68f), Modifier.fillMaxWidth(0.64f), height = 14.dp)
        }
    }
}

@Composable
fun CommentsHeaderGhost(modifier: Modifier = Modifier) {
    val ghostColor = rememberGhostColor()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GhostBar(ghostColor, width = 82.dp, height = 16.dp)
    }
}

@Composable
private fun GhostBar(
    color: Color,
    modifier: Modifier = Modifier,
    width: Dp = Dp.Unspecified,
    height: Dp
) {
    Box(
        modifier = modifier
            .then(if (width != Dp.Unspecified) Modifier.width(width) else Modifier)
            .height(height)
            .clip(RoundedCornerShape(6.dp))
            .background(color)
    )
}
