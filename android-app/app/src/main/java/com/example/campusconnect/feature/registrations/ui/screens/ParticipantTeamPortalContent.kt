package com.example.campusconnect.feature.registrations.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.ui.components.team.EmptyMessage
import com.example.campusconnect.feature.registrations.ui.components.team.TeamCard
import com.example.campusconnect.feature.registrations.ui.components.team.TeamInviteCard
import com.example.campusconnect.feature.registrations.ui.components.team.TeamPortalTab

@Composable
internal fun ParticipantTeamPortalContent(
    data: TeamPortalData,
    selectedTab: TeamPortalTab,
    filteredTeams: List<TeamSummary>,
    requestedTeamIds: Set<Int>,
    pendingTeamIds: Set<Int>,
    respondedInviteIds: Set<Int>,
    pendingInviteIds: Set<Int>,
    onRequestToJoin: (TeamSummary) -> Unit,
    onInviteResponse: (TeamInvite, Boolean) -> Unit,
    onContinueWithTeam: (TeamSummary) -> Unit,
) {
    val currentTeam = data.currentTeam
    if (currentTeam != null) {
        Column(
            Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("You’re on ${currentTeam.name}", fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(8.dp))
            Text("${currentTeam.memberCount}/${currentTeam.maxMembers} members · Led by ${currentTeam.leaderName}",
                color = TextSecondary, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { onContinueWithTeam(currentTeam) }) {
                Text("Continue registration")
            }
        }
        return
    }

    val invites = data.invites.filter { it.id !in respondedInviteIds }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (selectedTab) {
            TeamPortalTab.AVAILABLE -> {
                if (filteredTeams.isEmpty()) {
                    item {
                        EmptyMessage(
                            if (data.teams.isEmpty()) "No teams are available yet."
                            else "No teams match your search."
                        )
                    }
                }
                items(filteredTeams, key = { it.id }) { team ->
                    TeamCard(
                        team = team,
                        requested = team.id in requestedTeamIds,
                        pending = team.id in pendingTeamIds,
                        onRequest = { onRequestToJoin(team) },
                    )
                }
            }

            TeamPortalTab.INVITES -> {
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
