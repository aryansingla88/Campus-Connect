package com.example.campusconnect.feature.profile.ui.shared.panels.honor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.Honor
import com.example.campusconnect.feature.profile.ui.shared.*

/**
 * Honor panel for both MyProfile (isOwner = true) and ViewProfile (isOwner = false).
 *
 * @param honorRank    The displayed rank number (e.g. 2 → "#2").
 * @param badges       List of badge [Honor] items.
 * @param medals       List of medal [Honor] items.
 * @param isOwner      When true a "Manage Collection" CTA is shown in the bottom bar
 *                     (handled by the parent screen); the panel itself is identical.
 */
@Composable
fun HonorPanel(
    honorRank: Int,
    badges: List<Honor>,
    medals: List<Honor>,
    isOwner: Boolean = false,
    onHonorBoardClick: () -> Unit = {}
) {
    var showAllBadges by remember { mutableStateOf(false) }
    var showAllMedals by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // ── Rank + Honor Board button ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("HONOR RANK", fontSize = 10.sp, color = TextMuted, letterSpacing = 0.5.sp)
                Text("#$honorRank", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Orange)
            }
            TextButton(onClick = onHonorBoardClick) {
                Icon(Icons.Outlined.EmojiEvents, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Honor Board")
            }
        }

        HorizontalDivider(color = DividerColor, thickness = 1.dp)

        // ── Badges ─────────────────────────────────────────────────────────────
        HonorSection(
            title = "BADGES",
            items = badges,
            showAll = showAllBadges,
            onToggle = { showAllBadges = !showAllBadges }
        ) { badge ->
            ProfileListCard(
                title = badge.title,
                subtitle = badge.subtitle,
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(badge.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(badge.color)
                        )
                    }
                }
            )
        }

        // ── Medals ─────────────────────────────────────────────────────────────
        HonorSection(
            title = "MEDALS",
            items = medals,
            showAll = showAllMedals,
            onToggle = { showAllMedals = !showAllMedals }
        ) { medal ->
            ProfileListCard(
                title = medal.title,
                subtitle = medal.subtitle,
                leadingContent = {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(medal.color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Outlined.WorkspacePremium,
                            contentDescription = null,
                            tint = medal.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun HonorSection(
    title: String,
    items: List<Honor>,
    showAll: Boolean,
    onToggle: () -> Unit,
    itemContent: @Composable (Honor) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Orange, letterSpacing = 0.6.sp)
        IconButton(onClick = onToggle, modifier = Modifier.size(28.dp)) {
            Icon(
                if (showAll) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TextMuted
            )
        }
    }

    items.take(if (showAll) items.size else 3).forEach { item ->
        itemContent(item)
    }
}
