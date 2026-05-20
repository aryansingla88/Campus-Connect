package com.example.campusconnect.feature.profile.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.StatPanel

@Composable
fun StatsRow(
    connectionCount: Int,
    honorCount: Int,
    clubCount: Int,
    interestCount: Int,
    activePanel: StatPanel,
    onStatClick: (StatPanel) -> Unit
) {
    data class StatItem(
        val value: String,
        val label: String,
        val icon: ImageVector,
        val panel: StatPanel
    )

    val stats = listOf(
        StatItem(connectionCount.toString(), "Connections", Icons.Outlined.People,      StatPanel.CONNECTIONS),
        StatItem(honorCount.toString(),      "Honor",       Icons.Outlined.EmojiEvents, StatPanel.HONOR),
        StatItem(clubCount.toString(),       "Clubs",       Icons.Outlined.Shield,      StatPanel.CLUBS),
        StatItem(interestCount.toString(),   "Interests",   Icons.Outlined.Star,        StatPanel.INTERESTS),
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp)
            .offset(y = (-3).dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            stats.forEachIndexed { index, stat ->
                val isActive = activePanel == stat.panel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onStatClick(stat.panel) }
                            .background(if (isActive) OrangeLight else CardBg)
                            .padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = stat.icon,
                            contentDescription = stat.label,
                            tint = if (isActive) Orange else TextMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            stat.value,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) Orange else TextPrimary
                        )
                        Text(
                            stat.label,
                            fontSize = 9.sp,
                            color = if (isActive) Orange else TextMuted,
                            letterSpacing = 0.4.sp
                        )
                    }

                    // Bottom indicator bar
                    if (isActive) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth(0.5f)
                                .height(3.dp)
                                .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                                .background(Orange)
                        )
                    }

                    // Right divider
                    if (index < stats.lastIndex) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(1.dp)
                                .fillMaxHeight(0.5f)
                                .background(DividerColor)
                        )
                    }
                }
            }
        }
    }

    Spacer(Modifier.height(6.dp))
}
