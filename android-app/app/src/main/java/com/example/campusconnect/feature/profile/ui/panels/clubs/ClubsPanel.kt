package com.example.campusconnect.feature.profile.ui.panels.clubs

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
import com.example.campusconnect.core.components.AvatarShape
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.core.components.SearchPopup
import com.example.campusconnect.feature.profile.model.Club
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.ui.components.*

@Composable
fun ClubsPanel(
    clubs: List<Club>,
    mode: ProfileMode,
    allClubs: List<Club> = emptyList(),
    onJoinClub: (Int) -> Unit = {},
    onLeaveClub: (Int) -> Unit = {}
) {
    var showSearchPopup by remember {
        mutableStateOf(false)
    }

    var query by remember {
        mutableStateOf("")
    }

    val isOwnProfile = mode == ProfileMode.OWN

    val searchableClubs =
        if (isOwnProfile) {
            allClubs
        } else {
            clubs
        }

    val visibleClubs =
        if (query.isBlank()) {
            clubs
        } else {
            searchableClubs.filter { club ->
                club.name.contains(
                    other = query,
                    ignoreCase = true
                )
            }
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

        // Normal Clubs panel search bar.
        // It stays here; clicking it opens the common popup.
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            PanelSearchBar(
                value = query,
                onValueChange = {
                    query = it
                },
                placeholder =
                    if (isOwnProfile) {
                        "Search clubs to join…"
                    } else {
                        "Search clubs…"
                    },
                showEmbeddedPlus = isOwnProfile
            )

            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable {
                        showSearchPopup = true
                    }
            )
        }

        // Normal Clubs list
        visibleClubs.forEach { club ->

            ProfileListCard(
                title = club.name,
                subtitle = "${club.memberCount} members",

                leadingContent = {
                    AppAvatar(
                        entityId = club.clubId,
                        displayName = club.name,
                        imageUrl = club.logoUrl,
                        size = 38.dp,
                        shape = AvatarShape.ROUNDED
                    )
                },

                trailingContent = {
                    if (isOwnProfile) {
                        ClubButton(
                            status = club.status,
                            onClick = {
                                when (club.status) {

                                    ClubStatus.NOT_JOINED -> {
                                        onJoinClub(club.clubId)
                                    }

                                    ClubStatus.PENDING -> {
                                        // No action while request is pending.
                                    }

                                    ClubStatus.JOINED -> {
                                        onLeaveClub(club.clubId)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    /*
     * Common search popup
     */
    SearchPopup(
        visible = showSearchPopup,
        query = query,
        onQueryChange = {
            query = it
        },
        placeholder =
            if (isOwnProfile) {
                "Search clubs to join…"
            } else {
                "Search clubs…"
            },
        items =
            if (query.isBlank()) {
                searchableClubs
            } else {
                searchableClubs.filter { club ->
                    club.name.contains(
                        other = query,
                        ignoreCase = true
                    )
                }
            },
        onSearch = { },
        onDismiss = {
            showSearchPopup = false
            query = ""
        },
        itemContent = { club ->

            ProfileListCard(
                title = club.name,
                subtitle = "${club.memberCount} members",

                leadingContent = {
                    AppAvatar(
                        entityId = club.clubId,
                        displayName = club.name,
                        imageUrl = club.logoUrl,
                        size = 38.dp,
                        shape = AvatarShape.ROUNDED
                    )
                },

                trailingContent = {
                    if (isOwnProfile) {
                        ClubButton(
                            status = club.status,
                            onClick = {
                                when (club.status) {

                                    ClubStatus.NOT_JOINED -> {
                                        onJoinClub(club.clubId)
                                    }

                                    ClubStatus.PENDING -> {
                                        // No action while request is pending.
                                    }

                                    ClubStatus.JOINED -> {
                                        onLeaveClub(club.clubId)
                                    }
                                }
                            }
                        )
                    }
                }
            )
        }
    )
}

@Composable
private fun ClubButton(
    status: ClubStatus,
    onClick: () -> Unit
) {
    val containerColor = when (status) {
        ClubStatus.NOT_JOINED -> Orange
        ClubStatus.PENDING -> OrangeLight
        ClubStatus.JOINED -> Orange
    }

    val contentColor = when (status) {
        ClubStatus.NOT_JOINED -> Color.White
        ClubStatus.PENDING -> OrangeDark
        ClubStatus.JOINED -> Color.White
    }

    val label = when (status) {
        ClubStatus.NOT_JOINED -> "Join"
        ClubStatus.PENDING -> "Pending"
        ClubStatus.JOINED -> "Leave"
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
        )
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}