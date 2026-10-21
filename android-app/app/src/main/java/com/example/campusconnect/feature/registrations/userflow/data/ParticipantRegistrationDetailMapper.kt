package com.example.campusconnect.feature.registrations.userflow.data

import com.example.campusconnect.feature.registrations.model.QuickFieldValue
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationMemberDetail
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SavedRegistrationDetails

/** Host-detail models backed by the participant flow's fake registration record. */
data class ParticipantRegistrationDetailData(
    val response: RegistrationResponseItem,
    val detail: RegistrationDetailResponse,
)

fun SavedRegistrationDetails.toParticipantRegistrationDetail(): ParticipantRegistrationDetailData {
    val teamRegistration = teamId != null || mode == ParticipationMode.CREATE_TEAM
    val quickFields = listOf(
        1 to "Full Name",
        2 to "Email",
        3 to "Phone",
    ).mapNotNull { (id, label) -> participantAnswers[id]?.let { QuickFieldValue(label, it) } }
    val individualAnswers = participantAnswers.filterKeys { it !in 1..3 && it != 10 }.toResponseAnswers()
    val sharedAnswers = buildList {
        participantAnswers[10]?.let { add(ResponseAnswer(10, "What do you hope to get from this event?", "TEXT", it)) }
        addAll(teamAnswers.toResponseAnswers())
    }
    val name = participantAnswers[1]?.takeIf(String::isNotBlank)
    val email = participantAnswers[2]?.takeIf(String::isNotBlank)

    return ParticipantRegistrationDetailData(
        response = RegistrationResponseItem(
            id = registrationId,
            name = name,
            email = email,
            type = if (teamRegistration) RegistrationResponseType.TEAM else RegistrationResponseType.INDIVIDUAL,
            status = RegistrationResponseStatus.CONFIRMED,
            submittedAt = "Just now",
            memberCount = teamId?.let(FakeRegistrationStore::teamSummary)?.memberCount
                ?: if (teamRegistration) 1 else null,
            teamName = teamName,
        ),
        detail = RegistrationDetailResponse(
            teamAnswers = sharedAnswers,
            members = listOf(
                RegistrationMemberDetail(
                    userId = participantId,
                    quickFields = quickFields,
                    individualAnswers = individualAnswers,
                ),
            ),
        ),
    )
}

private fun Map<Int, String>.toResponseAnswers(): List<ResponseAnswer> = map { (id, value) ->
    val (label, type) = when (id) {
        4 -> "Branch" to "SELECT"
        5 -> "Year" to "SELECT"
        6 -> "Skills" to "MULTISELECT"
        11 -> "What is your team's domain?" to "SELECT"
        12 -> "Briefly describe your idea" to "TEXT"
        13 -> "Technologies you plan to use" to "MULTISELECT"
        else -> "Question $id" to "TEXT"
    }
    ResponseAnswer(fieldId = id, fieldLabel = label, fieldType = type, answer = value)
}
