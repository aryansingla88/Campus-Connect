package com.example.campusconnect.feature.registrations.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.ui.components.team.LeaderInviteCandidateItem
import com.example.campusconnect.feature.registrations.ui.components.team.LeaderJoinRequestItem
import com.example.campusconnect.feature.registrations.ui.components.team.TeamMemberItem
@Composable
internal fun LeaderTeamPortalContent(
    data: TeamPortalData,
    query: String,
    pendingJoinRequestIds: Set<Int>,
    pendingInviteSendIds: Set<Int>,
    onJoinRequestResponse: (TeamJoinRequest, Boolean) -> Unit,
    onInviteCandidate: (TeamInviteCandidate) -> Unit,
) {
    val team = data.currentTeam ?: return
    val candidates = data.inviteCandidates.filter { it.displayName.contains(query.trim(), true) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Surface(shape = RoundedCornerShape(16.dp), color = CardBg, border = BorderStroke(1.dp, Color(0xFFEDEEF0))) {
                Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(team.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${team.memberCount}/${team.maxMembers} members · minimum ${team.minMembers}", color = TextSecondary)
                    Text("Registration: ${team.registrationStatus}", color = TextSecondary)
                }
            }
        }
        item { Text("Members", fontWeight = FontWeight.SemiBold, color = TextPrimary) }
        items(data.members, key = { "member-${it.registrationId}" }) { member ->
            TeamMemberItem(member.displayName, member.status, member.userId == team.leaderUserId)
        }
        item { Text("Join requests", fontWeight = FontWeight.SemiBold, color = TextPrimary) }
        if (data.pendingJoinRequests.isEmpty()) item { Text("No pending join requests.", color = TextSecondary) }
        items(data.pendingJoinRequests, key = { "request-${it.requestId}" }) { request ->
            LeaderJoinRequestItem(
                request.participantName,
                pending = request.requestId in pendingJoinRequestIds,
                onRespond = { accept -> onJoinRequestResponse(request, accept) },
            )
        }
        item { Text("Invite participants", fontWeight = FontWeight.SemiBold, color = TextPrimary) }
        if (candidates.isEmpty()) item { Text("No eligible participants found.", color = TextSecondary) }
        items(candidates, key = { "candidate-${it.registrationId}" }) { candidate ->
            LeaderInviteCandidateItem(
                candidate.displayName,
                enabled = candidate.registrationId !in pendingInviteSendIds && team.memberCount < team.maxMembers,
                onInvite = { onInviteCandidate(candidate) },
            )
        }
    }
}
