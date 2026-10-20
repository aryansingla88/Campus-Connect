package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.*
import com.example.campusconnect.feature.registrations.userflow.viewmodel.PortalState

/** Two tabs: "Teams" (your team + find teams) and, depending on role, "Invite" (leader) or "Invitations" (joiner). */
private enum class PortalTab { TEAMS, INVITES }
private val PortalPageBg = Color(0xFFFBF7F4)
private val CardBorder = BorderStroke(1.dp, Color(0xFFEDEEF0))

/** Participant team portal: your team, available teams, leader invites, and incoming invitations. */
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
    onRequestToJoin: (TeamSummary) -> Unit,
    onInviteResponse: (invite: TeamInvite, accept: Boolean) -> Unit,
    onRetry: () -> Unit,
    transientError: String?,
    onErrorShown: () -> Unit,
    onBack: () -> Unit,
    pendingJoinRequestIds: Set<Int>,
    pendingSentInviteIds: Set<Int>,
    isSendingInvite: Boolean,
    /** "INVITES" opens the second tab; any other value (e.g. the legacy "MY_TEAM"/"AVAILABLE") opens Teams. */
    initialTab: String = "TEAMS",
    onRespondToJoinRequest: (TeamJoinRequest, Boolean) -> Unit,
    onSendInvite: (Int) -> Unit,
    onCancelInvitation: (TeamInvitation) -> Unit,
    /** Withdraws the join request for the given team id. Called immediately, without a confirmation dialog. */
    onCancelJoinRequest: (teamId: Int) -> Unit,
    onContinueWithTeam: (() -> Unit)?,
    onDone: (() -> Unit)? = null,
) {
    var tab by rememberSaveable { mutableStateOf(if (initialTab == PortalTab.INVITES.name) PortalTab.INVITES else PortalTab.TEAMS) }
    var inviteToDecline by remember { mutableStateOf<TeamInvite?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(transientError) {
        if (transientError != null) { snackbarHostState.showSnackbar(transientError); onErrorShown() }
    }
    val data = (portal as? PortalState.Loaded)?.data
    val isLeader = data?.myTeamRole == TeamRole.LEADER
    val teams = data?.teams.orEmpty()
    val invites = data?.invites.orEmpty().filter { it.id !in respondedInviteIds }
    val tags = remember(teams) { teams.flatMap { it.tags }.distinct().sorted() }
    val myTeamId = data?.myTeam?.id
    // Teams the participant has requested are pinned to the top (stable sort keeps the rest in order).
    val filtered = remember(teams, query, tagFilter, myTeamId, requestedTeamIds) {
        teams.filter { team -> team.id != myTeamId && (tagFilter == null || tagFilter in team.tags) &&
            (query.isBlank() || team.name.contains(query.trim(), true) || team.leaderName.contains(query.trim(), true)) }
            .sortedByDescending { it.id in requestedTeamIds }
    }
    Scaffold(
        containerColor = PortalPageBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(color = CardBg) {
                Column(Modifier.statusBarsPadding().fillMaxWidth().padding(bottom = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                        Text("Team Portal", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = TextPrimary, modifier = Modifier.weight(1f).padding(start = 4.dp))
                        if (onDone != null) TextButton(onClick = onDone) { Text("Done", color = OrangePrimary) }
                    }
                    val step = UserRegistrationStep.TEAM_PORTAL.progress
                    if (step != null) {
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            LinearProgressIndicator(progress = { step / 3f }, modifier = Modifier.weight(1f).height(5.dp), color = OrangePrimary)
                            Text("$step/3", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            PortalTabs(tab, secondLabel = if (isLeader) "Invite" else "Invitations", teamsCount = teams.size, onSelect = { tab = it })
            when (portal) {
                PortalState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }
                is PortalState.Failed -> Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center) {
                    Text(portal.message, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Retry", color = OrangePrimary) }
                }
                is PortalState.Loaded -> when {
                    tab == PortalTab.TEAMS -> TeamsTab(
                        data = portal.data, teams = filtered, totalTeams = teams.size,
                        query = query, onQueryChange = onQueryChange, tags = tags, tagFilter = tagFilter, onTagFilterChange = onTagFilterChange,
                        requestedTeamIds = requestedTeamIds, pendingTeamIds = pendingTeamIds, onRequestToJoin = onRequestToJoin,
                        pendingJoinRequestIds = pendingJoinRequestIds, onRespondToJoinRequest = onRespondToJoinRequest,
                        onCancelJoinRequest = onCancelJoinRequest, onContinueWithTeam = onContinueWithTeam,
                    )
                    isLeader -> InviteTab(portal.data, pendingSentInviteIds, isSendingInvite, onSendInvite, onCancelInvitation)
                    else -> InvitationsTab(invites, pendingInviteIds, onResponse = { invite, accept ->
                        if (accept) onInviteResponse(invite, true) else inviteToDecline = invite
                    })
                }
            }
        }
        if (inviteToDecline != null) AlertDialog(
            onDismissRequest = { inviteToDecline = null },
            title = { Text("Reject invitation?") },
            text = { Text("You can still browse and request to join other teams.") },
            confirmButton = {
                TextButton(onClick = { inviteToDecline?.let { onInviteResponse(it, false) }; inviteToDecline = null }) {
                    Text("Reject", color = OrangePrimary)
                }
            },
            dismissButton = { TextButton(onClick = { inviteToDecline = null }) { Text("Keep invitation") } },
        )
    }
}

@Composable
private fun PortalTabs(selected: PortalTab, secondLabel: String, teamsCount: Int, onSelect: (PortalTab) -> Unit) {
    Row(Modifier.fillMaxWidth().background(CardBg)) {
        listOf(PortalTab.TEAMS to "Teams ($teamsCount)", PortalTab.INVITES to secondLabel).forEach { (tab, label) ->
            Column(Modifier.weight(1f).clickable { onSelect(tab) }, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(label, fontSize = 13.sp, fontWeight = if (selected == tab) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (selected == tab) OrangePrimary else TextSecondary, modifier = Modifier.padding(vertical = 12.dp))
                Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected == tab) OrangePrimary else Color(0xFFE6E8EC)))
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = modifier)
}

// ---------------------------------------------------------------------------------------------
// Teams tab
// ---------------------------------------------------------------------------------------------

@Composable
private fun TeamsTab(
    data: TeamPortalData,
    teams: List<TeamSummary>,
    totalTeams: Int,
    query: String,
    onQueryChange: (String) -> Unit,
    tags: List<String>,
    tagFilter: String?,
    onTagFilterChange: (String?) -> Unit,
    requestedTeamIds: Set<Int>,
    pendingTeamIds: Set<Int>,
    onRequestToJoin: (TeamSummary) -> Unit,
    pendingJoinRequestIds: Set<Int>,
    onRespondToJoinRequest: (TeamJoinRequest, Boolean) -> Unit,
    onCancelJoinRequest: (teamId: Int) -> Unit,
    onContinueWithTeam: (() -> Unit)?,
) {
    var profileTeam by remember { mutableStateOf<TeamSummary?>(null) }
    var showRequests by rememberSaveable { mutableStateOf(false) }
    val myTeam = data.myTeam
    val isLeader = data.myTeamRole == TeamRole.LEADER
    val showHeadings = myTeam != null

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (myTeam != null) {
            item("your-team-title") { SectionTitle("Your Team") }
            item("your-team") {
                YourTeamCard(myTeam, requestCount = if (isLeader) data.incomingJoinRequests.size else null,
                    onOpen = { profileTeam = myTeam }, onRequests = { showRequests = true })
            }
            if (data.myTeamRole == TeamRole.MEMBER && onContinueWithTeam != null) item("continue") {
                Button(onClick = onContinueWithTeam, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)) {
                    Text("Continue registration with this team", fontWeight = FontWeight.SemiBold)
                }
            }
        } else if (requestedTeamIds.isEmpty()) when (data.membershipStatus) {
            TeamMembershipStatus.REQUEST_REJECTED -> item("notice") { Notice("Your last join request was declined. You can request to join another team.") }
            TeamMembershipStatus.INVITE_REJECTED -> item("notice") { Notice("You rejected the last invitation. Browse teams if you are still looking to join.") }
            else -> Unit
        }

        if (showHeadings) item("find-title") { SectionTitle("Find teams", Modifier.padding(top = 4.dp)) }
        item("search") { SearchAndFilterRow(query, onQueryChange, tags, tagFilter, onTagFilterChange) }
        if (teams.isEmpty()) item("empty") { EmptyMessage(if (totalTeams == 0) "No teams are available yet." else "No teams match your search.") }
        items(teams, key = { it.id }) { team ->
            TeamCard(team, requested = team.id in requestedTeamIds, busy = team.id in pendingTeamIds,
                canRequest = myTeam == null && data.invites.isEmpty()
                    && (requestedTeamIds.isEmpty() || team.id in requestedTeamIds), onOpen = { profileTeam = team },
                onRequest = { onRequestToJoin(team) }, onCancelRequest = { onCancelJoinRequest(team.id) })
        }
    }

    profileTeam?.let { team ->
        // The own team's roster comes from the portal; other teams carry theirs on the summary.
        TeamProfileSheet(team, if (team.id == myTeam?.id) data.members else team.members, onDismiss = { profileTeam = null })
    }
    if (showRequests && isLeader && myTeam != null) JoinRequestsSheet(
        teamName = myTeam.name, requests = data.incomingJoinRequests, pendingRequestIds = pendingJoinRequestIds,
        onRespond = onRespondToJoinRequest, onDismiss = { showRequests = false },
    )
}

