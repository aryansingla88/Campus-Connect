package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationDetailResponse
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationResponseItemOrNull
import com.example.campusconnect.feature.registrations.data.remote.dto.QuickFieldDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationMemberDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationSubmissionDetailResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationSubmissionResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationResponseMapperTest {
    @Test fun submissionMappingPreservesUnknownsNullsAndParsedTimestamp() {
        assertNull(
            RegistrationSubmissionResponse(null, "TEAM", null, null, null, null, null)
                .toRegistrationResponseItemOrNull(),
        )

        val mapped = RegistrationSubmissionResponse(
            registrationId = 14,
            type = "unknown",
            name = " ",
            teamCount = 3,
            email = null,
            status = "future-status",
            submittedAt = "2026-01-02T03:04:05Z",
        ).toRegistrationResponseItemOrNull()!!
        assertEquals(RegistrationResponseType.OTHER, mapped.type)
        assertNull(mapped.status)
        assertNull(mapped.name)
        assertNull(mapped.email)
        assertEquals(3, mapped.memberCount)
        assertEquals(RegistrationResponseStatus.CONFIRMED, RegistrationSubmissionResponse(
            15, "INDIVIDUAL", "Ari", null, null, "CONFIRMED", null,
        ).toRegistrationResponseItemOrNull()?.status)
        assertEquals(1767323045000L, mapped.submittedAtEpochMillis)
    }

    @Test fun detailMappingDropsMembersWithoutUserIdsAndOrdersAnswersByFieldOrder() {
        val response = RegistrationSubmissionDetailResponse(
            teamAnswers = listOf(
                RegistrationAnswerDto(7, "Second", "TEXT", "b"),
                RegistrationAnswerDto(3, "First", "TEXT", "a"),
                RegistrationAnswerDto(null, null, null, null),
            ),
            members = listOf(
                RegistrationMemberDto(null, null, null, null),
                RegistrationMemberDto(
                    registrationId = 90,
                    userId = 22,
                    quickFields = listOf(QuickFieldDto("Full Name", "  "), QuickFieldDto("Email", null)),
                    individualAnswers = null,
                ),
            ),
        )

        val mapped = response.toRegistrationDetailResponse(mapOf(3 to 0, 7 to 1))
        assertEquals(listOf(3, 7, null), mapped.teamAnswers.map { it.fieldId })
        assertEquals(1, mapped.members.size)
        assertEquals(22, mapped.members.single().userId)
        assertEquals(listOf("—", "—"), mapped.members.single().quickFields.map { it.value })
        assertEquals(emptyList<Int>(), mapped.members.single().individualAnswers.mapNotNull { it.fieldId })
    }
}
