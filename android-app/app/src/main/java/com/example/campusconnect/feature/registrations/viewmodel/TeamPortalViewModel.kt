package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.domain.repository.TeamRepository
import com.example.campusconnect.feature.registrations.model.RegistrationSession
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.TeamAssignment
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.teamNameLengthError
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.model.TeamInviteCandidate
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PortalState {
    data object Loading : PortalState
    data class Failed(val message: String) : PortalState
    data class Loaded(val data: TeamPortalData) : PortalState
}

enum class TeamNameCheck { AVAILABLE, UNAVAILABLE }

data class TeamPortalUiState(
    val session: RegistrationSession? = null,
    val portal: PortalState = PortalState.Loading,
    val query: String = "",
    val tagFilter: String? = null,
    val teamName: String = "",
    val checkingTeamName: Boolean = false,
    val teamNameAvailable: Boolean? = null,
    val creatingTeam: Boolean = false,
    val requestedTeamIds: Set<Int> = emptySet(),
    val pendingTeamIds: Set<Int> = emptySet(),
    val respondedInviteIds: Set<Int> = emptySet(),
    val pendingInviteIds: Set<Int> = emptySet(),
    val pendingJoinRequestIds: Set<Int> = emptySet(),
    val pendingInviteSendIds: Set<Int> = emptySet(),
    val transientError: String? = null,
)

/** Team discovery and membership operations, keyed to an explicit participant session. */
class TeamPortalViewModel(private val repository: TeamRepository) : ViewModel() {
    private val _state = MutableStateFlow(TeamPortalUiState())
    val state: StateFlow<TeamPortalUiState> = _state.asStateFlow()
    private var portalGeneration = 0L
    private var searchGeneration = 0L
    private var actionGeneration = 0L
    private var portalJob: Job? = null
    private var searchJob: Job? = null
    private val pendingActions = mutableSetOf<String>()

    /** Synchronizes the participant-owned session without fetching or resetting portal/name state. */
    fun syncSession(session: RegistrationSession) {
        val current = _state.value.session
        if (current == session) return

        // A changed participant/session invalidates work started for its previous identity.
        if (current?.eventId != session.eventId || current.registrationId != session.registrationId) {
            portalGeneration++
            portalJob?.cancel()
            searchJob?.cancel()
            searchGeneration++
            actionGeneration++
            pendingActions.clear()
            _state.update {
                it.copy(
                    session = session,
                    portal = PortalState.Loading,
                    query = "",
                    tagFilter = null,
                    teamName = "",
                    checkingTeamName = false,
                    teamNameAvailable = null,
                    creatingTeam = false,
                    requestedTeamIds = emptySet(),
                    pendingTeamIds = emptySet(),
                    respondedInviteIds = emptySet(),
                    pendingInviteIds = emptySet(),
                    pendingJoinRequestIds = emptySet(),
                    pendingInviteSendIds = emptySet(),
                    transientError = null,
                )
            }
            return
        }

        // Mode/progress changes for the same registration must not erase user-entered name state.
        _state.update { it.copy(session = session) }
    }

    fun open(session: RegistrationSession) {
        val currentSession = _state.value.session
        val sameIdentity = currentSession?.eventId == session.eventId &&
            currentSession.registrationId == session.registrationId
        val sameSession = sameIdentity && currentSession?.mode == session.mode
        if (sameSession && _state.value.portal !is PortalState.Failed && _state.value.portal !is PortalState.Loading) {
            syncSession(session)
            refresh()
            return
        }
        syncSession(session)
        portalGeneration++
        portalJob?.cancel()
        searchJob?.cancel()
        searchGeneration++
        load(session, "", portalGeneration)
    }

    fun refresh() {
        val session = _state.value.session ?: return
        load(session, _state.value.query, portalGeneration)
    }

    private fun load(session: RegistrationSession, search: String, generation: Long) {
        _state.update { it.copy(portal = PortalState.Loading) }
        portalJob = viewModelScope.launch {
            repository.getTeamPortal(session.eventId, session.registrationId, search.takeIf { it.isNotBlank() }).fold(
                onSuccess = { data -> if (isCurrent(session, generation)) _state.update {
                    it.copy(portal = PortalState.Loaded(data), requestedTeamIds = data.pendingTeamRequestIds)
                } },
                onFailure = { error -> if (isCurrent(session, generation)) _state.update {
                    it.copy(portal = PortalState.Failed(error.message ?: "Couldn't load teams."))
                } },
            )
        }
    }