/** The participant's own team. Leaders also get a "Requests (n)" pill; the whole card opens the team profile. */
@Composable
private fun YourTeamCard(team: TeamSummary, requestCount: Int?, onOpen: () -> Unit, onRequests: () -> Unit) {
    Surface(shape = RoundedCornerShape(16.dp), color = CardBg, border = CardBorder, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppAvatar(entityId = team.id, displayName = team.name, size = 52.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(team.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${team.memberCount}/${team.maxMembers} members", fontSize = 13.sp, color = TextSecondary)
                Text("Leader: ${team.leaderName}", fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (requestCount != null) OutlinedButton(
                onClick = onRequests, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 12.dp),
                border = BorderStroke(1.dp, OrangePrimary), colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                modifier = Modifier.height(34.dp),
            ) { Text("Requests ($requestCount)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) }
            Icon(Icons.Outlined.ChevronRight, contentDescription = "View team", tint = TextSecondary)
        }
    }
}

@Composable
private fun SearchAndFilterRow(query: String, onQueryChange: (String) -> Unit, tags: List<String>, tagFilter: String?,
    onTagFilterChange: (String?) -> Unit, modifier: Modifier = Modifier) {
    var menuOpen by remember { mutableStateOf(false) }
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(value = query, onValueChange = onQueryChange, placeholder = { Text("Search teams by name or leader...", fontSize = 13.sp, maxLines = 1) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) }, singleLine = true, shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = CardBg, unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = OrangePrimary),
            modifier = Modifier.weight(1f))
        Box {
            IconButton(onClick = { menuOpen = true }) { Icon(Icons.Outlined.FilterList, contentDescription = "Filter by tag", tint = if (tagFilter != null) OrangePrimary else TextPrimary) }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(text = { Text("All tags") }, onClick = { onTagFilterChange(null); menuOpen = false })
                tags.forEach { tag -> DropdownMenuItem(text = { Text(tag) }, onClick = { onTagFilterChange(tag); menuOpen = false }) }
            }
        }
    }
}

