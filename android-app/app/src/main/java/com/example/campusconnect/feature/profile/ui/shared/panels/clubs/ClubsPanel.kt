package com.example.campusconnect.feature.profile.ui.shared.panels.clubs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.Club
import com.example.campusconnect.model.ClubStatus
import com.example.campusconnect.feature.profile.ui.shared.*

/**
 * Clubs panel used by both MyProfile (isOwner = true) and ViewProfile (isOwner = false).
 *
 * When isOwner = true  → search bar with embedded plus, Joined / Leave flow.
 * When isOwner = false → search bar without plus, Join / Pending flow.
 */
@Composable
fun ClubsPanel(
    clubs: List<Club>,
    isOwner: Boolean,
    onStatusChange: (index: Int, newStatus: ClubStatus) -> Unit
) {
    var query by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        PanelSearchBar(
            value = query,
            onValueChange = { query = it },
            placeholder = if (isOwner) "Search clubs to join…" else "Search clubs…",
            showEmbeddedPlus = isOwner
        )

        clubs
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .forEachIndexed { index, c ->
                ProfileListCard(
                    title = c.name,
                    subtitle = c.members,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(c.iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Groups,
                                contentDescription = null,
                                tint = c.iconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    },
                    trailingContent = {
                        ClubButton(
                            status = c.status,
                            onClick = {
                                when (c.status) {
                                    ClubStatus.JOIN    -> onStatusChange(index, ClubStatus.PENDING)
                                    ClubStatus.PENDING -> Unit // no action
                                    ClubStatus.JOINED  -> Unit // leave popup later
                                }
                            }
                        )
                    }
                )
            }
    }
}

@Composable
private fun ClubButton(status: ClubStatus, onClick: () -> Unit) {
    val containerColor = when (status) {
        ClubStatus.JOIN    -> Orange
        ClubStatus.PENDING -> OrangeLight
        ClubStatus.JOINED  -> Orange
    }
    val contentColor = when (status) {
        ClubStatus.JOIN    -> Color.White
        ClubStatus.PENDING -> OrangeDark
        ClubStatus.JOINED  -> Color.White
    }
    val label = when (status) {
        ClubStatus.JOIN    -> "Join"
        ClubStatus.PENDING -> "Pending"
        ClubStatus.JOINED  -> "Joined"
    }

    Button(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