    fun onQueryChange(query: String) {
        _state.update { it.copy(query = query) }
        val session = _state.value.session ?: return
        val searchToken = ++searchGeneration
        val portalToken = portalGeneration
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(250)
            if (!isCurrent(session, portalToken) || searchToken != searchGeneration) return@launch
            _state.update { it.copy(portal = PortalState.Loading) }
            repository.getTeamPortal(session.eventId, session.registrationId, query.takeIf { it.isNotBlank() }).fold(
                onSuccess = { data -> if (isCurrent(session, portalToken) && searchToken == searchGeneration) _state.update {
                    it.copy(portal = PortalState.Loaded(data), requestedTeamIds = data.pendingTeamRequestIds)
                } },
                onFailure = { error -> if (isCurrent(session, portalToken) && searchToken == searchGeneration) _state.update {
                    it.copy(portal = PortalState.Failed(error.message ?: "Couldn't load teams."))
                } },
            )
        }
    }

    fun onTagFilterChange(tag: String?) = _state.update { it.copy(tagFilter = tag) }
    fun onTeamNameChange(value: String) {
        actionGeneration++
        _state.update { it.copy(teamName = value, checkingTeamName = false, teamNameAvailable = null, transientError = null) }
    }

    fun checkTeamName(onResult: (TeamNameCheck) -> Unit = {}) {
        val session = _state.value.session ?: return
        val name = _state.value.teamName.trim()
        val nameError = teamNameLengthError(name)
        if (nameError != null) {
            _state.update { it.copy(teamNameAvailable = false, transientError = nameError) }
            return
        }
        if (_state.value.checkingTeamName) return
        val token = ++actionGeneration
        val requestGeneration = portalGeneration
        _state.update { it.copy(checkingTeamName = true, teamNameAvailable = null, transientError = null) }
        viewModelScope.launch {
            repository.checkTeamName(session.eventId, session.registrationId, name).fold(
                onSuccess = { available -> if (isCurrent(session, requestGeneration) && token == actionGeneration && _state.value.teamName.trim() == name) {
                    _state.update { it.copy(checkingTeamName = false, teamNameAvailable = available) }
                    onResult(if (available) TeamNameCheck.AVAILABLE else TeamNameCheck.UNAVAILABLE)
                } },
                onFailure = { error -> if (isCurrent(session, requestGeneration) && token == actionGeneration && _state.value.teamName.trim() == name) _state.update {
                    it.copy(checkingTeamName = false, transientError = error.message ?: "Could not check team name.")
                } },
            )
        }
    }

    fun createTeam(onCreated: (TeamAssignment) -> Unit) {
        val session = _state.value.session ?: return
        val state = _state.value
        val actionKey = "create:${session.eventId}:${session.registrationId}"
        if (session.mode != ParticipationMode.CREATE_TEAM || state.teamNameAvailable != true || state.creatingTeam || !pendingActions.add(actionKey)) return
        val token = ++actionGeneration
        val requestGeneration = portalGeneration
        val teamName = state.teamName.trim()
        _state.update { it.copy(creatingTeam = true, transientError = null) }
        viewModelScope.launch {
            repository.createTeam(session.registrationId, teamName).fold(
                onSuccess = { assignment ->
                    pendingActions.remove(actionKey)
                    if (isCurrent(session, requestGeneration) && token == actionGeneration && _state.value.teamName.trim() == teamName) {
                        _state.update { it.copy(creatingTeam = false) }
                        onCreated(assignment)
                    }
                },
                onFailure = { error ->
                    repository.getTeamPortal(session.eventId, session.registrationId).fold(
                        onSuccess = { portal ->
                            val recoveredTeam = portal.currentTeam?.takeIf {
                                portal.currentUserIsLeader && it.name.equals(teamName, ignoreCase = true)
                            }
                            pendingActions.remove(actionKey)
                            if (isCurrent(session, requestGeneration) && token == actionGeneration && _state.value.teamName.trim() == teamName) {
                                if (recoveredTeam != null) {
                                    _state.update {
                                        it.copy(
                                            portal = PortalState.Loaded(portal),
                                            creatingTeam = false,
                                            teamNameAvailable = true,
                                            transientError = null,
                                        )
                                    }
                                    onCreated(TeamAssignment(session.registrationId, recoveredTeam.id))
                                } else {
                                    _state.update {
                                        it.copy(
                                            creatingTeam = false,
                                            teamNameAvailable = null,
                                            transientError = error.message ?: "Could not create the team.",
                                        )
                                    }
                                }
                            }
                        },
                        onFailure = {
                            pendingActions.remove(actionKey)
                            if (isCurrent(session, requestGeneration) && token == actionGeneration && _state.value.teamName.trim() == teamName) {
                                _state.update {
                                    it.copy(
                                        creatingTeam = false,
                                        teamNameAvailable = null,
                                        transientError = error.message ?: "Could not create the team.",
                                    )
                                }
                            }
                        },
                    )
                },
            )
        }
    }

    fun requestToJoin(team: TeamSummary, onRequested: (TeamSummary) -> Unit = {}) {
        val session = _state.value.session ?: return
        val key = "join:${team.id}"
        if (team.id in _state.value.requestedTeamIds || team.id in _state.value.pendingTeamIds || team.isFull || !pendingActions.add(key)) return
        val generation = portalGeneration
        _state.update { it.copy(pendingTeamIds = it.pendingTeamIds + team.id) }
        viewModelScope.launch {
            repository.requestToJoin(session.registrationId, team.id).fold(
                onSuccess = { pendingActions.remove(key); if (isCurrent(session, generation)) {
                    _state.update { it.copy(pendingTeamIds = it.pendingTeamIds - team.id, requestedTeamIds = it.requestedTeamIds + team.id) }
                    onRequested(team)
                } },
                onFailure = { error -> pendingActions.remove(key); if (isCurrent(session, generation)) _state.update {
                    it.copy(pendingTeamIds = it.pendingTeamIds - team.id, transientError = error.message ?: "Couldn't send your request.")
                } },
            )
        }
    }

    fun respondToInvite(invite: TeamInvite, accept: Boolean, onAccepted: (TeamInvite) -> Unit = {}) = action("invite:${invite.id}",
        { repository.respondToInvite(invite.id, accept) },
        onSuccess = { state -> state.copy(pendingInviteIds = state.pendingInviteIds - invite.id, respondedInviteIds = state.respondedInviteIds + invite.id) },
        onAccepted = { if (accept) onAccepted(invite) },
        onError = "Couldn't update the invite.",
        addPending = { it.copy(pendingInviteIds = it.pendingInviteIds + invite.id) },
    )

    fun respondToJoinRequest(requestId: Int, accept: Boolean) = action("request:$requestId",
        { repository.respondToJoinRequest(requestId, accept) },
        onSuccess = { it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds - requestId) },
        onError = "Could not update join request.",
        addPending = { it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds + requestId) },
        refreshAfter = true,
    )

    fun inviteParticipant(registrationId: Int) {
        val session = _state.value.session ?: return
        val teamId = (_state.value.portal as? PortalState.Loaded)?.data?.currentTeam?.id ?: return
        action("send-invite:$registrationId",
            { repository.inviteParticipant(teamId, registrationId) },
            onSuccess = { it.copy(pendingInviteSendIds = it.pendingInviteSendIds - registrationId) },
            onError = "Could not invite participant.",
            addPending = { it.copy(pendingInviteSendIds = it.pendingInviteSendIds + registrationId) },
            refreshAfter = true,
            sessionOverride = session,
        )
    }

    private fun action(
        key: String, operation: suspend () -> Result<Unit>,
        onSuccess: (TeamPortalUiState) -> TeamPortalUiState,
        onAccepted: () -> Unit = {}, onError: String, addPending: (TeamPortalUiState) -> TeamPortalUiState,
        refreshAfter: Boolean = false, sessionOverride: RegistrationSession? = null,
    ) {
        val session = sessionOverride ?: _state.value.session ?: return
        if (!pendingActions.add(key)) return
        _state.update(addPending)
        val generation = portalGeneration
        viewModelScope.launch {
            operation().fold(
                onSuccess = { pendingActions.remove(key); if (isCurrent(session, generation)) {
                    _state.update(onSuccess)
                    if (refreshAfter) refresh()
                    onAccepted()
                } },
                onFailure = { error -> pendingActions.remove(key); if (isCurrent(session, generation)) _state.update {
                    it.copy(transientError = error.message ?: onError)
                } },
            )
        }
    }

    fun consumeError() = _state.update { it.copy(transientError = null) }
    private fun isCurrent(session: RegistrationSession, generation: Long = portalGeneration) = _state.value.session == session && generation == portalGeneration
}
