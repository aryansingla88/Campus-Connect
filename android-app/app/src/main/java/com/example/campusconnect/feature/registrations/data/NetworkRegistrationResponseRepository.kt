package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.registrations.data.remote.RegistrationAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionDetailResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationSubmissionResponse
import com.example.campusconnect.feature.registrations.model.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NetworkRegistrationResponseRepository(
    private val api: EventsApi = RetrofitClient.eventsApi,
) : RegistrationResponseRepository {
    @Volatile private var fieldOrderById: Map<Int, Int> = emptyMap()

    override suspend fun getRegistrations(eventId: Int): Result<List<RegistrationResponseItem>> = try {
        fieldOrderById = emptyMap()
        val response = api.getEventRegistrations(eventId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load registrations (HTTP ${response.code()})."
            ))
        } else {
            // The detail response has labels but no field order; cache the existing
            // field definitions for answer ordering without making detail calls repeat it.
            val fieldsResponse = runCatching { api.getRegistrationFields(eventId).body() }.getOrNull()
            if (fieldsResponse?.success == true) {
                fieldOrderById = fieldsResponse.data.orEmpty().associate { it.id to it.fieldOrder }
            }
            Result.success(body.data.mapNotNull { it.toModel() })
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    override suspend fun getDetails(eventId: Int, registrationId: Int): Result<RegistrationDetailResponse> = try {
        val response = api.getEventRegistration(eventId, registrationId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load this registration's responses (HTTP ${response.code()})."
            ))
        } else {
            Result.success(body.data.toModel(fieldOrderById))
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun RegistrationSubmissionResponse.toModel(): RegistrationResponseItem? {
        val id = registrationId ?: return null
        val typeValue = type?.uppercase()
        val type = when (typeValue) {
            "TEAM" -> RegistrationResponseType.TEAM
            "INDIVIDUAL" -> RegistrationResponseType.INDIVIDUAL
            else -> RegistrationResponseType.OTHER
        }
        val status = when (status?.uppercase()) {
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
            status = status,
            submittedAt = submittedAt?.let(::formatTimestamp),
            memberCount = teamCount,
            submittedAtEpochMillis = submittedAt?.let { runCatching { Instant.parse(it).toEpochMilli() }.getOrNull() },
        )
    }

    private fun RegistrationSubmissionDetailResponse.toModel(
        fieldOrder: Map<Int, Int>,
    ) = RegistrationDetailResponse(
        teamAnswers = teamAnswers.orEmpty().ordered(fieldOrder),
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
                individualAnswers = member.individualAnswers.orEmpty().ordered(fieldOrder),
            )
        },
    )

    private fun List<RegistrationAnswerDto>.ordered(fieldOrder: Map<Int, Int>): List<ResponseAnswer> =
        map { answer ->
            ResponseAnswer(
                fieldId = answer.fieldId,
                fieldLabel = answer.fieldLabel?.takeIf(String::isNotBlank) ?: "Question",
                fieldType = answer.fieldType,
                answer = answer.answer,
            )
        }.sortedBy { answer -> answer.fieldId?.let(fieldOrder::get) ?: Int.MAX_VALUE }

    private fun formatTimestamp(value: String): String = runCatching {
        val instant = Instant.parse(value)
        DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrDefault(value)
}