@Composable
private fun TeamCard(
    team: TeamSummary,
    requested: Boolean,
    busy: Boolean,
    canRequest: Boolean,
    onOpen: () -> Unit,
    onRequest: () -> Unit,
    onCancelRequest: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(16.dp), color = CardBg, border = CardBorder, modifier = Modifier.fillMaxWidth().clickable(onClick = onOpen)) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppAvatar(entityId = team.id, displayName = team.name, size = 48.dp)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(team.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${team.memberCount}/${team.maxMembers} members · ${team.leaderName}", fontSize = 12.sp, color = TextSecondary,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (team.tags.isNotEmpty()) TagChips(team.tags)
            }
            if (requested) PillButton("Requested", onClick = onCancelRequest, tonal = true, enabled = !busy)
            else if (team.isFull) Button(
                onClick = {}, enabled = false, shape = RoundedCornerShape(20.dp), contentPadding = PaddingValues(horizontal = 16.dp),
                colors = ButtonDefaults.buttonColors(disabledContainerColor = Color(0xFFE3E4E7), disabledContentColor = TextSecondary),
                modifier = Modifier.height(34.dp),
            ) { Text("Full", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
            else OutlinedButton(
                onClick = onRequest, enabled = canRequest && !busy, shape = RoundedCornerShape(20.dp),
                contentPadding = PaddingValues(horizontal = 14.dp), modifier = Modifier.height(34.dp),
            ) { Text("Request", fontSize = 12.sp, fontWeight = FontWeight.Medium) }
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Invite tab (leader)
// ---------------------------------------------------------------------------------------------

private const val ACTIVE_INVITES_PREVIEW = 3

@Composable
private fun InviteTab(
    data: TeamPortalData,
    pendingSentInviteIds: Set<Int>,
    isSendingInvite: Boolean,
    onSendInvite: (Int) -> Unit,
    onCancelInvitation: (TeamInvitation) -> Unit,
) {
    var showSearch by rememberSaveable { mutableStateOf(false) }
    var showActive by rememberSaveable { mutableStateOf(false) }
    val candidates = data.inviteCandidates
    val invitations = data.outgoingInvites

    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item("invite-title") { SectionTitle("Invite people to your team") }
        item("invite-search") {
            Surface(shape = RoundedCornerShape(12.dp), color = CardBg, modifier = Modifier.fillMaxWidth().clickable { showSearch = true }) {
                Row(Modifier.padding(horizontal = 16.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Outlined.Search, contentDescription = null, tint = TextSecondary)
                    Text("Search by name, email or course...", fontSize = 13.sp, color = TextSecondary, maxLines = 1)
                }
            }
        }
        if (candidates.isEmpty()) item("no-candidates") { EmptyMessage("No participants are available to invite right now.") }
        else item("candidates") {
            Surface(shape = RoundedCornerShape(14.dp), color = CardBg, border = CardBorder, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp)) {
                    candidates.forEachIndexed { index, candidate ->
                        PersonRow(candidate.participantId, candidate.name, candidate.program, candidate.email) {
                            PillButton("Invite", onClick = { onSendInvite(candidate.registrationId) }, filled = true, enabled = !isSendingInvite)
                        }
                        if (index < candidates.lastIndex) RowDivider()
                    }
                }
            }
        }

        item("active-header") {
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                SectionTitle("Active invitations (${invitations.size})", Modifier.weight(1f))
                if (invitations.isNotEmpty()) TextButton(onClick = { showActive = true }) {
                    Text("View all", color = OrangePrimary, fontSize = 13.sp)
                }
            }
        }
        if (invitations.isEmpty()) item("no-invitations") { Text("No active invitations yet.", fontSize = 13.sp, color = TextSecondary) }
        else item("invitations") {
            Surface(shape = RoundedCornerShape(14.dp), color = CardBg, border = CardBorder, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(horizontal = 14.dp)) {
                    val shown = invitations.take(ACTIVE_INVITES_PREVIEW)
                    shown.forEachIndexed { index, invitation ->
                        ActiveInvitationRow(invitation, invitation.id in pendingSentInviteIds, onCancelInvitation)
                        if (index < shown.lastIndex) RowDivider()
                    }
                }
            }
        }
    }

    if (showSearch) InviteSearchSheet(candidates, isSendingInvite, onInvite = onSendInvite, onDismiss = { showSearch = false })
    if (showActive) ActiveInvitationsSheet(invitations, pendingSentInviteIds, onCancel = onCancelInvitation, onDismiss = { showActive = false })
}

// ---------------------------------------------------------------------------------------------
// Invitations tab (joiner)
// ---------------------------------------------------------------------------------------------

@Composable
private fun InvitationsTab(invites: List<TeamInvite>, pendingInviteIds: Set<Int>, onResponse: (TeamInvite, Boolean) -> Unit) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item("title") { SectionTitle("Team Invitations (${invites.size})") }
        items(invites, key = { it.id }) { invite ->
            TeamInviteCard(invite, pending = invite.id in pendingInviteIds, onResponse = { accept -> onResponse(invite, accept) })
        }
        item("footer") {
            Column(Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Outlined.MailOutline, contentDescription = null, tint = Color(0xFFC9CCD2), modifier = Modifier.size(44.dp))
                Text("No more invitations", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text("New invitations from teams will appear here.", fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
            }
        }
    }
}

// ---------------------------------------------------------------------------------------------

@Composable
private fun Notice(text: String) {
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF4F1EE), modifier = Modifier.fillMaxWidth()) {
        Text(text, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(12.dp))
    }
}

@Composable
private fun EmptyMessage(text: String) { Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
    Text(text, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
} }
