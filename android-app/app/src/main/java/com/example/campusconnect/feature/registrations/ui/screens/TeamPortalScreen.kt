package com.example.campusconnect.feature.registrations.ui.screens

import com.example.campusconnect.feature.registrations.ui.components.*
import com.example.campusconnect.feature.registrations.ui.components.team.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.viewmodel.PortalState



private val PortalPageBg = Color(0xFFFBF7F4)


/** Step 2/3: browse available teams and request to join, or answer invites. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamPortalScreen(
    portal: PortalState,
    query: String,
    onQueryChange: (String) -> Unit,
    tagFilter: String?,
    onTagFilterChange: (String?) -> Unit,
    requestedTeamIds: Set<Int>,
    pendingTeamIds: Set<Int>,
    respondedInviteIds: Set<Int>,
    pendingInviteIds: Set<Int>,
    pendingJoinRequestIds: Set<Int>,
    pendingInviteSendIds: Set<Int>,
    onRequestToJoin: (TeamSummary) -> Unit,
    onInviteResponse: (invite: TeamInvite, accept: Boolean) -> Unit,
    onJoinRequestResponse: (request: TeamJoinRequest, accept: Boolean) -> Unit,
    onInviteCandidate: (TeamInviteCandidate) -> Unit,
    onContinueWithTeam: (TeamSummary) -> Unit,
    onRetry: () -> Unit,
    transientError: String?,
    onErrorShown: () -> Unit,
    onBack: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(TeamPortalTab.AVAILABLE) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(transientError) {
        if (transientError != null) {
            snackbarHostState.showSnackbar(transientError)
            onErrorShown()
        }
    }

    val data = (portal as? PortalState.Loaded)?.data
    val teams = data?.teams.orEmpty()
    val invites = data?.invites.orEmpty().filter { it.id !in respondedInviteIds }
    val tags = remember(teams) { teams.flatMap { it.tags }.distinct().sorted() }
    val filtered = remember(teams, query, tagFilter) {
        val q = query.trim()
        teams.filter { team ->
            (tagFilter == null || tagFilter in team.tags) &&
                    (q.isBlank() || team.name.contains(q, true) || team.leaderName.contains(q, true))
        }
    }

    Scaffold(
        containerColor = PortalPageBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            FlowTopBar(
                title = if (data?.currentTeam != null && data.currentUserIsLeader) "Manage Your Team" else "Join a Team",
                onBack = onBack,
                step = UserRegistrationStep.TEAM_PORTAL.progress,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            if (data?.currentTeam == null) {
                TeamPortalTabs(
                    selected = tab,
                    availableCount = teams.size,
                    inviteCount = invites.size,
                    onSelect = { tab = it },
                )
            }
            if (data?.currentTeam == null || data.currentUserIsLeader) SearchAndFilterRow(
                query = query,
                onQueryChange = onQueryChange,
                tags = if (data?.currentTeam == null) tags else emptyList(),
                tagFilter = if (data?.currentTeam == null) tagFilter else null,
                onTagFilterChange = onTagFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            when (portal) {
                PortalState.Loading -> TeamListSkeleton()

                is PortalState.Failed -> Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(portal.message, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Retry", color = OrangePrimary) }
                }

                is PortalState.Loaded -> {
                    val loadedData = portal.data
                    if (loadedData.currentTeam != null && loadedData.currentUserIsLeader) {
                        LeaderTeamPortalContent(
                            data = loadedData,
                            query = query,
                            pendingJoinRequestIds = pendingJoinRequestIds,
                            pendingInviteSendIds = pendingInviteSendIds,
                            onJoinRequestResponse = onJoinRequestResponse,
                            onInviteCandidate = onInviteCandidate,
                        )
                    } else ParticipantTeamPortalContent(
                        data = loadedData,
                        selectedTab = tab,
                        filteredTeams = filtered,
                        requestedTeamIds = requestedTeamIds,
                        pendingTeamIds = pendingTeamIds,
                        respondedInviteIds = respondedInviteIds,
                        pendingInviteIds = pendingInviteIds,
                        onRequestToJoin = onRequestToJoin,
                        onInviteResponse = onInviteResponse,
                        onContinueWithTeam = onContinueWithTeam,
                    )
                }
            }
        }
    }
}
