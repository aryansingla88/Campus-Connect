package com.example.campusconnect.feature.registrations.userflow.ui

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
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.viewmodel.PortalState

private enum class PortalTab { AVAILABLE, INVITES }

private val PortalPageBg = Color(0xFFFBF7F4)
private val ChipBg = Color(0xFFF1F2F4)

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
    onRequestToJoin: (TeamSummary) -> Unit,
    onInviteResponse: (invite: TeamInvite, accept: Boolean) -> Unit,
    onRetry: () -> Unit,
    transientError: String?,
    onErrorShown: () -> Unit,
    onBack: () -> Unit,
) {
    var tab by rememberSaveable { mutableStateOf(PortalTab.AVAILABLE) }
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
                title = "Join a Team",
                onBack = onBack,
                step = UserRegistrationStep.TEAM_PORTAL.progress,
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            SearchAndFilterRow(
                query = query,
                onQueryChange = onQueryChange,
                tags = tags,
                tagFilter = tagFilter,
                onTagFilterChange = onTagFilterChange,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )

            PortalTabs(
                selected = tab,
                availableCount = teams.size,
                inviteCount = invites.size,
                onSelect = { tab = it },
            )

            when (portal) {
                PortalState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = OrangePrimary)
                }

                is PortalState.Failed -> Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(portal.message, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = onRetry) { Text("Retry", color = OrangePrimary) }
                }

                is PortalState.Loaded -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    when (tab) {
                        PortalTab.AVAILABLE -> {
                            if (filtered.isEmpty()) {
                                item {
                                    EmptyMessage(
                                        if (teams.isEmpty()) "No teams are available yet."
                                        else "No teams match your search."
                                    )
                                }
                            }
                            items(filtered, key = { it.id }) { team ->
                                TeamCard(
                                    team = team,
                                    requested = team.id in requestedTeamIds,
                                    pending = team.id in pendingTeamIds,
                                    onRequest = { onRequestToJoin(team) },
                                )
                            }
                        }

                        PortalTab.INVITES -> {
                            if (invites.isEmpty()) item { EmptyMessage("You have no pending invites.") }
                            items(invites, key = { it.id }) { invite ->
                                TeamInviteCard(
                                    invite = invite,
                                    pending = invite.id in pendingInviteIds,
                                    onResponse = { accept -> onInviteResponse(invite, accept) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Search + filter ───────────────────────────────────────────────────────────

@Composable
private fun SearchAndFilterRow(
    query: String,
    onQueryChange: (String) -> Unit,
    tags: List<String>,
    tagFilter: String?,
    onTagFilterChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuOpen by remember { mutableStateOf(false) }

    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Search teams by name or leader...", fontSize = 13.sp, maxLines = 1) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = CardBg,
                unfocusedContainerColor = CardBg,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = OrangePrimary,
            ),
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color(0xFFE6E8EC), RoundedCornerShape(12.dp)),
        )
        Box {
            val active = tagFilter != null
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) OrangeLight else CardBg)
                    .border(1.dp, if (active) OrangePrimary else Color(0xFFE6E8EC), RoundedCornerShape(12.dp))
                    .clickable { menuOpen = true },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    Icons.Outlined.FilterList,
                    contentDescription = "Filter by tag",
                    tint = if (active) OrangePrimary else TextPrimary,
                )
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DropdownMenuItem(
                    text = { Text("All tags") },
                    onClick = { onTagFilterChange(null); menuOpen = false },
                )
                tags.forEach { tag ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                tag,
                                fontWeight = if (tag == tagFilter) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (tag == tagFilter) OrangePrimary else TextPrimary,
                            )
                        },
                        onClick = { onTagFilterChange(tag); menuOpen = false },
                    )
                }
            }
        }
    }
}

@Composable
private fun PortalTabs(selected: PortalTab, availableCount: Int, inviteCount: Int, onSelect: (PortalTab) -> Unit) {
    Row(Modifier.fillMaxWidth()) {
        PortalTabItem("Available Teams ($availableCount)", selected == PortalTab.AVAILABLE, Modifier.weight(1f)) {
            onSelect(PortalTab.AVAILABLE)
        }
        PortalTabItem("Invites ($inviteCount)", selected == PortalTab.INVITES, Modifier.weight(1f)) {
            onSelect(PortalTab.INVITES)
        }
    }
}

@Composable
private fun PortalTabItem(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) OrangePrimary else TextSecondary,
            modifier = Modifier.padding(vertical = 12.dp),
        )
        Box(
            Modifier.fillMaxWidth().height(2.dp).background(if (selected) OrangePrimary else Color(0xFFE6E8EC))
        )
    }
}

// ── Cards ─────────────────────────────────────────────────────────────────────

@Composable
private fun TeamCard(team: TeamSummary, requested: Boolean, pending: Boolean, onRequest: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = CardBg,
        border = BorderStroke(1.dp, Color(0xFFEDEEF0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AppAvatar(entityId = team.id, displayName = team.name, size = 44.dp)

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    team.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text("${team.memberCount}/${team.maxMembers} members", fontSize = 13.sp, color = TextSecondary)
                if (team.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 3.dp)) {
                        team.tags.take(3).forEach { TagChip(it) }
                        if (team.tags.size > 3) TagChip("+${team.tags.size - 3}")
                    }
                }
            }

            val enabled = !requested && !pending && !team.isFull
            OutlinedButton(
                onClick = onRequest,
                enabled = enabled,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (enabled || pending) OrangePrimary else Color(0xFFD0D3D9)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                contentPadding = PaddingValues(horizontal = 12.dp),
                modifier = Modifier.height(36.dp),
            ) {
                if (pending) {
                    CircularProgressIndicator(Modifier.size(14.dp), strokeWidth = 2.dp, color = OrangePrimary)
                } else {
                    Text(
                        when {
                            requested -> "Requested"
                            team.isFull -> "Full"
                            else -> "Request to Join"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun TagChip(text: String) {
    Surface(shape = RoundedCornerShape(6.dp), color = ChipBg) {
        Text(
            text,
            fontSize = 11.sp,
            color = Color(0xFF445064),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
private fun EmptyMessage(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
    }
}
