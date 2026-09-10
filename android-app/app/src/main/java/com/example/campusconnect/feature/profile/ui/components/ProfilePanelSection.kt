package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.campusconnect.feature.profile.model.ClubStatus
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.model.ProfileMode
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.ui.panels.clubs.ClubsPanel
import com.example.campusconnect.feature.profile.ui.panels.connections.ConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.connections.ManageConnectionsPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.HonorPanel
import com.example.campusconnect.feature.profile.ui.panels.honor.ManageCollectionPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.InterestsPanel
import com.example.campusconnect.feature.profile.ui.panels.interests.ManageInterestsPanel
import com.example.campusconnect.feature.profile.viewmodel.BaseProfileViewModel
import com.example.campusconnect.feature.profile.viewmodel.MyProfileViewModel

@Composable
fun ProfilePanelSection(
    mode   : ProfileMode,
    vm     : BaseProfileViewModel,
    header : @Composable () -> Unit
) {
    val myVm = if (mode == ProfileMode.OWN) vm as MyProfileViewModel else null

    Column(modifier = Modifier.fillMaxSize()) {

        header()

        StatsRow(
            connectionCount = vm.connections.size,
            honorCount      = vm.badges.size + vm.medals.size,
            clubCount       = vm.clubs.count { it.status == ClubStatus.JOINED },
            interestCount   = vm.interests.size,
            activePanel     = vm.activePanel,
            onStatClick     = { vm.togglePanel(it) }
        )

        AnimatedContent(
            targetState = Pair(vm.activePanel, myVm?.activeManagePanel),
            transitionSpec = {
                (fadeIn() + slideInVertically { it / 10 })
                    .togetherWith(fadeOut() + slideOutVertically { -it / 10 })
            },
            label = "profile_panel"
        ) { (panel, managePanel) ->
            when {

                // -- Manage panels (OWN only) --------------------------------
                managePanel == StatPanel.CONNECTIONS ->
                    myVm?.let {
                        ManageConnectionsPanel(
                            incomingRequests = it.incomingRequests,
                            sentInvites = it.sentInvites,
                            onAccept = it::acceptRequest,
                            onDecline = it::declineRequest,
                            onCancelInvite = it::cancelInvite
                        )
                    }
                managePanel == StatPanel.HONOR ->
                    myVm?.let {
                        ManageCollectionPanel(
                            badges = it.badges,
                            medals = it.medals,

                            onBadgeMoveUp = it::moveBadgeUp,
                            onBadgeMoveDown = it::moveBadgeDown,

                            onMedalMoveUp = it::moveMedalUp,
                            onMedalMoveDown = it::moveMedalDown,

                            onBadgeMoveTo = it::moveBadgeTo,
                            onMedalMoveTo = it::moveMedalTo
                        )
                    }
                managePanel == StatPanel.INTERESTS ->
                    ManageInterestsPanel(
                        interests = vm.interests,
                        allInterests = vm.allInterests,
                        onAddInterest = myVm!!::addInterest
                    )

                // -- Stat panels --------------------------------
                panel == StatPanel.CONNECTIONS -> ConnectionsPanel(
                    connections = vm.connections,
                    mode = mode,
                    onStatusChange = { userId, status ->
                        if (
                            mode == ProfileMode.OWN &&
                            status == ConnectionStatus.PENDING
                        ) {
                            myVm?.sendConnectionRequest(userId)
                        }
                    },
                    onConnectionClick = { }
                )

                panel == StatPanel.HONOR -> HonorPanel(
                    honorRank = vm.honorRank,
                    badges = vm.badges,
                    medals = vm.medals,
                    mode = mode
                )

                panel == StatPanel.CLUBS -> ClubsPanel(
                    clubs = vm.clubs,
                    mode = mode,

                    allClubs = vm.filteredClubs,

                    onJoinClub = { clubId ->
                        myVm?.joinClub(clubId)
                    },

                    onLeaveClub = { clubId ->
                        myVm?.leaveClub(clubId)
                    }
                )

                panel == StatPanel.INTERESTS -> InterestsPanel(
                    interests = vm.interests,
                    mode = mode,
                    onRemove = { interest ->
                        if (mode == ProfileMode.OWN) {
                            myVm?.removeInterest(interest)
                        }
                    },
                    onAddClick = {
                        myVm?.openManagePanel(StatPanel.INTERESTS)
                    }
                )

                // -- Default: profile content --------------------------------
                else -> ProfileContent(
                    profile       = if (mode == ProfileMode.OWN) myVm!!.let {
                        if (it.isEditMode) it.editableProfile else it.profile
                    } else vm.profile,
                    mode          = mode,
                    isEditMode    = myVm?.isEditMode ?: false,
                    onValueChange = { myVm?.updateEditableProfile(it) }
                )
            }
        }
    }
}