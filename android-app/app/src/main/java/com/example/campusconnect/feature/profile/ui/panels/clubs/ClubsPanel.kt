package com.example.campusconnect.feature.profile.ui.panels.clubs

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
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.ui.components.*
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.components.AvatarShape
@Composable
fun ClubsPanel(
    clubs: List<Club>,
    mode: ProfileMode,
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
            value            = query,
            onValueChange    = { query = it },
            placeholder      = if (mode == ProfileMode.OWN) "Search clubs to join…" else "Search clubs…",
            showEmbeddedPlus = mode == ProfileMode.OWN
        )

        clubs
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .forEachIndexed { index, c ->
                ProfileListCard(
                    title    = c.name,
                    subtitle = "${c.memberCount} members",
                    leadingContent = {
                        AppAvatar(
                            entityId = c.clubId,
                            displayName = c.name,
                            imageUrl = c.logoUrl,
                            size = 38.dp,
                            shape = AvatarShape.ROUNDED
                        )
                    },
                    trailingContent = {
                        ClubButton(
                            status  = c.status,
                            onClick = {
                                when (c.status) {
                                    ClubStatus. NOT_JOINED    -> onStatusChange(index, ClubStatus.PENDING)
                                    ClubStatus.PENDING -> Unit
                                    ClubStatus.JOINED  -> Unit
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
        ClubStatus. NOT_JOINED    -> Orange
        ClubStatus.PENDING -> OrangeLight
        ClubStatus.JOINED  -> Orange
    }
    val contentColor = when (status) {
        ClubStatus. NOT_JOINED    -> Color.White
        ClubStatus.PENDING -> OrangeDark
        ClubStatus.JOINED  -> Color.White
    }
    val label = when (status) {
        ClubStatus. NOT_JOINED    -> "Join"
        ClubStatus.PENDING -> "Pending"
        ClubStatus.JOINED  -> "Joined"
    }

    Button(
        onClick        = onClick,
        modifier       = Modifier.height(28.dp),
        shape          = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors         = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        )
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}