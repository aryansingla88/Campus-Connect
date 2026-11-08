package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.NetworkUserRegistrationRepository
import com.example.campusconnect.feature.registrations.data.UserRegistrationRepository
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.model.TeamInvite
import com.example.campusconnect.feature.registrations.model.TeamPortalData
import com.example.campusconnect.feature.registrations.model.TeamSummary
import com.example.campusconnect.feature.registrations.model.TeamAssignment
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.model.isMissing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed interface CreateState {
    object Loading : CreateState
    data class Failed(val message: String) : CreateState
    data class Ready(val draft: RegistrationDraft) : CreateState
}

sealed interface PortalState {
    object Loading : PortalState
    data class Failed(val message: String) : PortalState
    data class Loaded(val data: TeamPortalData) : PortalState
}

data class UserRegistrationUiState(
    val step: UserRegistrationStep = UserRegistrationStep.CREATING,
    val create: CreateState = CreateState.Loading,
    val answers: Map<Int, String> = emptyMap(),
    val showErrors: Boolean = false,
    val savingDetails: Boolean = false,
    val teamName: String = "",
    val checkingTeamName: Boolean = false,
    val teamNameAvailable: Boolean? = null,
    val creatingTeam: Boolean = false,
    val mode: ParticipationMode? = null,
    val portal: PortalState = PortalState.Loading,
    val query: String = "",
    val tagFilter: String? = null,
    val requestedTeamIds: Set<Int> = emptySet(),
    val pendingTeamIds: Set<Int> = emptySet(),
    val respondedInviteIds: Set<Int> = emptySet(),
    val pendingInviteIds: Set<Int> = emptySet(),
    val pendingJoinRequestIds: Set<Int> = emptySet(),
    val pendingInviteSendIds: Set<Int> = emptySet(),
    val transientError: String? = null,
)

enum class TeamNameCheck { AVAILABLE, UNAVAILABLE }

