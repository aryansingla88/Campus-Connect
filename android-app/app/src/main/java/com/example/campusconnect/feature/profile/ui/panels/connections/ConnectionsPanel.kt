package com.example.campusconnect.feature.profile.ui.panels.connections

import androidx.compose.foundation.clickable
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
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.core.components.SearchPopup
import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.ui.components.*

@Composable
fun ConnectionsPanel(
    connections: List<Connection>,
    searchResults: List<Connection>,
    mode: ProfileMode,
    onSearch: (String) -> Unit,
    onStatusChange: (userId: Int, newStatus: ConnectionStatus) -> Unit,
    onRemoveConnection: (userId: Int) -> Unit = {},
    onConnectionClick: (userId: Int) -> Unit = {},
    onCancelConnectionRequest: (Int) -> Unit
) {
    var showSearchPopup by remember {
        mutableStateOf(false)
    }

    var query by remember {
        mutableStateOf("")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 12.dp,
                vertical = 14.dp
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        // Existing search bar.
        // It only opens the common search popup.
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            PanelSearchBar(
                value = "",
                onValueChange = {},
                placeholder =
                    if (mode == ProfileMode.OWN) {
                        "Search people to connect…"
                    } else {
                        "Search connections…"
                    },
                showEmbeddedPlus = mode == ProfileMode.OWN
            )

            Surface(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showSearchPopup = true
                    },
                color = Color.Transparent
            ) {}
        }

        // Normal Connections list
        connections.forEach { connection ->

            ProfileListCard(
                title = connection.fullName,
                subtitle =
                    "${connection.course} • ${connection.academicYear}",
                onClick = {
                    onConnectionClick(connection.userId)
                },
                leadingContent = {
                    AppAvatar(
                        entityId = connection.userId,
                        displayName = connection.fullName,
                        imageUrl = connection.avatarUrl,
                        size = 38.dp
                    )
                },
                trailingContent = {
                    ConnectionButton(
                        status = connection.status,
                        mode = mode,
                        onClick = {
                            when (connection.status) {

                                ConnectionStatus.NOT_CONNECTED -> {
                                    onStatusChange(
                                        connection.userId,
                                        ConnectionStatus.PENDING
                                    )
                                }

                                ConnectionStatus.PENDING -> {
                                    if (mode == ProfileMode.OWN) {
                                        onCancelConnectionRequest(
                                            connection.userId
                                        )
                                    }
                                }

                                ConnectionStatus.CONNECTED -> {
                                    if (mode == ProfileMode.OWN) {
                                        onRemoveConnection(
                                            connection.userId
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    }

    // Common search popup
    SearchPopup(
        visible = showSearchPopup,
        query = query,
        onQueryChange = {
            query = it
        },
        placeholder =
            if (mode == ProfileMode.OWN) {
                "Search people to connect…"
            } else {
                "Search connections…"
            },
        items =
            if (query.isBlank()) {
                connections
            } else {
                searchResults
            },
        onSearch = onSearch,
        onDismiss = {
            showSearchPopup = false
            query = ""
        },
        itemContent = { connection ->

            ProfileListCard(
                title = connection.fullName,
                subtitle =
                    "${connection.course} • ${connection.academicYear}",
                onClick = {
                    onConnectionClick(connection.userId)
                },
                leadingContent = {
                    AppAvatar(
                        entityId = connection.userId,
                        displayName = connection.fullName,
                        imageUrl = connection.avatarUrl,
                        size = 38.dp
                    )
                },
                trailingContent = {
                    ConnectionButton(
                        status = connection.status,
                        mode = mode,
                        onClick = {
                            when (connection.status) {

                                ConnectionStatus.NOT_CONNECTED -> {
                                    onStatusChange(
                                        connection.userId,
                                        ConnectionStatus.PENDING
                                    )
                                }

                                ConnectionStatus.PENDING -> {
                                    if (mode == ProfileMode.OWN) {
                                        onCancelConnectionRequest(
                                            connection.userId
                                        )
                                    }
                                }

                                ConnectionStatus.CONNECTED -> {
                                    if (mode == ProfileMode.OWN) {
                                        onRemoveConnection(
                                            connection.userId
                                        )
                                    }
                                }
                            }
                        }
                    )
                }
            )
        }
    )
}

@Composable
private fun ConnectionButton(
    status: ConnectionStatus,
    mode: ProfileMode,
    onClick: () -> Unit
) {
    val containerColor = when (status) {
        ConnectionStatus.NOT_CONNECTED -> Orange
        ConnectionStatus.PENDING -> OrangeLight
        ConnectionStatus.CONNECTED -> Color.Transparent
    }

    val contentColor = when (status) {
        ConnectionStatus.NOT_CONNECTED -> Color.White
        ConnectionStatus.PENDING -> OrangeDark
        ConnectionStatus.CONNECTED -> TextMuted
    }

    val label = when {
        status == ConnectionStatus.NOT_CONNECTED -> "Add"
        status == ConnectionStatus.PENDING -> "Pending"
        mode == ProfileMode.OWN -> "Remove"
        else -> "Connected"
    }

    Button(
        onClick = onClick,
        modifier = Modifier.height(28.dp),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(
            horizontal = 10.dp,
            vertical = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        border =
            if (status == ConnectionStatus.CONNECTED) {
                ButtonDefaults.outlinedButtonBorder
            } else {
                null
            }
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}