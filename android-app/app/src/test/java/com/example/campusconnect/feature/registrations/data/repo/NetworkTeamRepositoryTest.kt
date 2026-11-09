package com.example.campusconnect.feature.registrations.data.repo

import com.example.campusconnect.core.network.ApiResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.*
import com.example.campusconnect.feature.registrations.model.TeamAssignment
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class NetworkTeamRepositoryTest {
    @Test
    fun eventPortalFiltersUserWideRequestsByRegistrationAndSearchesEventTeams() = kotlinx.coroutines.runBlocking {
        val api = registrationApiStub { method, args ->
            when (method) {
                "getParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(30, 4, null, null, "PENDING", true, emptyList()),
                ))
                "getMyTeamRequests" -> Response.success(envelope(listOf(
                    request(1, registrationId = 30, teamId = 11, initiator = "LEADER"),
                    request(2, registrationId = 30, teamId = 12, initiator = "PARTICIPANT"),
                    request(3, registrationId = 99, teamId = 13, initiator = "LEADER"),
                )))
                "getAvailableRegistrationTeams" -> {
                    assertEquals(4, args[0])
                    assertEquals("robot", args[1])
                    Response.success(envelope(listOf(AvailableTeamDto(12, "Robotics", 2, 5, "Ari"))))
                }
                else -> error("Unexpected API call: $method")
            }
        }

        val portal = NetworkTeamRepository(api, NetworkRegistrationRepository(api))
            .getTeamPortal(4, 30, "robot").getOrThrow()

        assertEquals(listOf(1), portal.invites.map { it.id })
        assertEquals(setOf(12), portal.pendingTeamRequestIds)
        assertEquals(listOf(12), portal.teams.map { it.id })
    }

    @Test
    fun teamMutationsAndLeaderQueriesUseTheirDeclaredOperations() = kotlinx.coroutines.runBlocking {
        val calls = mutableListOf<String>()
        val api = registrationApiStub { method, args ->
            calls += method
            when (method) {
                "checkTeamNameAvailability" -> Response.success(envelope(true))
                "createParticipantTeam" -> Response.success(envelope(
                    ParticipantRegistrationDto(30, 4, 12, "Robotics", "PENDING", true, emptyList()),
                ))
                "requestToJoinTeam", "acceptTeamRequest", "rejectTeamRequest", "cancelTeamRequest", "inviteParticipantToTeam" ->
                    Response.success(envelope(request(5, 30, 12, "PARTICIPANT")))
                "getTeamJoinRequests" -> Response.success(envelope(listOf(request(5, 30, 12, "PARTICIPANT"))))
                "getTeamCandidates" -> {
                    assertEquals("sam", args[1])
                    Response.success(envelope(listOf(TeamCandidateDto(30, 7, "Sam"))))
                }
                else -> error("Unexpected API call: $method")
            }
        }
        val repository = NetworkTeamRepository(api, NetworkRegistrationRepository(api))

        assertTrue(repository.checkTeamName(4, 30, "  Robots ").getOrThrow())
        assertEquals(TeamAssignment(30, 12), repository.createTeam(30, " Robotics ").getOrThrow())
        assertTrue(repository.requestToJoin(30, 12).isSuccess)
        assertTrue(repository.respondToInvite(5, true).isSuccess)
        assertEquals(5, repository.getTeamJoinRequests(12).getOrThrow().single().requestId)
        assertTrue(repository.respondToJoinRequest(5, false).isSuccess)
        assertTrue(repository.cancelTeamRequest(5).isSuccess)
        assertTrue(repository.inviteParticipant(12, 31).isSuccess)
        assertEquals(listOf(TeamInviteCandidate(30, 7, "Sam")), repository.searchInviteCandidates(4, "sam").getOrThrow())
        assertEquals(
            listOf("checkTeamNameAvailability", "createParticipantTeam", "requestToJoinTeam", "acceptTeamRequest", "getTeamJoinRequests", "rejectTeamRequest", "cancelTeamRequest", "inviteParticipantToTeam", "getTeamCandidates"),
            calls,
        )
    }

    @Test
    fun teamApiLevelFailureIsReturnedAsFailure() = kotlinx.coroutines.runBlocking {
        val api = registrationApiStub { method, _ ->
            when (method) {
                "getParticipantRegistration" -> Response.success(envelope(
                    ParticipantRegistrationDto(30, 4, null, null, "PENDING", true, emptyList()),
                ))
                "getMyTeamRequests" -> Response.success(ApiResponse<List<TeamRequestDto>>(false, null, "Requests unavailable"))
                else -> error("Unexpected API call: $method")
            }
        }

        val result = NetworkTeamRepository(api, NetworkRegistrationRepository(api)).getTeamPortal(4, 30)
        assertEquals("Requests unavailable", result.exceptionOrNull()?.message)
    }

    private fun request(id: Int, registrationId: Int, teamId: Int, initiator: String) = TeamRequestDto(
        requestId = id,
        teamId = teamId,
        teamName = "Team $teamId",
        registrationId = registrationId,
        participantId = 3,
        participantName = "Member",
        initiatedBy = initiator,
        status = "PENDING",
        createdAt = "now",
    )

    private fun <T> envelope(data: T) = ApiResponse(true, data, null)
}
