package com.example.campusconnect.feature.profile.ui.screen

import androidx.activity.compose.BackHandler
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

import com.example.campusconnect.core.utils.Image.ImagePickerSheet
import com.example.campusconnect.core.utils.Image.ImageSource
import com.example.campusconnect.core.utils.Image.rememberImagePicker
import com.example.campusconnect.feature.profile.model.ConnectionStatus
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

//-------------------------------
// My Profile Screen
//-------------------------------

@Composable
fun MyProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onNavigateToProfile: (Int) -> Unit = {},
    initialEditMode: Boolean = false,
    vm: MyProfileViewModel = viewModel()
) {

    //-------------------------------
    // Local State
    //-------------------------------

    var showImagePicker by remember {
        mutableStateOf(false)
    }

    val snackbarHostState = remember {
        SnackbarHostState()
    }

    LaunchedEffect(initialEditMode) {
        if (initialEditMode && !vm.isEditMode) {
            vm.startEditing()
        }
    }

    val currentProfile =
        if (vm.isEditMode) {
            vm.editableProfile
        } else {
            vm.profile
        }

    //-------------------------------
    // Back Handling
    //-------------------------------

    BackHandler(
        enabled =
            showImagePicker ||
                    vm.isEditMode ||
                    vm.activeManagePanel != null ||
                    vm.activePanel != null
    ) {
        when {
            showImagePicker -> {
                showImagePicker = false
            }

            vm.isEditMode -> {
                vm.cancelEditing()
            }

            vm.activeManagePanel != null -> {
                vm.closeManagePanel()
            }

            vm.activePanel != null -> {
                vm.togglePanel(vm.activePanel!!)
            }
        }
    }

    //-------------------------------
    // Image Picker
    //-------------------------------

    val pickImage = rememberImagePicker { uri ->
        vm.updateAvatar(uri)
    }

    if (showImagePicker) {
        ImagePickerSheet(
            onDismiss = {
                showImagePicker = false
            },
            onCameraClick = {
                showImagePicker = false
                pickImage(ImageSource.CAMERA)
            },
            onGalleryClick = {
                showImagePicker = false
                pickImage(ImageSource.GALLERY)
            }
        )
    }

    //-------------------------------
    // Scaffold
    //-------------------------------

    Scaffold(
        containerColor = PageBg,
        snackbarHost = {
            SnackbarHost(snackbarHostState)
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

        //-------------------------------
        // Main Layout
        //-------------------------------

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            //-------------------------------
            // Profile Header
            //-------------------------------

            ProfileHeader(
                entityId = currentProfile.userId,
                avatarUrl = currentProfile.avatarUrl,
                avatarUri = vm.selectedAvatarUri,
                displayName = currentProfile.fullName,
                username = currentProfile.username,
                bio = currentProfile.bio,

                badgeColors = listOf(
                    Color(0xFF2196F3),
                    Color(0xFF9C27B0),
                    Color(0xFF00C853)
                ),

                medalColors = listOf(
                    Color(0xFFFFA000),
                    Color(0xFF9E9E9E)
                ),

                isEditMode = vm.isEditMode,

                onEditAvatar = {
                    showImagePicker = true
                },

                onBioChange = { newBio ->
                    vm.updateEditableProfile(
                        currentProfile.copy(
                            bio = newBio
                        )
                    )
                },

                onBack = onBack,
                onSettings = onSettings
            )

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            //-------------------------------
            // Profile Stats
            //-------------------------------

            val profileInteractionLocked =
                vm.isEditMode || vm.activeManagePanel != null

            StatsRow(
                connectionCount = vm.stats.connectionCount,
                honorCount = vm.stats.honorCount,
                clubCount = vm.stats.clubCount,
                interestCount = vm.stats.interestCount,
                activePanel = vm.activePanel,
                enabled = !profileInteractionLocked,
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
                    targetState = Pair(
                        vm.activePanel,
                        vm.activeManagePanel
                    ),
                    transitionSpec = {
                        (
                                fadeIn() +
                                        slideInVertically { it / 10 }
                                ).togetherWith(
                                fadeOut() +
                                        slideOutVertically { -it / 10 }
                            )
                    },
                    label = "my_profile_panel"
                ) { (panel, managePanel) ->

                    when {

                        //-------------------------------
                        // Manage Connections
                        //-------------------------------

                        managePanel == StatPanel.CONNECTIONS -> {
                            ManageConnectionsPanel(
                                incomingRequests = vm.incomingRequests,
                                sentInvites = vm.sentInvites,
                                onAccept = vm::acceptRequest,
                                onDecline = vm::declineRequest,
                                onCancelInvite = vm::cancelInvite
                            )
                        }

                        //-------------------------------
                        // Manage Honors
                        //-------------------------------

                        managePanel == StatPanel.HONOR -> {
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
                        }

                        //-------------------------------
                        // Manage Interests
                        //-------------------------------

                        managePanel == StatPanel.INTERESTS -> {
                            ManageInterestsPanel(
                                interests = vm.interests,
                                allInterests = vm.allInterests,
                                onAddInterest = vm::addInterest
                            )
                        }

                        //-------------------------------
                        // Connections
                        //-------------------------------

                        panel == StatPanel.CONNECTIONS -> {
                            ConnectionsPanel(
                                connections = vm.connections,
                                searchResults = vm.searchResults,
                                mode = ProfileMode.OWN,
                                onSearch = vm::searchUsers,

                                onStatusChange = { userId, status ->
                                    if (status == ConnectionStatus.PENDING) {
                                        vm.sendConnectionRequest(userId)
                                    }
                                },

                                onRemoveConnection = vm::removeConnection,

                                onConnectionClick = onNavigateToProfile,

                                onCancelConnectionRequest =
                                    vm::cancelConnectionRequest
                            )
                        }

                        //-------------------------------
                        // Honors
                        //-------------------------------

                        panel == StatPanel.HONOR -> {
                            HonorPanel(
                                honorRank = vm.honorRank,
                                badges = vm.badges,
                                medals = vm.medals,
                                mode = ProfileMode.OWN
                            )
                        }

                        //-------------------------------
                        // Clubs
                        //-------------------------------

                        panel == StatPanel.CLUBS -> {
                            ClubsPanel(
                                clubs = vm.clubs,
                                mode = ProfileMode.OWN,
                                allClubs = vm.allClubs,
                                onJoinClub = vm::joinClub,
                                onLeaveClub = vm::leaveClub
                            )
                        }

                        //-------------------------------
                        // Interests
                        //-------------------------------

                        panel == StatPanel.INTERESTS -> {
                            InterestsPanel(
                                interests = vm.interests,
                                mode = ProfileMode.OWN,
                                onRemove = vm::removeInterest,
                                onAddClick = {
                                    vm.openManagePanel(
                                        StatPanel.INTERESTS
                                    )
                                }
                            )
                        }

                        //-------------------------------
                        // Main Profile
                        //-------------------------------

                        else -> {
                            ProfileContent(
                                profile = currentProfile,
                                mode = ProfileMode.OWN,
                                isEditMode = vm.isEditMode,
                                onValueChange = vm::updateEditableProfile
                            )
                        }
                    }
                }
            }
        }
    }
}