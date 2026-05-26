package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.campusconnect.feature.profile.model.ClubStatus
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
                managePanel == StatPanel.CONNECTIONS -> ManageConnectionsPanel()
                managePanel == StatPanel.HONOR       -> ManageCollectionPanel()
                managePanel == StatPanel.INTERESTS   -> ManageInterestsPanel()

                // -- Stat panels --------------------------------
                panel == StatPanel.CONNECTIONS -> ConnectionsPanel(
                    connections       = vm.connections,
                    mode              = mode,
                    onStatusChange    = { idx, status ->
                        vm.connections[idx] = vm.connections[idx].copy(status = status)
                    },
                    onConnectionClick = { userId ->
                        // ViewProfile handles navigation externally via header lambda
                    }
                )

                panel == StatPanel.HONOR -> HonorPanel(
                    honorRank    = vm.profile.honorRank,
                    badges       = vm.badges,
                    medals       = vm.medals,
                    honorEntries = vm.honorEntries,
                    mode         = mode
                )

                panel == StatPanel.CLUBS -> ClubsPanel(
                    clubs          = vm.clubs,
                    mode           = mode,
                    onStatusChange = { idx, status ->
                        vm.clubs[idx] = vm.clubs[idx].copy(status = status)
                    }
                )

                panel == StatPanel.INTERESTS -> InterestsPanel(
                    interests  = vm.interests,
                    mode       = mode,
                    onRemove   = { if (mode == ProfileMode.OWN) vm.interests.remove(it) },
                    onAddClick = { myVm?.openManagePanel(StatPanel.INTERESTS) }
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