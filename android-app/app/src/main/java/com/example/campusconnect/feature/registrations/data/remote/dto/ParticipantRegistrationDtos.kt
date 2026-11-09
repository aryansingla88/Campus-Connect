package com.example.campusconnect.feature.registrations.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ParticipantRegistrationConfigDto(
    val eventId: Int?,
    val minTeamMembers: Int?,
    val maxTeamMembers: Int?,
)

data class ParticipantRegistrationDto(
    val registrationId: Int?,
    val eventId: Int?,
    val teamId: Int?,
    val teamName: String?,
    val status: String?,
    val canResume: Boolean?,
    val answers: List<ParticipantSavedAnswerDto>?,
)

data class ParticipantSavedAnswerDto(
    val fieldId: Int?,
    val answer: String?,
    val type: String?,
)

data class ParticipantQuestionDto(
    val id: Int?,
    val fieldLabel: String?,
    val fieldType: String?,
    val required: Boolean?,
    val placeholder: String?,
    val fieldOrder: Int?,
    val individual: Boolean?,
    val options: List<ParticipantQuestionOptionDto>?,
)

data class ParticipantQuestionOptionDto(val optionValue: String?, val optionOrder: Int?)

data class AvailableTeamDto(
    val teamId: Int?,
    val teamName: String?,
    val memberCount: Int?,
    val capacity: Int?,
    val leaderName: String?,
)

data class TeamRequestDto(
    val requestId: Int?,
    val teamId: Int?,
    val teamName: String?,
    val registrationId: Int?,
    val participantId: Int?,
    val participantName: String?,
    val initiatedBy: String?,
    val status: String?,
    val createdAt: String?,
)

data class TeamIdRequest(val teamId: Int)
data class CreateTeamRequest(val teamName: String)
data class CreateInvitationRequest(val registrationId: Int)
data class SaveAnswersRequest(
    val type: String,
    val answers: List<SaveAnswerDto>,
)
data class SaveAnswerDto(val fieldId: Int, val answer: String)

data class ParticipantTeamMemberDto(
    val userId: Int?,
    val username: String?,
    val status: String?,
)

data class TeamCandidateDto(
    val registrationId: Int?,
    val userId: Int?,
    val displayName: String?,
)

data class TeamPortalResponseDto(
    val teamId: Int?,
    val leaderUserId: Int?,
    val teamName: String?,
    val leaderName: String?,
    val memberCount: Int?,
    val minMembers: Int?,
    val maxMembers: Int?,
    val status: String?,
    val currentUserIsLeader: Boolean?,
    val members: List<TeamMemberResponseDto>?,
    val pendingJoinRequests: List<TeamRequestDto>?,
)
data class TeamMemberResponseDto(
    val registrationId: Int?,
    val userId: Int?,
    val displayName: String?,
    val status: String?,
)

/** Response shapes for the legacy team detail endpoints kept for API compatibility. */
data class LegacyTeamMemberDto(
    val registrationId: Int?,
    val userId: Int?,
    val name: String?,
    val email: String? = null,
    val role: String? = null,
    val program: String? = null,
)

data class LegacyTeamInviteCandidateDto(
    val registrationId: Int?,
    val participantId: Int?,
    val name: String?,
    val email: String? = null,
    val program: String? = null,
)
