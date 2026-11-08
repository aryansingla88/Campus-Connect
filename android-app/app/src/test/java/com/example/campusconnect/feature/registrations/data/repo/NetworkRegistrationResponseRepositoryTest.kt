package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.*
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkRegistrationResponseRepositoryTest {
    @Test
    fun listsMapsDetailsInFieldOrderAndReviewsThroughHostEndpoints() = kotlinx.coroutines.runBlocking {
        val calls = mutableListOf<String>()
        val api = registrationApiStub { method, args ->
            calls += method
            when (method) {
                "getEventRegistrations" -> Response.success(envelope(listOf(
                    RegistrationSubmissionResponse(40, "TEAM", "Robotics", 3, null, "CONFIRMED", "2026-01-02T03:04:05Z"),
                )))
                "getRegistrationFields" -> Response.success(envelope(listOf(
                    field(8, order = 1), field(7, order = 0),
                )))
                "getEventRegistration" -> Response.success(envelope(
                    RegistrationSubmissionDetailResponse(
                        teamAnswers = listOf(
                            RegistrationAnswerDto(8, "Question 8", "TEXT", "second"),
                            RegistrationAnswerDto(7, "Question 7", "TEXT", "first"),
                        ),
                        members = listOf(RegistrationMemberDto(
                            registrationId = 40,
                            userId = 5,
                            quickFields = listOf(QuickFieldDto("Full Name", "Ari")),
                            individualAnswers = emptyList(),
                        )),
                    ),
                ))
                "approveRegistration" -> {
                    assertEquals(2, args[0])
                    assertEquals(40, args[1])
                    Response.success(envelope(ParticipantRegistrationDto(40, 2, null, null, "CONFIRMED", false, emptyList())))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val registrationRepository = NetworkRegistrationRepository(api)
        val repository = NetworkRegistrationResponseRepository(api, registrationRepository)

        val items = repository.getRegistrations(2).getOrThrow()
        assertEquals(RegistrationResponseType.TEAM, items.single().type)
        assertEquals(RegistrationResponseStatus.CONFIRMED, items.single().status)
        assertEquals(3, items.single().memberCount)
        assertEquals("Robotics", items.single().name)

        val details = repository.getDetails(2, 40).getOrThrow()
        assertEquals(listOf(7, 8), details.teamAnswers.map { it.fieldId })
        assertEquals("Ari", details.members.single().quickFields.single().value)
        assertTrue(repository.review(2, 40, approve = true).isSuccess)
        assertEquals(listOf("getEventRegistrations", "getRegistrationFields", "getEventRegistration", "approveRegistration"), calls)
    }

    @Test
    fun responseApiFailureIsReturnedAsFailure() = kotlinx.coroutines.runBlocking {
        val api = registrationApiStub { method, _ ->
            when (method) {
                "getEventRegistrations" -> Response.success(ApiResponse<List<RegistrationSubmissionResponse>>(false, null, "Organizer access required"))
                else -> error("Unexpected API call: $method")
            }
        }

        val result = NetworkRegistrationResponseRepository(api, NetworkRegistrationRepository(api)).getRegistrations(2)
        assertEquals("Organizer access required", result.exceptionOrNull()?.message)
    }

    private fun field(id: Int, order: Int) = RegistrationFieldResponse(
        id = id,
        fieldLabel = "Question $id",
        fieldType = "TEXT",
        required = false,
        placeholder = null,
        fieldOrder = order,
        individual = false,
        options = emptyList(),
    )

    private fun <T> envelope(data: T) = ApiResponse(true, data, null)
}
