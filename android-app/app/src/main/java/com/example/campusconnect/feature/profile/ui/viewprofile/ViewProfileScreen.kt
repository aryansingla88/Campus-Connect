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
import android.app.Application
import androidx.compose.ui.platform.LocalContext

import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.ui.components.*
import com.example.campusconnect.feature.profile.ui.panels.connections.ConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.HonorPanel
import com.example.campusconnect.feature.profile.ui.panels.clubs.ClubsPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.InterestsPanel
import com.example.campusconnect.feature.profile.viewmodel.ViewProfileViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewProfileScreen(
    userId: Int = 22,
    onBack: () -> Unit = {}
) {

    val application =
        LocalContext.current.applicationContext as Application

    val vm: ViewProfileViewModel =
        viewModel(
            factory = ViewProfileViewModel.factory(
                application = application,
                userId = userId
            )
        )
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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileHeader(
                entityId = vm.profile.userId,
                avatarUrl = vm.profile.avatarUrl,
                displayName  = vm.profile.fullName,
                username     = vm.profile.username,
                bio          = vm.profile.bio,
                badgeColors = emptyList(),
                medalColors = emptyList(),
                headerAction = {
                    // Connect / Message button
                }
            )

            StatsRow(
                connectionCount = vm.connections.size,
                honorCount      = vm.badges.size + vm.medals.size,
                clubCount       = vm.clubs.size,
                interestCount   = vm.interests.size,
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
            ) { panel: StatPanel? ->
                when (panel) {
                    StatPanel.CONNECTIONS -> ConnectionsPanel(
                        connections    = vm.connections,
                        mode           = ProfileMode.VIEW,
                        onStatusChange = { idx, status ->
                            vm.connections[idx] = vm.connections[idx].copy(status = status)
                        }
                    )
                    StatPanel.HONOR -> HonorPanel(
                        honorRank    = vm.honorRank,
                        badges = vm.badges,
                        medals = vm.medals,
                        mode = ProfileMode.VIEW
                    )
                    StatPanel.CLUBS -> ClubsPanel(
                        clubs          = vm.clubs,
                        mode           = ProfileMode.VIEW,
                        onStatusChange = { idx, status ->
                            vm.clubs[idx] = vm.clubs[idx].copy(status = status)
                        }
                    )
                    StatPanel.INTERESTS -> InterestsPanel(
                        interests = vm.interests,
                        mode = ProfileMode.VIEW,
                        onRemove = {}
                    )
                    null -> ProfileContent(
                        profile = vm.profile,
                        mode    = ProfileMode.VIEW,
                        isEditMode = false
                    )
                }
            }
        }
    }
}