class UserRegistrationViewModel(
    private val repository: UserRegistrationRepository = NetworkUserRegistrationRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(UserRegistrationUiState())
    val state: StateFlow<UserRegistrationUiState> = _state.asStateFlow()

    private var eventId: Int? = null

    // ── Creating registration ────────────────────────────────────────────────

    /** Safe to call on every composition; only (re)starts for a new event or after a failure. */
    fun start(eventId: Int) {
        if (this.eventId == eventId && _state.value.create !is CreateState.Failed) return
        this.eventId = eventId
        createRegistration()
    }

    fun retryCreate() = createRegistration()

    private fun createRegistration() {
        val id = eventId ?: return
        _state.update { it.copy(step = UserRegistrationStep.CREATING, create = CreateState.Loading) }
        viewModelScope.launch {
            val result = repository.createRegistration(id)
            // Keep the transition loader visible long enough to read, even on a fast response.
            delay(500)
            result.fold(
                onSuccess = { draft ->
                    _state.update {
                        it.copy(
                            step = UserRegistrationStep.DETAILS,
                            create = CreateState.Ready(draft),
                            answers = draft.initialAnswers,
                            mode = if (draft.allowTeam) ParticipationMode.CREATE_TEAM else ParticipationMode.SOLO,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(create = CreateState.Failed(e.message ?: "Couldn't start your registration."))
                    }
                },
            )
        }
    }

    // ── Individual details ───────────────────────────────────────────────────

    fun onAnswerChange(questionId: Int, value: String) {
        _state.update { it.copy(answers = it.answers + (questionId to value)) }
    }

    fun submitDetails(onContinueSolo: () -> Unit = {}) {
        val current = _state.value
        val draft = (current.create as? CreateState.Ready)?.draft ?: return

        val missing = draft.questions.any { it.isMissing(current.answers[it.id]) }
        if (missing) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        if (_state.value.savingDetails) return
        _state.update { it.copy(savingDetails = true, showErrors = false, transientError = null) }
        viewModelScope.launch {
            repository.saveIndividualAnswers(draft.registrationId, current.answers).fold(
                onSuccess = {
                    _state.update { it.copy(savingDetails = false, step = if (draft.allowTeam) UserRegistrationStep.PARTICIPATION else it.step) }
                    if (!draft.allowTeam && draft.allowSolo) onContinueSolo()
                },
                onFailure = { error -> _state.update {
                    it.copy(savingDetails = false, transientError = error.message ?: "Could not save your answers.")
                } },
            )
        }
    }

    // ── Participation choice ─────────────────────────────────────────────────

    fun selectMode(mode: ParticipationMode) {
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        if (mode == ParticipationMode.SOLO && !draft.allowSolo) return
        if (mode != ParticipationMode.SOLO && !draft.allowTeam) return
        _state.update { it.copy(mode = mode) }
    }

    fun onTeamNameChange(value: String) = _state.update {
        it.copy(teamName = value, teamNameAvailable = null, transientError = null)
    }

    fun checkTeamName(onResult: (TeamNameCheck) -> Unit = {}) {
        val eventId = this.eventId ?: return
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        val name = _state.value.teamName.trim()
        if (name.isBlank() || name.length > 100) {
            _state.update { it.copy(teamNameAvailable = false, transientError = "Enter a team name between 1 and 100 characters.") }
            return
        }
        if (_state.value.checkingTeamName) return
        _state.update { it.copy(checkingTeamName = true, teamNameAvailable = null, transientError = null) }
        viewModelScope.launch {
            repository.checkTeamName(eventId, draft.registrationId, name).fold(
                onSuccess = { available ->
                    _state.update { it.copy(checkingTeamName = false, teamNameAvailable = available) }
                    onResult(if (available) TeamNameCheck.AVAILABLE else TeamNameCheck.UNAVAILABLE)
                },
                onFailure = { error -> _state.update {
                    it.copy(checkingTeamName = false, teamNameAvailable = null,
                        transientError = error.message ?: "Could not check team name.")
                } },
            )
        }
    }

    fun createTeam(onCreated: (TeamAssignment) -> Unit) {
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        val state = _state.value
        if (state.mode != ParticipationMode.CREATE_TEAM || state.teamNameAvailable != true || state.creatingTeam) return
        _state.update { it.copy(creatingTeam = true, transientError = null) }
        viewModelScope.launch {
            repository.createTeam(draft.registrationId, state.teamName.trim()).fold(
                onSuccess = { assignment ->
                    _state.update { it.copy(creatingTeam = false) }
                    onCreated(assignment)
                },
                onFailure = { error -> _state.update {
                    it.copy(creatingTeam = false, teamNameAvailable = null,
                        transientError = error.message ?: "Could not create the team.")
                } },
            )
        }
    }

    fun openTeamPortal() {
        _state.update { it.copy(step = UserRegistrationStep.TEAM_PORTAL) }
        loadTeams()
    }

    // ── Team portal ──────────────────────────────────────────────────────────

    fun loadTeams() {
        val id = eventId ?: return
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        _state.update { it.copy(portal = PortalState.Loading) }
        viewModelScope.launch {
            repository.getTeams(id, draft.registrationId).fold(
                onSuccess = { data -> _state.update { it.copy(
                    portal = PortalState.Loaded(data),
                    requestedTeamIds = data.pendingTeamRequestIds,
                ) } },
                onFailure = { e ->
                    _state.update { it.copy(portal = PortalState.Failed(e.message ?: "Couldn't load teams.")) }
                },
            )
        }
    }

    fun onQueryChange(query: String) = _state.update { it.copy(query = query) }

    fun onTagFilterChange(tag: String?) = _state.update { it.copy(tagFilter = tag) }

    fun requestToJoin(team: TeamSummary, onRequested: (TeamSummary) -> Unit = {}) {
        val id = eventId ?: return
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        val s = _state.value
        if (team.id in s.requestedTeamIds || team.id in s.pendingTeamIds || team.isFull) return

        _state.update { it.copy(pendingTeamIds = it.pendingTeamIds + team.id) }
        viewModelScope.launch {
            repository.requestToJoin(draft.registrationId, team.id).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            pendingTeamIds = it.pendingTeamIds - team.id,
                            requestedTeamIds = it.requestedTeamIds + team.id,
                        )
                    }
                    onRequested(team)
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingTeamIds = it.pendingTeamIds - team.id,
                            transientError = e.message ?: "Couldn't send your request.",
                        )
                    }
                },
            )
        }
    }

    fun respondToInvite(invite: TeamInvite, accept: Boolean, onAccepted: (TeamInvite) -> Unit = {}) {
        if (invite.id in _state.value.pendingInviteIds) return

        _state.update { it.copy(pendingInviteIds = it.pendingInviteIds + invite.id) }
        viewModelScope.launch {
            repository.respondToInvite(invite.id, accept).fold(
                onSuccess = {
                    _state.update {
                        it.copy(
                            pendingInviteIds = it.pendingInviteIds - invite.id,
                            respondedInviteIds = it.respondedInviteIds + invite.id,
                        )
                    }
                    if (accept) onAccepted(invite)
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingInviteIds = it.pendingInviteIds - invite.id,
                            transientError = e.message ?: "Couldn't update the invite.",
                        )
                    }
                },
            )
        }
    }

    fun respondToJoinRequest(requestId: Int, accept: Boolean) {
        if (requestId in _state.value.pendingJoinRequestIds) return
        _state.update { it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds + requestId) }
        viewModelScope.launch {
            repository.respondToJoinRequest(requestId, accept).fold(
                onSuccess = { loadTeams() },
                onFailure = { error -> _state.update {
                    it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds - requestId,
                        transientError = error.message ?: "Could not update join request.")
                } },
            )
        }
    }

    fun inviteParticipant(registrationId: Int) {
        val data = (_state.value.portal as? PortalState.Loaded)?.data ?: return
        val teamId = data.currentTeam?.id ?: return
        if (registrationId in _state.value.pendingInviteSendIds) return
        _state.update { it.copy(pendingInviteSendIds = it.pendingInviteSendIds + registrationId) }
        viewModelScope.launch {
            repository.inviteParticipant(teamId, registrationId).fold(
                onSuccess = { loadTeams() },
                onFailure = { error -> _state.update {
                    it.copy(pendingInviteSendIds = it.pendingInviteSendIds - registrationId,
                        transientError = error.message ?: "Could not invite participant.")
                } },
            )
        }
    }

    fun consumeError() = _state.update { it.copy(transientError = null) }

    // ── Navigation inside the flow ───────────────────────────────────────────

    /** Steps back inside the flow. Returns false when there is nowhere to go (the host should exit). */
    fun back(): Boolean = when (_state.value.step) {
        UserRegistrationStep.PARTICIPATION -> {
            _state.update { it.copy(step = UserRegistrationStep.DETAILS) }
            true
        }
        UserRegistrationStep.TEAM_PORTAL -> {
            _state.update { it.copy(step = UserRegistrationStep.PARTICIPATION) }
            true
        }
        else -> false
    }
}
