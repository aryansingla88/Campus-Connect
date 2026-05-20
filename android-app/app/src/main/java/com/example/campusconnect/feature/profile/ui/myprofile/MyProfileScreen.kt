package com.example.campusconnect.feature.profile.ui.myprofile

import kotlin.Pair
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
import com.example.campusconnect.feature.profile.data.FakeProfileService
import com.example.campusconnect.model.*
import com.example.campusconnect.feature.profile.ui.shared.*
import com.example.campusconnect.feature.profile.ui.shared.panels.connections.*
import com.example.campusconnect.feature.profile.ui.shared.panels.clubs.*
import com.example.campusconnect.feature.profile.ui.shared.panels.honor.*
import com.example.campusconnect.feature.profile.ui.shared.panels.interests.*
import com.example.campusconnect.feature.profile.viewmodel.MyProfileViewModel
import com.example.campusconnect.model.StatPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyProfileScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    vm: MyProfileViewModel = viewModel()
) {
    // ── Mutable local state wired to FakeProfileService ───────────────────────
    val connections = remember { FakeProfileService.connections.toMutableStateList() }
    val clubs       = remember { FakeProfileService.clubs.toMutableStateList() }
    val interests   = remember { FakeProfileService.interests.toMutableStateList() }

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
            MyProfileBottomBar(
                activePanel = vm.activePanel,
                onEditProfile = onEditProfile,
                onRequestsClick = { vm.openManagePanel(StatPanel.CONNECTIONS) },
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
                initials    = "AS",
                displayName = "Aryan Sharma",
                username    = "@aryan.sharma",
                bio         = "Tech enthusiast, problem solver and always up for new ideas.",
                badgeColors = listOf(BadgeBlue, BadgePurple, BadgeGreen),
                medalColors = listOf(MedalGold, MedalSilver, MedalOrange),
            )

            StatsRow(
                connectionCount = connections.size,
                honorCount      = FakeProfileService.badges.size + FakeProfileService.medals.size,
                clubCount       = clubs.count { it.status == ClubStatus.JOINED },
                interestCount   = interests.size,
                activePanel     = vm.activePanel,
                onStatClick     = { vm.togglePanel(it) }
            )

            // If a manage panel is open, show it; otherwise show the stat panel or default content
            AnimatedContent<
                    Pair<StatPanel, StatPanel?>
                    >(
                targetState = Pair(
                    vm.activePanel,
                    vm.activeManagePanel
                ),
                transitionSpec = {
                    (fadeIn() + slideInVertically { it / 10 })
                        .togetherWith(
                            fadeOut() + slideOutVertically { -it / 10 }
                        )
                },
                label = "my_profile_panel"
            ) { state: Pair<StatPanel, StatPanel?> ->

                val panel = state.first
                val managePanel = state.second
                when {
                    managePanel == StatPanel.CONNECTIONS -> ManageConnectionsPanel()
                    managePanel == StatPanel.HONOR       -> ManageCollectionPanel()
                    managePanel == StatPanel.INTERESTS   -> ManageInterestsPanel()
                    panel == StatPanel.CONNECTIONS -> ConnectionsPanel(
                        connections = connections,
                        isOwner = true,
                        onStatusChange = { idx, status -> connections[idx] = connections[idx].copy(status = status) }
                    )
                    panel == StatPanel.HONOR       -> HonorPanel(
                        honorRank = 2,
                        badges    = FakeProfileService.badges,
                        medals    = FakeProfileService.medals,
                        isOwner   = true
                    )
                    panel == StatPanel.CLUBS       -> ClubsPanel(
                        clubs = clubs,
                        isOwner = true,
                        onStatusChange = { idx, status -> clubs[idx] = clubs[idx].copy(status = status) }
                    )
                    panel == StatPanel.INTERESTS   -> InterestsPanel(
                        interests = interests,
                        isOwner = true,
                        onRemove = { interests.remove(it) },
                        onAddClick = { vm.openManagePanel(StatPanel.INTERESTS) }
                    )

                    else                           -> MyProfileContent()
                }
            }
        }
    }
}

// ── Bottom bar ────────────────────────────────────────────────────────────────
@Composable
private fun MyProfileBottomBar(
    activePanel: StatPanel,
    onEditProfile: () -> Unit,
    onRequestsClick: () -> Unit,
    onManageCollectionClick: () -> Unit
) {
    val config: Triple<String, ImageVector, () -> Unit>? = when (activePanel) {
        StatPanel.NONE        -> Triple("Edit Profile",       Icons.Outlined.Edit,             onEditProfile)
        StatPanel.CONNECTIONS -> Triple("Requests",           Icons.Outlined.PersonAdd,        onRequestsClick)
        StatPanel.HONOR       -> Triple("Manage Collection",  Icons.Outlined.WorkspacePremium, onManageCollectionClick)
        StatPanel.CLUBS,
        StatPanel.INTERESTS   -> null
    }

    AnimatedVisibility(
        visible = config != null,
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut()
    ) {
        config?.let { (text, icon, click) ->
            Surface(color = PageBg, shadowElevation = 8.dp, tonalElevation = 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Button(
                        onClick = click,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
