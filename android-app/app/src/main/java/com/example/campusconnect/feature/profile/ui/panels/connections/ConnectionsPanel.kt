package com.example.campusconnect.feature.profile.ui.panels.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.ui.components.*
import com.example.campusconnect.core.components.AppAvatar

@Composable
fun ConnectionsPanel(
    connections: List<Connection>,
    mode: ProfileMode,
    onStatusChange: (index: Int, newStatus: ConnectionStatus) -> Unit,
    onConnectionClick: (userId: Int) -> Unit = {}
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
            placeholder      = if (mode == ProfileMode.OWN) "Search people to connect…" else "Search connections…",
            showEmbeddedPlus = mode == ProfileMode.OWN
        )

        connections
            .filter { query.isBlank() || it.fullName.contains(query, ignoreCase = true) }
            .forEachIndexed { index, c ->
                ProfileListCard(
                    title = c.fullName,
                    subtitle = "${c.course} • Year ${c.academicYear}",
                    onClick  = { onConnectionClick(c.userId) },
                    leadingContent = {
                        AppAvatar(
                            entityId = c.userId,
                            displayName = c.fullName,
                            imageUrl = c.avatarUrl,
                            size = 38.dp
                        )},
                    trailingContent = {
                        ConnectionButton(
                            status  = c.status,
                            mode    = mode,
                            onClick = {
                                when (c.status) {
                                    ConnectionStatus.NOT_CONNECTED       -> onStatusChange(index, ConnectionStatus.PENDING)
                                    ConnectionStatus.PENDING   -> Unit
                                    ConnectionStatus.CONNECTED -> Unit
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
    mode: ProfileMode,
    onClick: () -> Unit
) {
    val containerColor = when (status) {
        ConnectionStatus.NOT_CONNECTED       -> Orange
        ConnectionStatus.PENDING   -> OrangeLight
        ConnectionStatus.CONNECTED -> Color.Transparent
    }
    val contentColor = when (status) {
        ConnectionStatus.NOT_CONNECTED       -> Color.White
        ConnectionStatus.PENDING   -> OrangeDark
        ConnectionStatus.CONNECTED -> TextMuted
    }
    val label = when {
        status == ConnectionStatus.NOT_CONNECTED     -> "Add"
        status == ConnectionStatus.PENDING -> "Pending"
        mode   == ProfileMode.OWN         -> "Remove"
        else                               -> "Connected"
    }

    Button(
        onClick        = onClick,
        modifier       = Modifier.height(28.dp),
        shape          = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
        colors         = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor   = contentColor
        ),
        border = if (status == ConnectionStatus.CONNECTED) ButtonDefaults.outlinedButtonBorder else null
    ) {
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium)
    }
}