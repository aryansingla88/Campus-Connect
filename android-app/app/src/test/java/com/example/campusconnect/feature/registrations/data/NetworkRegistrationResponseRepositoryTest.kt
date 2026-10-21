package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.*
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationResponseRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkRegistrationResponseRepositoryTest {
    @Test
    fun detailLoadsFieldOrderingWithoutAListRequest() = runBlocking {
        val api = FakeRegistrationApi(
            fieldOrders = mapOf(1 to listOf(field(10, 0), field(20, 1))),
            details = mapOf(1 to detail(20, 10)),
        )

        val result = NetworkRegistrationResponseRepository(api).getDetails(1, 7)

        assertTrue(result.isSuccess)
        assertEquals(listOf(10, 20), result.getOrThrow().teamAnswers.map { it.fieldId })
        assertEquals(0, api.registrationListCalls)
    }

    @Test
    fun loadingEventBListDoesNotChangeEventADetailOrdering() = runBlocking {
        val api = FakeRegistrationApi(
            fieldOrders = mapOf(
                1 to listOf(field(10, 0), field(20, 1)),
                2 to listOf(field(10, 1), field(20, 0)),
            ),
            details = mapOf(1 to detail(20, 10)),
        )
        val repository = NetworkRegistrationResponseRepository(api)

        repository.getRegistrations(1)
        repository.getRegistrations(2)
        val result = repository.getDetails(1, 7).getOrThrow()

        assertEquals(listOf(10, 20), result.teamAnswers.map { it.fieldId })
    }

    @Test
    fun refreshedFieldMetadataReplacesStaleOrderForTheSameEvent() = runBlocking {
        val api = FakeRegistrationApi(
            fieldOrders = mapOf(1 to listOf(field(10, 0), field(20, 1))),
            details = mapOf(1 to detail(20, 10)),
        )
        val repository = NetworkRegistrationResponseRepository(api)
        repository.getRegistrations(1)
        api.setFieldOrder(1, listOf(field(10, 2), field(20, 0)))
        repository.getRegistrations(1)

        val result = repository.getDetails(1, 7).getOrThrow()
        assertEquals(listOf(20, 10), result.teamAnswers.map { it.fieldId })
    }

    @Test
    fun failedMetadataRefreshDoesNotReuseStaleEventOrder() = runBlocking {
        val api = FakeRegistrationApi(
            fieldOrders = mapOf(1 to listOf(field(10, 0), field(20, 1))),
            details = mapOf(1 to detail(20, 10)),
        )
        val repository = NetworkRegistrationResponseRepository(api)
        repository.getRegistrations(1)
        api.failFieldOrder = true
        repository.getRegistrations(1)

        val result = repository.getDetails(1, 7).getOrThrow()
        assertEquals(listOf(20, 10), result.teamAnswers.map { it.fieldId })
    }

    @Test
    fun missingFieldMetadataKeepsAnswerOrderDeterministic() = runBlocking {
        val api = FakeRegistrationApi(details = mapOf(1 to detail(20, 10)))

        val result = NetworkRegistrationResponseRepository(api).getDetails(1, 7)

        assertTrue(result.isSuccess)
        // Unknown order values sort last; stable sorting preserves source order.
        assertEquals(listOf(20, 10), result.getOrThrow().teamAnswers.map { it.fieldId })
    }

    private class FakeRegistrationApi(
        fieldOrders: Map<Int, List<RegistrationFieldResponse>> = emptyMap(),
        private val details: Map<Int, RegistrationSubmissionDetailResponse> = emptyMap(),
    ) : RegistrationApi {
        private val fieldOrders = fieldOrders.toMutableMap()
        var registrationListCalls = 0
        var failFieldOrder = false

        fun setFieldOrder(eventId: Int, fields: List<RegistrationFieldResponse>) { fieldOrders[eventId] = fields }

        override suspend fun getRegistrationFields(eventId: Int) = if (failFieldOrder) {
            Response.error<ApiResponse<List<RegistrationFieldResponse>>>(500, okhttp3.ResponseBody.create(null, ""))
        } else Response.success(
                ApiResponse(success = true, data = fieldOrders[eventId], message = null),
            )

        override suspend fun getEventRegistration(eventId: Int, registrationId: Int) = Response.success(
            ApiResponse(success = true, data = details[eventId], message = null),
        )

        override suspend fun getEventRegistrations(eventId: Int): Response<ApiResponse<List<RegistrationSubmissionResponse>>> {
            registrationListCalls++
            return Response.success(ApiResponse(success = true, data = emptyList(), message = null))
        }

        override suspend fun getRegistrationConfig(eventId: Int): Response<ApiResponse<RegistrationConfigResponse>> = unsupported()
        override suspend fun createRegistrationConfig(eventId: Int, request: RegistrationConfigRequest): Response<ApiResponse<RegistrationConfigResponse>> = unsupported()
        override suspend fun updateRegistrationConfig(eventId: Int, request: RegistrationConfigRequest): Response<ApiResponse<RegistrationConfigResponse>> = unsupported()
        override suspend fun updateQuickFieldsConfig(eventId: Int, request: QuickFieldsConfigRequest): Response<ApiResponse<RegistrationConfigResponse>> = unsupported()
        override suspend fun createRegistrationFields(eventId: Int, fields: List<RegistrationFieldRequest>): Response<ApiResponse<List<RegistrationFieldResponse>>> = unsupported()
        override suspend fun updateRegistrationFields(eventId: Int, fields: List<RegistrationFieldRequest>): Response<ApiResponse<List<RegistrationFieldResponse>>> = unsupported()
        override suspend fun initializeParticipantRegistration(eventId: Int): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun getParticipantRegistration(eventId: Int): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun cancelParticipantRegistration(eventId: Int): Response<ApiResponse<Unit>> = unsupported()
        override suspend fun getMyRegistrations(): Response<ApiResponse<List<ParticipantRegistrationResponse>>> = unsupported()
        override suspend fun getParticipantQuestions(eventId: Int, individual: Boolean): Response<ApiResponse<List<RegistrationFieldResponse>>> = unsupported()
        override suspend fun getAvailableTeams(eventId: Int, search: String?): Response<ApiResponse<List<AvailableTeamDto>>> = unsupported()
        override suspend fun isTeamNameAvailable(eventId: Int, teamName: String): Response<ApiResponse<Boolean>> = unsupported()
        override suspend fun getTeamSummary(teamId: Int): Response<ApiResponse<AvailableTeamDto>> = unsupported()
        override suspend fun saveParticipantAnswers(registrationId: Int, request: ParticipantAnswersRequest): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun createParticipantTeam(registrationId: Int, request: CreateParticipantTeamRequest): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun finalizeParticipantRegistration(registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun createTeamJoinRequest(registrationId: Int, request: TeamRequestCreateDto): Response<ApiResponse<TeamRequestDto>> = unsupported()
        override suspend fun createTeamInvitation(teamId: Int, request: TeamInvitationCreateDto): Response<ApiResponse<TeamRequestDto>> = unsupported()
        override suspend fun getMyTeamRequests(): Response<ApiResponse<List<TeamRequestDto>>> = unsupported()
        override suspend fun getTeamRequests(teamId: Int): Response<ApiResponse<List<TeamRequestDto>>> = unsupported()
        override suspend fun getTeamMembers(teamId: Int): Response<ApiResponse<List<TeamMemberDto>>> = unsupported()
        override suspend fun getTeamInviteCandidates(teamId: Int): Response<ApiResponse<List<TeamInviteCandidateDto>>> = unsupported()
        override suspend fun acceptTeamRequest(requestId: Int): Response<ApiResponse<TeamRequestDto>> = unsupported()
        override suspend fun rejectTeamRequest(requestId: Int): Response<ApiResponse<TeamRequestDto>> = unsupported()
        override suspend fun cancelTeamRequest(requestId: Int): Response<ApiResponse<TeamRequestDto>> = unsupported()
        override suspend fun approveRegistration(eventId: Int, registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()
        override suspend fun rejectRegistration(eventId: Int, registrationId: Int): Response<ApiResponse<ParticipantRegistrationResponse>> = unsupported()

        private fun <T> unsupported(): T = error("Not used by this repository test")
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
        teamAnswers = ids.map { id ->
            RegistrationAnswerDto(id, "Question $id", "TEXT", "answer $id")
        },
        members = emptyList(),
    )
}
