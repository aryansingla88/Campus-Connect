package com.example.campusconnect.feature.registrations.userflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.userflow.data.FakeUserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.data.UserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.model.isMissing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
    val mode: ParticipationMode? = null,
    val portal: PortalState = PortalState.Loading,
    val query: String = "",
    val tagFilter: String? = null,
    val requestedTeamIds: Set<Int> = emptySet(),
    val pendingTeamIds: Set<Int> = emptySet(),
    val respondedInviteIds: Set<Int> = emptySet(),
    val pendingInviteIds: Set<Int> = emptySet(),
    val transientError: String? = null,
)

enum class DetailsResult { INVALID, NEXT_STEP, DONE_SOLO }

class UserRegistrationViewModel(
    private val repository: UserRegistrationRepository = FakeUserRegistrationRepository(),
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
            repository.createRegistration(id).fold(
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

    fun submitDetails(): DetailsResult {
        val current = _state.value
        val draft = (current.create as? CreateState.Ready)?.draft ?: return DetailsResult.INVALID

        val missing = draft.questions.any { it.isMissing(current.answers[it.id]) }
        if (missing) {
            _state.update { it.copy(showErrors = true) }
            return DetailsResult.INVALID
        }
        return if (draft.allowTeam) {
            _state.update { it.copy(step = UserRegistrationStep.PARTICIPATION, showErrors = false) }
            DetailsResult.NEXT_STEP
        } else {
            DetailsResult.DONE_SOLO
        }
    }

    // ── Participation choice ─────────────────────────────────────────────────

    fun selectMode(mode: ParticipationMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun openTeamPortal() {
        _state.update { it.copy(step = UserRegistrationStep.TEAM_PORTAL) }
        loadTeams()
    }

    // ── Team portal ──────────────────────────────────────────────────────────

    fun loadTeams() {
        val id = eventId ?: return
        _state.update { it.copy(portal = PortalState.Loading) }
        viewModelScope.launch {
            repository.getTeams(id).fold(
                onSuccess = { data -> _state.update { it.copy(portal = PortalState.Loaded(data)) } },
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
        val s = _state.value
        if (team.id in s.requestedTeamIds || team.id in s.pendingTeamIds || team.isFull) return

        _state.update { it.copy(pendingTeamIds = it.pendingTeamIds + team.id) }
        viewModelScope.launch {
            repository.requestToJoin(id, team.id).fold(
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
        val id = eventId ?: return
        if (invite.id in _state.value.pendingInviteIds) return

        _state.update { it.copy(pendingInviteIds = it.pendingInviteIds + invite.id) }
        viewModelScope.launch {
            repository.respondToInvite(id, invite.id, accept).fold(
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
