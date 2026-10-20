package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.example.campusconnect.feature.registrations.userflow.model.*

internal val ChipBg = Color(0xFFF1F2F4)
private val DividerColor = Color(0xFFEDEEF0)

// ---------------------------------------------------------------------------------------------
// Shared building blocks
// ---------------------------------------------------------------------------------------------

/** Small grey skill chips ("Kotlin", "Android"...). */
@Composable
internal fun TagChips(tags: List<String>) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        tags.take(3).forEach { tag ->
            Text(
                tag, fontSize = 11.sp, color = TextSecondary, maxLines = 1,
                modifier = Modifier.background(ChipBg, RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 3.dp),
            )
        }
    }
}

/** Rounded action button used in rows and sheets. Filled = primary action, outlined = secondary. */
@Composable
internal fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    filled: Boolean = false,
    enabled: Boolean = true,
    /** Soft orange "state" button, e.g. Requested / Pending; tapping it undoes the state. */
    tonal: Boolean = false,
) {
    val shape = RoundedCornerShape(20.dp)
    val padding = PaddingValues(horizontal = 16.dp)
    if (tonal) Button(
        onClick = onClick, enabled = enabled, shape = shape, contentPadding = padding,
        border = BorderStroke(1.dp, OrangePrimary.copy(alpha = if (enabled) 1f else 0.4f)),
        colors = ButtonDefaults.buttonColors(containerColor = OrangeLight, contentColor = OrangePrimary,
            disabledContainerColor = OrangeLight, disabledContentColor = OrangePrimary.copy(alpha = 0.6f)),
        modifier = modifier.height(36.dp),
    ) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
    else if (filled) Button(
        onClick = onClick, enabled = enabled, shape = shape, contentPadding = padding,
        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
        modifier = modifier.height(36.dp),
    ) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
    else OutlinedButton(
        onClick = onClick, enabled = enabled, shape = shape, contentPadding = padding,
        border = BorderStroke(1.dp, if (enabled) OrangePrimary else Color(0xFFD5D8DD)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
        modifier = modifier.height(36.dp),
    ) { Text(text, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
}

/** Avatar + name + "MCA · 2027" + optional email, with a trailing slot for actions. */
@Composable
internal fun PersonRow(
    id: Int,
    name: String,
    program: String,
    email: String? = null,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier.fillMaxWidth().padding(vertical = 10.dp), verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AppAvatar(entityId = id, displayName = name, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (program.isNotBlank()) Text(program, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
            if (!email.isNullOrBlank()) Text(email, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        trailing()
    }
}

@Composable
internal fun RowDivider() { Box(Modifier.fillMaxWidth().height(1.dp).background(DividerColor)) }

/** Bottom sheet with the portal's look: white surface, rounded top, scrollable body. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PortalSheet(onDismiss: () -> Unit, content: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = CardBg,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
    ) {
        Column(
            Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)
                .padding(bottom = 24.dp).navigationBarsPadding(),
            content = content,
        )
    }
}

@Composable
private fun SheetHeader(title: String, subtitle: String?, onClose: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Column(Modifier.weight(1f).padding(top = 4.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            if (subtitle != null) Text(subtitle, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
        }
        IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextPrimary) }
    }
    Spacer(Modifier.height(8.dp))
}

@Composable
private fun SheetEmpty(text: String) {
    Text(text, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp))
}

// ---------------------------------------------------------------------------------------------
// Team profile popup
// ---------------------------------------------------------------------------------------------

@Composable
internal fun TeamProfileSheet(team: TeamSummary, members: List<TeamMember> = team.members, onDismiss: () -> Unit) {
    PortalSheet(onDismiss) {
        Box(Modifier.fillMaxWidth()) {
            IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopEnd)) {
                Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextPrimary)
            }
            Column(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                AppAvatar(entityId = team.id, displayName = team.name, size = 64.dp)
                Text(team.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary, modifier = Modifier.padding(top = 10.dp))
                Text("${team.memberCount} of ${team.maxMembers} members", fontSize = 13.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                Text("Leader: ${team.leaderName}", fontSize = 13.sp, color = TextSecondary)
                if (team.tags.isNotEmpty()) Box(Modifier.padding(top = 8.dp)) { TagChips(team.tags) }
            }
        }
        Spacer(Modifier.height(12.dp))
        RowDivider()
        Text("Team Members (${members.size})", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary,
            modifier = Modifier.padding(top = 14.dp, bottom = 2.dp))
        members.forEachIndexed { index, member ->
            PersonRow(member.id, member.name, member.program) {
                if (member.role == TeamRole.LEADER) Text(
                    "Leader", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = OrangePrimary,
                    modifier = Modifier.background(OrangeLight, RoundedCornerShape(20.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                )
            }
            if (index < members.lastIndex) RowDivider()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Join requests popup (leader)
// ---------------------------------------------------------------------------------------------

@Composable
internal fun JoinRequestsSheet(
    teamName: String,
    requests: List<TeamJoinRequest>,
    pendingRequestIds: Set<Int>,
    onRespond: (TeamJoinRequest, Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    var requestToDecline by remember { mutableStateOf<TeamJoinRequest?>(null) }
    PortalSheet(onDismiss) {
        SheetHeader("Join Requests (${requests.size})", "People who requested to join $teamName", onDismiss)
        RowDivider()
        if (requests.isEmpty()) SheetEmpty("No pending join requests.")
        requests.forEachIndexed { index, request ->
            val busy = request.id in pendingRequestIds
            PersonRow(request.participantId, request.participantName, request.program, request.email) {
                PillButton("Accept", onClick = { onRespond(request, true) }, filled = true, enabled = !busy)
                PillButton("Decline", onClick = { requestToDecline = request }, enabled = !busy)
            }
            if (index < requests.lastIndex) RowDivider()
        }
    }
    requestToDecline?.let { request ->
        AlertDialog(
            onDismissRequest = { requestToDecline = null },
            title = { Text("Decline join request?") },
            text = { Text("${request.participantName} will be able to request another team.") },
            confirmButton = {
                TextButton(onClick = { onRespond(request, false); requestToDecline = null }) { Text("Decline request", color = OrangePrimary) }
            },
            dismissButton = { TextButton(onClick = { requestToDecline = null }) { Text("Keep request") } },
        )
    }
}

// ---------------------------------------------------------------------------------------------
// Invite search popup (leader)
// ---------------------------------------------------------------------------------------------

internal fun List<TeamInviteCandidate>.matching(query: String): List<TeamInviteCandidate> {
    val q = query.trim()
    if (q.isEmpty()) return this
    return filter { it.name.contains(q, true) || it.email.contains(q, true) || it.program.contains(q, true) }
}

@Composable
internal fun InviteSearchSheet(
    candidates: List<TeamInviteCandidate>,
    isSendingInvite: Boolean,
    onInvite: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var query by rememberSaveable { mutableStateOf("") }
    val focus = remember { FocusRequester() }
    LaunchedEffect(Unit) { runCatching { focus.requestFocus() } }
    val results = remember(candidates, query) { candidates.matching(query) }
    PortalSheet(onDismiss) {
        SheetHeader("Search Participants", null, onDismiss)
        TextField(
            value = query, onValueChange = { query = it }, singleLine = true, shape = RoundedCornerShape(12.dp),
            placeholder = { Text("Search by name, email or course...", fontSize = 13.sp, maxLines = 1) },
            leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
            trailingIcon = if (query.isNotEmpty()) ({
                IconButton(onClick = { query = "" }) { Icon(Icons.Outlined.Close, contentDescription = "Clear search") }
            }) else null,
            colors = TextFieldDefaults.colors(focusedContainerColor = Color(0xFFF6F6F7), unfocusedContainerColor = Color(0xFFF6F6F7),
                focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, cursorColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth().focusRequester(focus),
        )
        Spacer(Modifier.height(8.dp))
        if (results.isEmpty()) SheetEmpty(if (candidates.isEmpty()) "No participants available to invite." else "No participants match your search.")
        results.forEachIndexed { index, candidate ->
            PersonRow(candidate.participantId, candidate.name, candidate.program, candidate.email) {
                PillButton("Invite", onClick = { onInvite(candidate.registrationId) }, filled = true, enabled = !isSendingInvite)
            }
            if (index < results.lastIndex) RowDivider()
        }
    }
}

// ---------------------------------------------------------------------------------------------
// Active invitations popup (leader)
// ---------------------------------------------------------------------------------------------

@Composable
internal fun ActiveInvitationsSheet(
    invitations: List<TeamInvitation>,
    pendingInviteIds: Set<Int>,
    onCancel: (TeamInvitation) -> Unit,
    onDismiss: () -> Unit,
) {
    PortalSheet(onDismiss) {
        SheetHeader("Active Invitations (${invitations.size})", "Invitations sent from your team", onDismiss)
        RowDivider()
        if (invitations.isEmpty()) SheetEmpty("No active invitations.")
        invitations.forEachIndexed { index, invitation -> ActiveInvitationRow(invitation, invitation.id in pendingInviteIds, onCancel)
            if (index < invitations.lastIndex) RowDivider() }
    }
}

@Composable
internal fun ActiveInvitationRow(invitation: TeamInvitation, busy: Boolean, onCancel: (TeamInvitation) -> Unit) {
    PersonRow(invitation.registrationId, invitation.participantName, invitation.program, invitation.email) {
        // "Pending" doubles as the cancel button: one tap withdraws the invitation.
        PillButton("Pending", onClick = { onCancel(invitation) }, tonal = true, enabled = !busy)
    }
}
