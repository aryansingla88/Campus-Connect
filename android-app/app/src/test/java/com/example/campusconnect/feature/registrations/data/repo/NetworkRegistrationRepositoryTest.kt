package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantQuestionDto
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantQuestionOptionDto
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantRegistrationConfigDto
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantRegistrationDto
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantSavedAnswerDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.SaveAnswersRequest
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationConfigLoadResult
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkRegistrationRepositoryTest {
    @Test
    fun initializationLoadsRulesIndividualQuestionsAndSavedDraftAnswers() = kotlinx.coroutines.runBlocking {
        val calls = mutableListOf<String>()
        val api = registrationApiStub { method, args ->
            calls += method
            when (method) {
                "initializeParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(51, 9, null, null, "PENDING", true, emptyList()),
                ))
                "getParticipantRegistrationConfig" -> Response.success(envelope(
                    ParticipantRegistrationConfigDto(9, 1, 5),
                ))
                "getParticipantRegistrationQuestions" -> {
                    assertEquals(true, args[1])
                    Response.success(envelope(listOf(questionDto(72, true))))
                }
                "getParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(
                        51, 9, null, null, "PENDING", true,
                        listOf(ParticipantSavedAnswerDto(72, "Ada", "INDIVIDUAL")),
                    ),
                ))
                else -> error("Unexpected API call: $method")
            }
        }

        val result = NetworkRegistrationRepository(api).createRegistration(9)

        assertTrue(result.isSuccess)
        val draft = result.getOrThrow()
        assertEquals(51, draft.registrationId)
        assertEquals(listOf(72), draft.questions.map { it.backendId })
        assertEquals(mapOf(72 to "Ada"), draft.initialAnswers)
        assertTrue(draft.allowSolo)
        assertTrue(draft.allowTeam)
        assertEquals(
            listOf("initializeParticipantRegistration", "getParticipantRegistrationConfig", "getParticipantRegistrationQuestions", "getParticipantRegistration"),
            calls,
        )
    }

    @Test
    fun initializationReturnsApiLevelFailureAndEmptyQuestionBodiesAreReported() = kotlinx.coroutines.runBlocking {
        val failedApi = registrationApiStub { method, _ ->
            when (method) {
                "initializeParticipantRegistration" -> Response.success(ApiResponse<ParticipantRegistrationDto>(false, null, "Registration is closed."))
                else -> error("Unexpected API call: $method")
            }
        }
        val failed = NetworkRegistrationRepository(failedApi).createRegistration(9)
        assertEquals("Registration is closed.", failed.exceptionOrNull()?.message)

        val emptyApi = registrationApiStub { method, _ ->
            when (method) {
                "getRegistrationFields" -> Response.success(ApiResponse<List<RegistrationFieldResponse>>(true, null, null))
                else -> error("Unexpected API call: $method")
            }
        }
        val empty = NetworkRegistrationRepository(emptyApi).loadQuestions(9)
        assertFalse(empty.isSuccess)
        assertEquals("The server returned no registration questions.", empty.exceptionOrNull()?.message)
    }

    @Test
    fun httpQuestionFailureAndIndividualAnswerPayloadUseSharedHandling() = kotlinx.coroutines.runBlocking {
        val httpFailureApi = registrationApiStub { method, _ ->
            when (method) {
                "getRegistrationFields" -> Response.error<ApiResponse<List<RegistrationFieldResponse>>>(
                    503,
                    "offline".toResponseBody(),
                )
                else -> error("Unexpected API call: $method")
            }
        }
        val httpFailure = NetworkRegistrationRepository(httpFailureApi).loadQuestions(9)
        assertFalse(httpFailure.isSuccess)
        assertTrue(httpFailure.exceptionOrNull()?.message.orEmpty().contains("HTTP 503"))

        var request: SaveAnswersRequest? = null
        val saveApi = registrationApiStub { method, args ->
            when (method) {
                "saveParticipantAnswers" -> {
                    request = args[1] as SaveAnswersRequest
                    Response.success(envelope(ParticipantRegistrationDto(51, 9, null, null, "PENDING", true, emptyList())))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val saved = NetworkRegistrationRepository(saveApi).saveIndividualAnswers(51, mapOf(72 to "Ada"))
        assertTrue(saved.isSuccess)
        assertEquals("INDIVIDUAL", request?.type)
        assertEquals(72, request?.answers?.single()?.fieldId)
        assertEquals("Ada", request?.answers?.single()?.answer)
    }

    @Test
    fun questionScopesPreserveTeamClassificationAndOrdering() = kotlinx.coroutines.runBlocking {
        val api = registrationApiStub { method, args ->
            when (method) {
                "getParticipantRegistrationQuestions" -> {
                    assertEquals(false, args[1])
                    Response.success(envelope(listOf(questionDto(80, false))))
                }
                else -> error("Unexpected API call: $method")
            }
        }

        val result = NetworkRegistrationRepository(api).loadQuestions(9, QuestionScope.TEAM).getOrThrow()

        assertEquals(QuestionScope.TEAM, result.single().scope)
        assertEquals(80, result.single().backendId)
    }

    @Test
    fun questionPersistencePreservesBackendIdsAndCreatesUnsavedFields() = kotlinx.coroutines.runBlocking {
        var outgoing: List<com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldRequest>? = null
        val api = registrationApiStub { method, args ->
            when (method) {
                "updateRegistrationFields" -> {
                    outgoing = args[1] as List<com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldRequest>
                    Response.success(envelope(listOf(
                        fieldResponse(9, "Saved", 0), fieldResponse(10, "New", 1),
                    )))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val questions = listOf(
            RegistrationQuestion(label = "New", type = QuestionType.Known.TEXT, scope = QuestionScope.TEAM),
            RegistrationQuestion(backendId = 9, label = "Saved", scope = QuestionScope.INDIVIDUAL),
        )

        val saved = NetworkRegistrationRepository(api).saveQuestions(9, questions).getOrThrow()

        assertEquals(listOf(9, null), outgoing?.map { it.id })
        assertEquals(listOf(9, 10), saved.map { it.backendId })
    }

    @Test
    fun configurationAndVoidLifecycleEndpointsUseSharedEnvelopeHandling() = kotlinx.coroutines.runBlocking {
        val calls = mutableListOf<String>()
        val api = registrationApiStub { method, _ ->
            calls += method
            when (method) {
                "getRegistrationConfig", "updateRegistrationConfig" -> Response.success(envelope(
                    com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationConfigResponse(
                        9, 1, 5, 0L, true, null, null,
                    ),
                ))
                "cancelParticipantRegistration" -> Response.success(ApiResponse<Unit>(true, null, "Cancelled"))
                else -> error("Unexpected API call: $method")
            }
        }
        val repository = NetworkRegistrationRepository(api)

        assertTrue(repository.loadSettings(9) is RegistrationConfigLoadResult.Existing)
        assertTrue(repository.saveSettings(9, com.example.campusconnect.feature.registrations.model.RegistrationSettings(), exists = true).isSuccess)
        assertTrue(repository.cancelRegistration(9).isSuccess)
        assertEquals(listOf("getRegistrationConfig", "updateRegistrationConfig", "cancelParticipantRegistration"), calls)
    }

    private fun questionDto(id: Int, individual: Boolean) = ParticipantQuestionDto(
        id = id,
        fieldLabel = "Question $id",
        fieldType = "TEXT",
        required = true,
        placeholder = null,
        fieldOrder = 1,
        individual = individual,
        options = listOf(ParticipantQuestionOptionDto("Option", 0)),
    )

    private fun <T> envelope(data: T) = ApiResponse(true, data, null)

    private fun fieldResponse(id: Int, label: String, order: Int) = RegistrationFieldResponse(
        id, label, "TEXT", false, null, order, false, emptyList(),
    )
}
