package com.example.campusconnect.feature.profile.ui.myprofile

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onNavigateToProfile: (String) -> Unit = {},
    vm: MyProfileViewModel = viewModel()
) {

    val snackbarHostState = remember { SnackbarHostState() }
    val scope             = rememberCoroutineScope()
    val currentProfile =
        if (vm.isEditMode)
            vm.editableProfile
        else
            vm.profile


    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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

            when {

                vm.isEditMode -> {
                    ProfileBottomBar(
                        buttons = listOf(
                            BottomBarButton(
                                text = "Cancel",
                                icon = Icons.Outlined.Close,
                                onClick = vm::cancelEditing
                            ),
                            BottomBarButton(
                                text = "Save Changes",
                                icon = Icons.Outlined.Check,
                                onClick = vm::saveProfileChanges
                            )
                        )
                    )
                }

                vm.activeManagePanel != null -> {
                    ProfileBottomBar(
                        buttons = listOf(
                            BottomBarButton(
                                text = "Done",
                                icon = Icons.Outlined.Check,
                                onClick = vm::closeManagePanel
                            )
                        )
                    )
                }

                vm.activePanel == null -> {
                    ProfileBottomBar(
                        buttons = listOf(
                            BottomBarButton(
                                text = "Edit Profile",
                                icon = Icons.Outlined.Edit,
                                onClick = vm::startEditing
                            )
                        )
                    )
                }

                vm.activePanel == StatPanel.CONNECTIONS -> {
                    ProfileBottomBar(
                        buttons = listOf(
                            BottomBarButton(
                                text = "Requests",
                                icon = Icons.Outlined.PersonAdd,
                                onClick = {
                                    vm.openManagePanel(
                                        StatPanel.CONNECTIONS
                                    )
                                }
                            )
                        )
                    )
                }

                vm.activePanel == StatPanel.HONOR -> {
                    ProfileBottomBar(
                        buttons = listOf(
                            BottomBarButton(
                                text = "Manage Collection",
                                icon = Icons.Outlined.WorkspacePremium,
                                onClick = {
                                    vm.openManagePanel(
                                        StatPanel.HONOR
                                    )
                                }
                            )
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ProfileHeader(
                initials      = currentProfile.initials,
                displayName   = currentProfile.fullName,
                username      = currentProfile.username,
                bio           = currentProfile.bio,
                badgeColors   = vm.badges.map  { it.color },
                medalColors   = vm.medals.map  { it.color },
                isEditMode    = vm.isEditMode,
                onEditAvatar  = {
                    scope.launch {
                        snackbarHostState.showSnackbar("Image picker after backend and firebase")
                    }
                },
                onBioChange   = { newBio ->
                    vm.updateEditableProfile(currentProfile.copy(bio = newBio))
                }
            )

            StatsRow(
                connectionCount = vm.connections.size,
                honorCount      = vm.badges.size + vm.medals.size,
                clubCount       = vm.clubs.count { it.status == ClubStatus.JOINED },
                interestCount   = vm.interests.size,
                activePanel     = vm.activePanel,
                onStatClick     = { vm.togglePanel(it) }
            )


                // temp before refraction
            AnimatedContent(
                targetState = Pair(vm.activePanel, vm.activeManagePanel),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 10 })
                        .togetherWith(fadeOut() + slideOutVertically { -it / 10 })
                },
                label = "my_profile_panel"
            ) { (panel, managePanel) ->
                when {
                    managePanel == StatPanel.CONNECTIONS ->
                        ManageConnectionsPanel(
                            incomingRequests = vm.incomingRequests,
                            sentInvites = vm.sentInvites,
                            onAccept = vm::acceptRequest,
                            onDecline = vm::declineRequest,
                            onCancelInvite = vm::cancelInvite
                        )
                    managePanel == StatPanel.HONOR ->
                        ManageCollectionPanel(
                            badges = vm.badges,
                            medals = vm.medals,
                            onBadgeMoveUp = vm::moveBadgeUp,
                            onBadgeMoveDown = vm::moveBadgeDown,
                            onMedalMoveUp = vm::moveMedalUp,
                            onMedalMoveDown = vm::moveMedalDown,
                            onBadgeMoveTo = vm::moveBadgeTo,
                            onMedalMoveTo = vm::moveMedalTo
                        )
                    managePanel == StatPanel.INTERESTS ->
                        ManageInterestsPanel(
                            interests = vm.interests,
                            allInterests = vm.allInterests,
                            onAddInterest = vm::addInterest
                        )

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
                        profile       = currentProfile,
                        mode          = ProfileMode.OWN,
                        isEditMode    = vm.isEditMode,
                        onValueChange = vm::updateEditableProfile
                    )
                }
            }
        }
    }
}

