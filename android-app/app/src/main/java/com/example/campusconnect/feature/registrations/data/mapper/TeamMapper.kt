package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.AvailableTeamDto
import com.example.campusconnect.feature.registrations.data.remote.dto.TeamCandidateDto
import com.example.campusconnect.feature.registrations.data.remote.dto.TeamMemberResponseDto
import com.example.campusconnect.feature.registrations.data.remote.dto.TeamPortalResponseDto
import com.example.campusconnect.feature.registrations.data.remote.dto.TeamRequestDto
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamMemberSummary
import com.example.campusconnect.feature.registrations.model.TeamSummary

fun TeamRequestDto.toInviteOrNull(): TeamInvite? {
    val requestId = requestId ?: return null
    val teamId = teamId ?: return null
    return TeamInvite(
        id = requestId,
        teamId = teamId,
        teamName = teamName.orEmpty(),
        invitedBy = "Team leader",
        memberCount = 0,
        maxMembers = 0,
        sentAgo = createdAt.orEmpty(),
    )
}

fun List<TeamRequestDto>.toPendingInvites(): List<TeamInvite> =
    filter { it.initiatedBy.equals("LEADER", ignoreCase = true) && it.status.equals("PENDING", ignoreCase = true) }
        .mapNotNull { it.toInviteOrNull() }

fun List<TeamRequestDto>.pendingJoinTeamIds(): Set<Int> =
    filter { it.initiatedBy.equals("PARTICIPANT", ignoreCase = true) && it.status.equals("PENDING", ignoreCase = true) }
        .mapNotNull { it.teamId }
        .toSet()

fun TeamPortalResponseDto.toTeamSummary(fallbackTeamId: Int): TeamSummary = TeamSummary(
    id = teamId ?: fallbackTeamId,
    name = teamName.orEmpty(),
    leaderName = leaderName.orEmpty(),
    memberCount = memberCount ?: 0,
    maxMembers = maxMembers ?: 0,
    leaderUserId = leaderUserId,
    minMembers = minMembers ?: 1,
    registrationStatus = status.orEmpty(),
)

fun TeamPortalResponseDto.toMemberSummaries(): List<TeamMemberSummary> =
    members.orEmpty().mapNotNull { it.toTeamMemberSummaryOrNull() }

fun TeamMemberResponseDto.toTeamMemberSummaryOrNull(): TeamMemberSummary? {
    val registrationId = registrationId ?: return null
    val userId = userId ?: return null
    return TeamMemberSummary(registrationId, userId, displayName.orEmpty(), status.orEmpty())
}

fun TeamRequestDto.toTeamJoinRequestOrNull(): TeamJoinRequest? {
    val requestId = requestId ?: return null
    val registrationId = registrationId ?: return null
    return TeamJoinRequest(requestId, registrationId, participantName.orEmpty())
}

fun TeamCandidateDto.toTeamInviteCandidateOrNull(): TeamInviteCandidate? {
    val registrationId = registrationId ?: return null
    val userId = userId ?: return null
    return TeamInviteCandidate(registrationId, userId, displayName.orEmpty())
}

fun AvailableTeamDto.toTeamSummaryOrNull(): TeamSummary? {
    val id = teamId ?: return null
    return TeamSummary(id, teamName.orEmpty(), leaderName.orEmpty(), memberCount ?: 0, capacity ?: 0)
}
