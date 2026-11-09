package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationSubmissionDetailResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationSubmissionResponse
import com.example.campusconnect.feature.registrations.model.QuickFieldValue
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationMemberDetail
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

fun RegistrationSubmissionResponse.toRegistrationResponseItemOrNull(): RegistrationResponseItem? {
    val id = registrationId ?: return null
    val type = when (type?.uppercase()) {
        "TEAM" -> RegistrationResponseType.TEAM
        "INDIVIDUAL" -> RegistrationResponseType.INDIVIDUAL
        else -> RegistrationResponseType.OTHER
    }
    val responseStatus = when (status?.uppercase()) {
        "PENDING" -> RegistrationResponseStatus.PENDING
        "CONFIRMED" -> RegistrationResponseStatus.CONFIRMED
        "REJECTED" -> RegistrationResponseStatus.REJECTED
        "WAITLISTED" -> RegistrationResponseStatus.WAITLISTED
        else -> null
    }
    return RegistrationResponseItem(
        id = id,
        name = name?.takeIf(String::isNotBlank),
        email = email?.takeIf(String::isNotBlank),
        type = type,
        status = responseStatus,
        submittedAt = submittedAt?.let(::formatRegistrationTimestamp),
        memberCount = teamCount,
        submittedAtEpochMillis = submittedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() },
    )
}

fun RegistrationSubmissionDetailResponse.toRegistrationDetailResponse(
    fieldOrder: Map<Int, Int>,
) = RegistrationDetailResponse(
    teamAnswers = teamAnswers.orEmpty().toResponseAnswers(fieldOrder),
    members = members.orEmpty().mapNotNull { memberValue ->
        val member = memberValue ?: return@mapNotNull null
        val userId = member.userId ?: return@mapNotNull null
        RegistrationMemberDetail(
            userId = userId,
            quickFields = member.quickFields.orEmpty().mapNotNull { field ->
                field.field?.takeIf(String::isNotBlank)?.let { label ->
                    QuickFieldValue(label, field.value.orEmpty().ifBlank { "—" })
                }
            },
            individualAnswers = member.individualAnswers.orEmpty().toResponseAnswers(fieldOrder),
        )
    },
)

private fun List<RegistrationAnswerDto>.toResponseAnswers(fieldOrder: Map<Int, Int>): List<ResponseAnswer> =
    map { answer ->
        ResponseAnswer(
            fieldId = answer.fieldId,
            fieldLabel = answer.fieldLabel?.takeIf(String::isNotBlank) ?: "Question",
            fieldType = answer.fieldType,
            answer = answer.answer,
        )
    }.sortedBy { answer -> answer.fieldId?.let(fieldOrder::get) ?: Int.MAX_VALUE }

private fun formatRegistrationTimestamp(value: String): String = runCatching {
    val instant = Instant.parse(value)
    DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
        .withZone(ZoneId.systemDefault())
        .format(instant)
}.getOrDefault(value)
