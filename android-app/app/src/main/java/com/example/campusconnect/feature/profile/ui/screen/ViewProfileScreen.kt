package com.example.campusconnect.feature.profile.ui.screen

import android.app.Application

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.ui.components.*
import com.example.campusconnect.feature.profile.ui.panels.clubs.ClubsPanel
import com.example.campusconnect.feature.profile.ui.panels.connections.ConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.HonorPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.InterestsPanel
import com.example.campusconnect.feature.profile.viewmodel.ViewProfileViewModel

//-------------------------------
// View Profile Screen
//-------------------------------

@Composable
fun ViewProfileScreen(
    userId: Int,
    onBack: () -> Unit
) {

    //-------------------------------
    // ViewModel
    //-------------------------------

    val application =
        LocalContext.current.applicationContext as Application

    val vm: ViewProfileViewModel = viewModel(
        factory = ViewProfileViewModel.factory(
            application = application,
            userId = userId
        )
    )

    //-------------------------------
    // Scaffold
    //-------------------------------

    Scaffold(
        containerColor = PageBg
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            //-------------------------------
            // Profile Header
            //-------------------------------

            ProfileHeader(
                entityId = vm.profile.userId,
                avatarUrl = vm.profile.avatarUrl,
                displayName = vm.profile.fullName,
                username = vm.profile.username,
                bio = vm.profile.bio,

                badgeColors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF9C27B0),
                    Color(0xFF00C853)
                ),

                medalColors = listOf(
                    Color(0xFFFFA000),
                    Color(0xFF9E9E9E)
                ),

                onBack = onBack,

                headerAction = {

                    //-------------------------------
                    // Connection Action
                    //-------------------------------

                    when (vm.profile.relationshipStatus) {

                        ConnectionStatus.NOT_CONNECTED -> {
                            IconButton(
                                onClick = {
                                    vm.sendConnectionRequest()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Orange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = "Connect",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        ConnectionStatus.PENDING -> {
                            IconButton(
                                onClick = {},
                                enabled = false,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Orange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Pending",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        ConnectionStatus.CONNECTED -> {
                            IconButton(
                                onClick = {
                                    vm.removeConnection()
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Orange)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Remove connection",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        null -> Unit
                    }
                }
            )

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            //-------------------------------
            // Profile Stats
            //-------------------------------

            StatsRow(
                connectionCount = vm.stats.connectionCount,
                honorCount = vm.stats.honorCount,
                clubCount = vm.stats.clubCount,
                interestCount = vm.stats.interestCount,
                activePanel = vm.activePanel,
                onStatClick = vm::togglePanel
            )

            //-------------------------------
            // Profile Content / Panels
            //-------------------------------

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                AnimatedContent(
                    modifier = Modifier.fillMaxSize(),
                    targetState = vm.activePanel,
                    transitionSpec = {
                        (
                                fadeIn() +
                                        slideInVertically { it / 10 }
                                ).togetherWith(
                                fadeOut() +
                                        slideOutVertically { -it / 10 }
                            )
                    },
                    label = "view_profile_panel"
                ) { panel ->

                    when (panel) {

                        //-------------------------------
                        // Connections
                        //-------------------------------

                        StatPanel.CONNECTIONS -> {
                            ConnectionsPanel(
                                connections = vm.connections,
                                searchResults = emptyList(),
                                mode = ProfileMode.VIEW,
                                onSearch = {},
                                onStatusChange = { _, _ -> },
                                onRemoveConnection = {},
                                onConnectionClick = {},
                                onCancelConnectionRequest = {}
                            )
                        }

                        //-------------------------------
                        // Honors
                        //-------------------------------

                        StatPanel.HONOR -> {
                            HonorPanel(
                                honorRank = vm.honorRank,
                                badges = vm.badges,
                                medals = vm.medals,
                                mode = ProfileMode.VIEW
                            )
                        }

                        //-------------------------------
                        // Clubs
                        //-------------------------------

                        StatPanel.CLUBS -> {
                            ClubsPanel(
                                clubs = vm.clubs,
                                mode = ProfileMode.VIEW
                            )
                        }

                        //-------------------------------
                        // Interests
                        //-------------------------------

                        StatPanel.INTERESTS -> {
                            InterestsPanel(
                                interests = vm.interests,
                                mode = ProfileMode.VIEW,
                                onRemove = {}
                            )
                        }

                        //-------------------------------
                        // Main Profile
                        //-------------------------------

                        null -> {
                            ProfileContent(
                                profile = vm.profile,
                                mode = ProfileMode.VIEW,
                                isEditMode = false
                            )
                        }
                    }
                }
            }
        }
    }
}