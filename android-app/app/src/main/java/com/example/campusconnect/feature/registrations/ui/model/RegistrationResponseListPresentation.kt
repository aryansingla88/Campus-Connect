package com.example.campusconnect.feature.registrations.ui.model

import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType

internal enum class ResponseTab(val label: String) {
    TOTAL("Total\nRegistrations"),
    INDIVIDUAL("Individual"),
    TEAMS("Teams"),
    PENDING("Pending\nReview"),
}

internal enum class ResponseSort(val label: String) {
    LATEST("Latest first"),
    OLDEST("Oldest first"),
    NAME("Name A–Z"),
}

internal fun List<RegistrationResponseItem>.forResponseList(
    tab: ResponseTab,
    query: String,
    sort: ResponseSort,
    statusFilter: RegistrationResponseStatus?,
): List<RegistrationResponseItem> = asSequence()
    .filter { tab.includes(it) }
    .filter { statusFilter == null || it.status == statusFilter }
    .filter { response ->
        query.isBlank() || response.name.orEmpty().contains(query, ignoreCase = true) ||
            response.email.orEmpty().contains(query, ignoreCase = true)
    }
    .let { responses ->
        when (sort) {
            ResponseSort.LATEST -> responses.sortedByDescending { it.submittedAtEpochMillis ?: Long.MIN_VALUE }
            ResponseSort.OLDEST -> responses.sortedBy { it.submittedAtEpochMillis ?: Long.MAX_VALUE }
            ResponseSort.NAME -> responses.sortedBy { it.name.orEmpty().lowercase() }
        }
    }
    .toList()

internal fun ResponseTab.countIn(responses: List<RegistrationResponseItem>): Int =
    responses.count(::includes)

private fun ResponseTab.includes(response: RegistrationResponseItem): Boolean = when (this) {
    ResponseTab.TOTAL -> true
    ResponseTab.INDIVIDUAL -> response.type == RegistrationResponseType.INDIVIDUAL
    ResponseTab.TEAMS -> response.type == RegistrationResponseType.TEAM
    ResponseTab.PENDING -> response.status == RegistrationResponseStatus.WAITLISTED
}
