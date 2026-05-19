package com.example.campusconnect.feature.profile.ui.viewprofile

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.profile.data.FakePublicProfileService
import com.example.campusconnect.model.StatPanel
import com.example.campusconnect.feature.profile.ui.shared.*
import com.example.campusconnect.feature.profile.ui.shared.panels.clubs.ClubsPanel
import com.example.campusconnect.feature.profile.ui.shared.panels.connections.ConnectionsPanel
import com.example.campusconnect.feature.profile.ui.shared.panels.honor.HonorPanel
import com.example.campusconnect.feature.profile.ui.shared.panels.interests.InterestsPanel
import com.example.campusconnect.feature.profile.viewmodel.ViewProfileViewModel

/**
 * Screen for viewing another user's profile.
 *
 * @param userId  The id of the user to display. Passed to [FakePublicProfileService] for now.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfileScreen(
    userId: String = "demo",
    onBack: () -> Unit = {},
    vm: ViewProfileViewModel = viewModel()
) {
    val profile     = remember(userId) { FakePublicProfileService.getProfile(userId) }
    val connections = remember { FakePublicProfileService.connections.toMutableStateList() }
    val clubs       = remember { FakePublicProfileService.clubs.toMutableStateList() }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            TopAppBar(
                title = {},
                modifier = Modifier.height(40.dp),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
        // No bottom bar on ViewProfile (no edit actions)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileHeader(
                initials    = profile.initials,
                displayName = profile.displayName,
                username    = profile.username,
                bio         = profile.bio,
                badgeColors = FakePublicProfileService.badges.map { it.color },
                medalColors = FakePublicProfileService.medals.map { it.color },
                headerAction = {
                    // Connect / Message button can be placed here when designed
                }
            )

            StatsRow(
                connectionCount = profile.connectionCount,
                honorCount      = FakePublicProfileService.badges.size + FakePublicProfileService.medals.size,
                clubCount       = profile.clubCount,
                interestCount   = profile.interestCount,
                activePanel     = vm.activePanel,
                onStatClick     = { vm.togglePanel(it) }
            )

            AnimatedContent(
                targetState = vm.activePanel,
                transitionSpec = {
                    fadeIn() + slideInVertically { it / 10 } togetherWith
                            fadeOut() + slideOutVertically { -it / 10 }
                },
                label = "view_profile_panel"
            ) { panel: StatPanel ->
                when (panel) {
                    StatPanel.CONNECTIONS -> ConnectionsPanel(
                        connections = connections,
                        isOwner = false,
                        onStatusChange = { idx, status -> connections[idx] = connections[idx].copy(status = status) }
                    )
                    StatPanel.HONOR      -> HonorPanel(
                        honorRank = profile.honorRank,
                        badges    = FakePublicProfileService.badges,
                        medals    = FakePublicProfileService.medals,
                        isOwner   = false
                    )
                    StatPanel.CLUBS      -> ClubsPanel(
                        clubs   = clubs,
                        isOwner = false,
                        onStatusChange = { idx, status -> clubs[idx] = clubs[idx].copy(status = status) }
                    )
                    StatPanel.INTERESTS  -> InterestsPanel(
                        interests = FakePublicProfileService.interests,
                        isOwner   = false
                    )
                    StatPanel.NONE       -> ViewProfileContent(profile = profile)
                }
            }
        }
    }
}