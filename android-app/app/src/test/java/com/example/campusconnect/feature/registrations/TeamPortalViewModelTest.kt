package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.domain.repository.TeamRepository
import com.example.campusconnect.feature.registrations.model.*
import com.example.campusconnect.feature.registrations.viewmodel.PortalState
import com.example.campusconnect.feature.registrations.viewmodel.TeamPortalViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TeamPortalViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setUp() { Dispatchers.setMain(dispatcher) }
    @After fun tearDown() { Dispatchers.resetMain() }

    @Test fun switchingSessionIgnoresOldPortalResult() = runTest(dispatcher) {
        val repo = FakeTeamRepository()
        val old = CompletableDeferred<Result<TeamPortalData>>()
        repo.results[1] = old
        val vm = TeamPortalViewModel(repo)
        vm.open(session(1, 11)); runCurrent()
        vm.open(session(2, 22)); advanceUntilIdle()
        old.complete(Result.success(TeamPortalData(listOf(team(1)), emptyList())))
        advanceUntilIdle()
        val current = vm.state.value
        assertEquals(2, current.session?.eventId)
        assertEquals(listOf(2), (current.portal as PortalState.Loaded).data.teams.map { it.id })
    }

    @Test fun latestSearchWinsAndJoinRequestStaysInPortal() = runTest(dispatcher) {
        val repo = FakeTeamRepository()
        val oldSearch = CompletableDeferred<Result<TeamPortalData>>()
        repo.searchResults["old"] = oldSearch
        repo.searchResults["new"] = CompletableDeferred(
            Result.success(TeamPortalData(listOf(TeamSummary(2, "Team 2", "Leader", 1, 4)), emptyList())),
        )
        val vm = TeamPortalViewModel(repo)
        vm.open(session(1, 11)); advanceUntilIdle()
        vm.onQueryChange("old"); runCurrent()
        advanceTimeBy(250); advanceUntilIdle()
        vm.onQueryChange("new"); runCurrent()
        advanceTimeBy(250); advanceUntilIdle()
        oldSearch.complete(Result.success(TeamPortalData(listOf(team(99)), emptyList())))
        advanceUntilIdle()
        assertEquals(listOf(2), ((vm.state.value.portal as PortalState.Loaded).data.teams).map { it.id })
        vm.requestToJoin(team(2)); advanceUntilIdle()
        assertTrue(2 in vm.state.value.requestedTeamIds)
        assertTrue(vm.state.value.portal is PortalState.Loaded)
        assertEquals(11, repo.joinRegistrationId)
    }

    @Test fun duplicateCreateAndRepeatedOpenDoNotRepeatOperations() = runTest(dispatcher) {
        val repo = FakeTeamRepository()
        val vm = TeamPortalViewModel(repo)
        val session = session(1, 11, ParticipationMode.CREATE_TEAM)
        vm.open(session); advanceUntilIdle()
        vm.onTeamNameChange("Team")
        vm.checkTeamName(); advanceUntilIdle()
        vm.createTeam {}; vm.createTeam {}; advanceUntilIdle()
        assertEquals(1, repo.teamCreates)
        assertEquals(11, repo.createdForRegistrationId)
        vm.open(session); advanceUntilIdle()
        assertEquals(2, repo.portalLoads)
    }

    private fun session(event: Int, registration: Int, mode: ParticipationMode = ParticipationMode.JOIN_TEAM) =
        RegistrationSession(event, registration, mode)
    private fun team(id: Int) = TeamSummary(id, "Team $id", "Leader", 1, 4)

    private class FakeTeamRepository : TeamRepository {
        val results = mutableMapOf<Int, CompletableDeferred<Result<TeamPortalData>>>()
        val searchResults = mutableMapOf<String, CompletableDeferred<Result<TeamPortalData>>>()
        var portalLoads = 0
        var teamCreates = 0
        var createdForRegistrationId: Int? = null
        var joinRegistrationId: Int? = null
        override suspend fun getTeamPortal(eventId: Int, registrationId: Int, search: String?): Result<TeamPortalData> {
            portalLoads++
            return searchResults[search]?.await() ?: results[eventId]?.await()
                ?: Result.success(TeamPortalData(
                    listOf(TeamSummary(eventId, "Team $eventId", "Leader", 1, 4)), emptyList(),
                ))
        }
        override suspend fun checkTeamName(eventId: Int, registrationId: Int, teamName: String) = Result.success(true)
        override suspend fun createTeam(registrationId: Int, teamName: String): Result<TeamAssignment> {
            teamCreates++; createdForRegistrationId = registrationId
            return Result.success(TeamAssignment(registrationId, 5))
        }
        override suspend fun requestToJoin(registrationId: Int, teamId: Int): Result<Unit> { joinRegistrationId = registrationId; return Result.success(Unit) }
        override suspend fun respondToInvite(requestId: Int, accept: Boolean) = Result.success(Unit)
        override suspend fun getTeamJoinRequests(teamId: Int) = Result.success(emptyList<TeamJoinRequest>())
        override suspend fun respondToJoinRequest(requestId: Int, accept: Boolean) = Result.success(Unit)
        override suspend fun cancelTeamRequest(requestId: Int) = Result.success(Unit)
        override suspend fun inviteParticipant(teamId: Int, registrationId: Int) = Result.success(Unit)
        override suspend fun searchInviteCandidates(eventId: Int, query: String) = Result.success(emptyList<TeamInviteCandidate>())
    }
}
