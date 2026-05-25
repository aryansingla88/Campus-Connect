package com.example.campusconnect.feature.profile.ui.myprofile

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.ui.components.*
import com.example.campusconnect.feature.profile.ui.panels.clubs.ClubsPanel
import com.example.campusconnect.feature.profile.ui.panels.connections.ConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.connections.ManageConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.HonorPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.ManageCollectionPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.InterestsPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.ManageInterestsPanel
import com.example.campusconnect.feature.profile.viewmodel.MyProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    vm: MyProfileViewModel = viewModel()
) {
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
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            ProfileBottomBar(
                activePanel             = vm.activePanel,
                onEditProfile           = onEditProfile,
                onRequestsClick         = { vm.openManagePanel(StatPanel.CONNECTIONS) },
                onManageCollectionClick = { vm.openManagePanel(StatPanel.HONOR) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileHeader(
                initials    = vm.profile.initials,
                displayName = vm.profile.fullName,
                username    = vm.profile.username,
                bio         = vm.profile.bio,
                badgeColors = vm.badges.map { it.color },
                medalColors = vm.medals.map { it.color },
            )

            StatsRow(
                connectionCount = vm.connections.size,
                honorCount      = vm.badges.size + vm.medals.size,
                clubCount       = vm.clubs.count { it.status == ClubStatus.JOINED },
                interestCount   = vm.interests.size,
                activePanel     = vm.activePanel,
                onStatClick     = { vm.togglePanel(it) }
            )

            AnimatedContent(
                targetState = Pair(vm.activePanel, vm.activeManagePanel),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 10 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 10 })
                },
                label = "my_profile_panel"
            ) { (panel, managePanel) ->
                when {
                    managePanel == StatPanel.CONNECTIONS -> ManageConnectionsPanel()
                    managePanel == StatPanel.HONOR       -> ManageCollectionPanel()
                    managePanel == StatPanel.INTERESTS   -> ManageInterestsPanel()

                    panel == StatPanel.CONNECTIONS -> ConnectionsPanel(
                        connections    = vm.connections,
                        mode           = ProfileMode.OWN,
                        onStatusChange = { idx, status -> vm.connections[idx] = vm.connections[idx].copy(status = status) },
                        onConnectionClick = { userId -> onNavigateToProfile(userId) }
                    )
                    panel == StatPanel.HONOR -> HonorPanel(
                        honorRank    = vm.profile.honorRank,
                        badges       = vm.badges,
                        medals       = vm.medals,
                        honorEntries = vm.honorEntries,
                        mode         = ProfileMode.OWN
                    )
                    panel == StatPanel.CLUBS -> ClubsPanel(
                        clubs          = vm.clubs,
                        mode           = ProfileMode.OWN,
                        onStatusChange = { idx, status ->
                            vm.clubs[idx] = vm.clubs[idx].copy(status = status)
                        }
                    )
                    panel == StatPanel.INTERESTS -> InterestsPanel(
                        interests  = vm.interests,
                        mode       = ProfileMode.OWN,
                        onRemove   = { vm.interests.remove(it) },
                        onAddClick = { vm.openManagePanel(StatPanel.INTERESTS) }
                    )
                    else -> ProfileContent(
                        profile = vm.profile,
                        mode    = ProfileMode.OWN
                    )
                }
            }
        }
    }
}

