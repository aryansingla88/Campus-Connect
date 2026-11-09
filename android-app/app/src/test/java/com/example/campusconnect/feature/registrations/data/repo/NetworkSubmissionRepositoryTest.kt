package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.*
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.AttachedFile
import com.example.campusconnect.feature.registrations.model.SubmissionContext
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkSubmissionRepositoryTest {
    @Test
    fun retrievesTeamQuestionsAndSavedAnswersThenPersistsAndFinalizes() = kotlinx.coroutines.runBlocking {
        var saved: SaveAnswersRequest? = null
        val calls = mutableListOf<String>()
        val api = registrationApiStub { method, args ->
            calls += method
            when (method) {
                "getParticipantRegistrationQuestions" -> {
                    assertEquals(false, args[1])
                    Response.success(envelope(listOf(ParticipantQuestionDto(8, "Team idea", "TEXT", true, null, 0, false, emptyList()))))
                }
                "getParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(30, 4, 12, "Robotics", "PENDING", true,
                        listOf(ParticipantSavedAnswerDto(8, "saved draft", "TEAM"))),
                ))
                "saveParticipantAnswers" -> {
                    saved = args[1] as SaveAnswersRequest
                    Response.success(envelope(ParticipantRegistrationDto(30, 4, 12, "Robotics", "PENDING", true, emptyList())))
                }
                "finalizeParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(30, 4, 12, "Robotics", "CONFIRMED", false, emptyList()),
                ))
                else -> error("Unexpected API call: $method")
            }
        }
        val registrations = NetworkRegistrationRepository(api)
        val repository = NetworkSubmissionRepository(api, registrations)

        val questions = repository.getTeamQuestions(4, 30).getOrThrow()
        assertEquals(listOf(8), questions.questions.map { it.backendId })
        assertEquals(mapOf(8 to "saved draft"), questions.savedAnswers)

        val context = SubmissionContext(4, 30, "Event", ParticipationMode.CREATE_TEAM, 12)
        val receipt = repository.submit(SubmissionRequest(context, mapOf(8 to "final answer"), emptyMap())).getOrThrow()
        assertEquals("#30", receipt.registrationCode)
        assertEquals("Confirmed", receipt.status)
        assertEquals("TEAM", saved?.type)
        assertEquals("final answer", saved?.answers?.single()?.answer)
        assertEquals(
            listOf("getParticipantRegistrationQuestions", "getParticipantRegistration", "saveParticipantAnswers", "finalizeParticipantRegistration"),
            calls,
        )
    }

    @Test
    fun finalSubmissionSurfacesApiFailure() = kotlinx.coroutines.runBlocking {
        var finalized = false
        val api = registrationApiStub { method, _ ->
            when (method) {
                "finalizeParticipantRegistration" -> {
                    finalized = true
                    Response.success(ApiResponse<ParticipantRegistrationDto>(false, null, "Finalize failed"))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val repository = NetworkSubmissionRepository(api, NetworkRegistrationRepository(api))
        val base = SubmissionContext(4, 30, "Event", ParticipationMode.SOLO)
        val failed = repository.submit(SubmissionRequest(base, emptyMap(), emptyMap()))
        assertEquals("Finalize failed", failed.exceptionOrNull()?.message)
        assertTrue(finalized)
    }

    @Test
    fun unsupportedFileUploadIsRejectedBeforeNetworkSubmission() = kotlinx.coroutines.runBlocking {
        var finalized = false
        val api = registrationApiStub { method, _ ->
            when (method) {
                "finalizeParticipantRegistration" -> {
                    finalized = true
                    Response.success(envelope(ParticipantRegistrationDto(30, 4, null, null, "CONFIRMED", false, emptyList())))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val context = SubmissionContext(4, 30, "Event", ParticipationMode.SOLO)
        val file = AttachedFile("content://attachment", "brief.pdf", 100L)

        val result = NetworkSubmissionRepository(api, NetworkRegistrationRepository(api))
            .submit(SubmissionRequest(context, emptyMap(), mapOf(8 to file)))

        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("File upload questions are not supported yet"))
        assertTrue(!finalized)
    }

    private fun <T> envelope(data: T) = ApiResponse(true, data, null)
}
