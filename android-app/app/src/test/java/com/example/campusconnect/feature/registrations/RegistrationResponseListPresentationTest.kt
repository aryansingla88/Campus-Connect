package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.ui.model.ResponseSort
import com.example.campusconnect.feature.registrations.ui.model.ResponseTab
import com.example.campusconnect.feature.registrations.ui.model.countIn
import com.example.campusconnect.feature.registrations.ui.model.forResponseList
import org.junit.Assert.assertEquals
import org.junit.Test

class RegistrationResponseListPresentationTest {
    private val responses = listOf(
        response(1, "Ari", "ari@example.com", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.WAITLISTED, 100L),
        response(2, "Robotics", "team@example.com", RegistrationResponseType.TEAM, RegistrationResponseStatus.CONFIRMED, 300L),
        response(3, "Zoya", "zoya@example.com", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.REJECTED, 200L),
        response(4, "Other", null, RegistrationResponseType.OTHER, RegistrationResponseStatus.PENDING, 400L),
    )

    @Test fun tabsKeepIndividualTeamAndWaitlistedRulesAndCounts() {
        assertEquals(listOf(3, 1), responses.forResponseList(ResponseTab.INDIVIDUAL, "", ResponseSort.LATEST, null).map { it.id })
        assertEquals(listOf(2), responses.forResponseList(ResponseTab.TEAMS, "", ResponseSort.LATEST, null).map { it.id })
        assertEquals(listOf(1), responses.forResponseList(ResponseTab.PENDING, "", ResponseSort.LATEST, null).map { it.id })
        assertEquals(4, ResponseTab.TOTAL.countIn(responses))
        assertEquals(2, ResponseTab.INDIVIDUAL.countIn(responses))
        assertEquals(1, ResponseTab.TEAMS.countIn(responses))
        assertEquals(1, ResponseTab.PENDING.countIn(responses))
    }

    @Test fun searchFiltersNameAndEmailAndSortsByLatestOldestOrName() {
        assertEquals(listOf(2), responses.forResponseList(ResponseTab.TOTAL, "TEAM@EXAMPLE", ResponseSort.LATEST, null).map { it.id })
        assertEquals(listOf(3, 1), responses.forResponseList(ResponseTab.INDIVIDUAL, "", ResponseSort.LATEST, null).map { it.id })
        assertEquals(listOf(1, 3), responses.forResponseList(ResponseTab.INDIVIDUAL, "", ResponseSort.OLDEST, null).map { it.id })
        assertEquals(listOf(1, 3), responses.forResponseList(ResponseTab.INDIVIDUAL, "", ResponseSort.NAME, null).map { it.id })
    }

    @Test fun explicitStatusFilterCombinesWithSelectedTab() {
        assertEquals(
            listOf(3),
            responses.forResponseList(ResponseTab.INDIVIDUAL, "", ResponseSort.LATEST, RegistrationResponseStatus.REJECTED)
                .map { it.id },
        )
    }

    private fun response(
        id: Int,
        name: String?,
        email: String?,
        type: RegistrationResponseType,
        status: RegistrationResponseStatus,
        submittedAtEpochMillis: Long,
    ) = RegistrationResponseItem(id, name, email, type, status, null, submittedAtEpochMillis = submittedAtEpochMillis)
}
