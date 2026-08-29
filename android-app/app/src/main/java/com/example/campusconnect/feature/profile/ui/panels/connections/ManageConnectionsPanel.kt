package com.example.campusconnect.feature.profile.ui.panels.connections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.model.ConnectionRequest
import com.example.campusconnect.feature.profile.ui.components.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.example.campusconnect.core.components.AppAvatar

private enum class ManageTab {
    REQUESTS,
    INVITES
}

@Composable
fun ManageConnectionsPanel(
    incomingRequests: List<ConnectionRequest>,
    sentInvites: List<ConnectionRequest>,
    onAccept: (Int) -> Unit,
    onDecline: (Int) -> Unit,
    onCancelInvite: (Int) -> Unit
) {
    var selectedTab by remember {
        mutableStateOf(ManageTab.REQUESTS)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        TabRow(
            selectedTabIndex =
                if (selectedTab == ManageTab.REQUESTS) 0 else 1,
            containerColor = Color.White,
            contentColor = Orange,
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        positions[
                            if (selectedTab == ManageTab.REQUESTS) 0 else 1
                        ]
                    ),
                    color = Orange
                )
            }
        ) {

            Tab(
                selected = selectedTab == ManageTab.REQUESTS,
                onClick = {
                    selectedTab = ManageTab.REQUESTS
                },
                text = {
                    Text(
                        text = "Requests",
                        color =
                            if (selectedTab == ManageTab.REQUESTS)
                                Orange
                            else
                                TextMuted
                    )
                }
            )

            Tab(
                selected = selectedTab == ManageTab.INVITES,
                onClick = {
                    selectedTab = ManageTab.INVITES
                },
                text = {
                    Text(
                        text = "Invites",
                        color =
                            if (selectedTab == ManageTab.INVITES)
                                Orange
                            else
                                TextMuted
                    )
                }
            )
        }

        val list =
            if (selectedTab == ManageTab.REQUESTS)
                incomingRequests
            else
                sentInvites

        if (list.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(
                    text =
                        if (selectedTab == ManageTab.REQUESTS)
                            "No requests"
                        else
                            "No pending invites",
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = 12.dp,
                    vertical = 14.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                items(list, key = { it.userId }) { person ->

                    ProfileListCard(
                        title = person.fullName,
                        subtitle = "${person.course} • Year ${person.academicYear}",

                        leadingContent = {
                            AppAvatar(
                                entityId = person.userId,
                                displayName = person.fullName,
                                imageUrl = person.avatarUrl,
                                size = 44.dp
                            )
                        },

                        trailingContent = {

                            if (selectedTab == ManageTab.REQUESTS) {

                                Row(
                                    horizontalArrangement =
                                        Arrangement.spacedBy(6.dp)
                                ) {

                                    Button(
                                        onClick = {
                                            onAccept(person.userId)
                                        },
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors =
                                            ButtonDefaults.buttonColors(
                                                containerColor = Orange
                                            ),
                                        contentPadding =
                                            PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            "Accept",
                                            fontSize = 10.sp,
                                            fontWeight =
                                                FontWeight.Medium
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            onDecline(person.userId)
                                        },
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding =
                                            PaddingValues(horizontal = 10.dp)
                                    ) {
                                        Text(
                                            "Decline",
                                            fontSize = 10.sp
                                        )
                                    }
                                }

                            } else {

                                OutlinedButton(
                                    onClick = {
                                        onCancelInvite(person.userId)
                                    },
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors =
                                        ButtonDefaults.outlinedButtonColors(
                                            contentColor =
                                                MaterialTheme.colorScheme.error
                                        ),
                                    border =
                                        ButtonDefaults.outlinedButtonBorder
                                ) {
                                    Text(
                                        "Pending",
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

