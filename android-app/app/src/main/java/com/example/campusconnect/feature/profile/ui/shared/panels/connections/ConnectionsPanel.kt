package com.example.campusconnect.feature.profile.ui.shared.panels.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.model.Connection
import com.example.campusconnect.model.ConnectionStatus
import com.example.campusconnect.feature.profile.ui.shared.*

/**
 * Connections panel used by both MyProfile (isOwner = true) and ViewProfile (isOwner = false).
 *
 * When isOwner = true  → search bar with embedded plus, Remove button for CONNECTED.
 * When isOwner = false → search bar without plus, Add/Pending button for each person.
 */
@Composable
fun ConnectionsPanel(
    connections: List<Connection>,
    isOwner: Boolean,
    onStatusChange: (index: Int, newStatus: ConnectionStatus) -> Unit
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
            placeholder = if (isOwner) "Search people to connect…" else "Search connections…",
            showEmbeddedPlus = isOwner
        )

        connections
            .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
            .forEachIndexed { index, c ->
                ProfileListCard(
                    title = c.name,
                    subtitle = c.sub,
                    leadingContent = {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(c.avatarBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                c.initials,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.avatarText
                            )
                        }
                    },
                    trailingContent = {
                        ConnectionButton(
                            status = c.status,
                            isOwner = isOwner,
                            onClick = {
                                when (c.status) {
                                    ConnectionStatus.ADD       -> onStatusChange(index, ConnectionStatus.PENDING)
                                    ConnectionStatus.PENDING   -> Unit // no action
                                    ConnectionStatus.CONNECTED -> Unit // remove popup later
                                }
                            }
                        )
                    }
                )
            }
    }
}

@Composable
private fun ConnectionButton(
    status: ConnectionStatus,
    isOwner: Boolean,
    onClick: () -> Unit
) {
    val containerColor = when (status) {
        ConnectionStatus.ADD       -> Orange
        ConnectionStatus.PENDING   -> OrangeLight
        ConnectionStatus.CONNECTED -> Color.Transparent
    }
    val contentColor = when (status) {
        ConnectionStatus.ADD       -> Color.White
        ConnectionStatus.PENDING   -> OrangeDark
        ConnectionStatus.CONNECTED -> TextMuted
    }
    val label = when {
        status == ConnectionStatus.ADD       -> "Add"
        status == ConnectionStatus.PENDING   -> "Pending"
        isOwner                              -> "Remove"
        else                                 -> "Connected"
    }

    Button(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border = if (status == ConnectionStatus.CONNECTED) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}
