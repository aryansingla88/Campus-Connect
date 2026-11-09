package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.mapper.toRegistrationResponseItemOrNull
import com.example.campusconnect.feature.registrations.data.remote.dto.*
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationRepository
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationResponseRepository
import com.example.campusconnect.feature.registrations.data.repo.registrationApiStub
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkRegistrationResponseRepositoryTest {
    @Test fun detailsUseCurrentQuestionOrderMetadata() = runBlocking {
        val api = registrationApiStub { method, _ ->
            when (method) {
                "getEventRegistrations" -> Response.success(envelope(emptyList<RegistrationSubmissionResponse>()))
                "getRegistrationFields" -> Response.success(envelope(listOf(field(10, 0), field(20, 1))))
                "getEventRegistration" -> Response.success(envelope(detail(20, 10)))
                else -> error("Unexpected API call: $method")
            }
        }
        val repository = NetworkRegistrationResponseRepository(api, NetworkRegistrationRepository(api))

        repository.getRegistrations(1).getOrThrow()
        val result = repository.getDetails(1, 7).getOrThrow()

        assertEquals(listOf(10, 20), result.teamAnswers.map { it.fieldId })
    }

    @Test fun failedMetadataRefreshDoesNotKeepStaleOrder() = runBlocking {
        var fieldRequestCount = 0
        val api = registrationApiStub { method, _ ->
            when (method) {
                "getEventRegistrations" -> Response.success(envelope(emptyList<RegistrationSubmissionResponse>()))
                "getRegistrationFields" -> {
                    fieldRequestCount++
                    if (fieldRequestCount == 1) Response.success(envelope(listOf(field(10, 0), field(20, 1))))
                    else Response.success(ApiResponse<List<RegistrationFieldResponse>>(false, null, "unavailable"))
                }
                "getEventRegistration" -> Response.success(envelope(detail(20, 10)))
                else -> error("Unexpected API call: $method")
            }
        }
        val repository = NetworkRegistrationResponseRepository(api, NetworkRegistrationRepository(api))

        repository.getRegistrations(1).getOrThrow()
        repository.getRegistrations(1).getOrThrow()
        val result = repository.getDetails(1, 7).getOrThrow()

        // Unknown field order keeps the backend's source order deterministically.
        assertEquals(listOf(20, 10), result.teamAnswers.map { it.fieldId })
    }

    @Test fun registrationItemsWithMissingIdsAreDroppedByTheSharedMapper() {
        assertTrue(RegistrationSubmissionResponse(null, "TEAM", null, null, null, null, null)
            .toRegistrationResponseItemOrNull() == null)
    }

    private fun field(id: Int, order: Int) = RegistrationFieldResponse(
        id = id,
        fieldLabel = "Question $id",
        fieldType = "TEXT",
        required = false,
        placeholder = null,
        fieldOrder = order,
        individual = false,
    )

    private fun detail(vararg ids: Int) = RegistrationSubmissionDetailResponse(
        teamAnswers = ids.map { id -> RegistrationAnswerDto(id, "Question $id", "TEXT", "answer $id") },
        members = emptyList(),
    )

    private fun <T> envelope(data: T) = ApiResponse(true, data, null)
}
