package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.RegistrationAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionDetailResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionResponse
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

object RegistrationResponseMapper {
    fun toModel(response: RegistrationSubmissionResponse): RegistrationResponseItem? {
        val id = response.registrationId ?: return null
        val type = when (response.type?.uppercase()) {
            "TEAM" -> RegistrationResponseType.TEAM
            "INDIVIDUAL" -> RegistrationResponseType.INDIVIDUAL
            else -> RegistrationResponseType.OTHER
        }
        val status = when (response.status?.uppercase()) {
            "PENDING" -> RegistrationResponseStatus.PENDING
            "CONFIRMED" -> RegistrationResponseStatus.CONFIRMED
            "REJECTED" -> RegistrationResponseStatus.REJECTED
            "WAITLISTED" -> RegistrationResponseStatus.WAITLISTED
            else -> null
        }
        return RegistrationResponseItem(
            id = id,
            name = response.name?.takeIf(String::isNotBlank),
            email = response.email?.takeIf(String::isNotBlank),
            type = type,
            status = status,
            submittedAt = response.submittedAt?.let(::formatTimestamp),
            memberCount = response.teamCount,
            submittedAtEpochMillis = response.submittedAt?.let {
                runCatching { Instant.parse(it).toEpochMilli() }.getOrNull()
            },
        )
    }

    fun toDetailModel(
        response: RegistrationSubmissionDetailResponse,
        fieldOrderById: Map<Int, Int>,
    ): RegistrationDetailResponse = RegistrationDetailResponse(
        teamAnswers = response.teamAnswers.orEmpty().ordered(fieldOrderById),
        members = response.members.orEmpty().mapNotNull { member ->
            val userId = member?.userId ?: return@mapNotNull null
            RegistrationMemberDetail(
                userId = userId,
                quickFields = member.quickFields.orEmpty().mapNotNull { field ->
                    field.field?.takeIf(String::isNotBlank)?.let { label ->
                        QuickFieldValue(label, field.value.orEmpty().ifBlank { "—" })
                    }
                },
                individualAnswers = member.individualAnswers.orEmpty().ordered(fieldOrderById),
            )
        },
    )

    private fun List<RegistrationAnswerDto>.ordered(fieldOrderById: Map<Int, Int>): List<ResponseAnswer> =
        map { answer ->
            ResponseAnswer(
                fieldId = answer.fieldId,
                fieldLabel = answer.fieldLabel?.takeIf(String::isNotBlank) ?: "Question",
                fieldType = answer.fieldType,
                answer = answer.answer,
            )
        }.sortedBy { answer -> answer.fieldId?.let(fieldOrderById::get) ?: Int.MAX_VALUE }

    private fun formatTimestamp(value: String): String = runCatching {
        val instant = Instant.parse(value)
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrDefault(value)
}
