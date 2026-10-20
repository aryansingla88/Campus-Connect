package com.example.campusconnect.feature.registrations.userflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.userflow.data.ParticipantRegistrationRepositories
import com.example.campusconnect.feature.registrations.userflow.data.UserRegistrationRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenConfig
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenKind
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvitation
import com.example.campusconnect.feature.registrations.userflow.model.TeamJoinRequest
import com.example.campusconnect.feature.registrations.userflow.model.TeamPortalData
import com.example.campusconnect.feature.registrations.userflow.model.TeamSummary
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.userflow.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.userflow.model.updateAnswer
import com.example.campusconnect.feature.registrations.userflow.model.validateAnswers
import com.example.campusconnect.feature.registrations.userflow.model.teamNameLengthError
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
    val questionScreenConfig: RegistrationQuestionScreenConfig = RegistrationQuestionScreenConfig(
        kind = RegistrationQuestionScreenKind.INDIVIDUAL,
        subtitle = "Please answer the following questions.",
        progressStep = 1,
        actionLabel = "Next",
    ),
    val answers: Map<Int, String> = emptyMap(),
    val validationErrors: Map<Int, String> = emptyMap(),
    val mode: ParticipationMode? = null,
    val teamNameError: String? = null,
    val teamNameResultMessage: String? = null,
    val teamNameResultAvailable: Boolean? = null,
    val teamNamePromptOpen: Boolean = false,
    val isCheckingTeamName: Boolean = false,
    val teamNameConfirmed: Boolean = false,
    val portal: PortalState = PortalState.Loading,
    val query: String = "",
    val tagFilter: String? = null,
    val requestedTeamIds: Set<Int> = emptySet(),
    val pendingTeamIds: Set<Int> = emptySet(),
    val respondedInviteIds: Set<Int> = emptySet(),
    val pendingInviteIds: Set<Int> = emptySet(),
    val pendingJoinRequestIds: Set<Int> = emptySet(),
    val pendingSentInviteIds: Set<Int> = emptySet(),
    val isSendingInvite: Boolean = false,
    val transientError: String? = null,
)

enum class DetailsResult { INVALID, NEXT_STEP, DONE_SOLO }

class UserRegistrationViewModel(
    private val repository: UserRegistrationRepository = ParticipantRegistrationRepositories.user,
) : ViewModel() {

    private val _state = MutableStateFlow(UserRegistrationUiState())
    val state: StateFlow<UserRegistrationUiState> = _state.asStateFlow()

    private var eventId: Int? = null
    private var participantId: Int? = null
    private var teamNameCheckVersion = 0L

    // ── Creating registration ────────────────────────────────────────────────

    /** Safe to call on every composition; only (re)starts for a new event or after a failure. */
    fun start(eventId: Int, participantId: Int? = null) {
        if (this.eventId == eventId && this.participantId == participantId && _state.value.create !is CreateState.Failed) return
        this.eventId = eventId
        this.participantId = participantId
        repository.selectParticipant(participantId)
        createRegistration()
    }

    fun retryCreate() = createRegistration()

    fun startPortal(eventId: Int, participantId: Int?) {
        this.eventId = eventId
        this.participantId = participantId
        repository.selectParticipant(participantId)
        loadTeams()
    }

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
                            validationErrors = emptyMap(),
                            teamNameError = null,
                            isCheckingTeamName = false,
                            teamNameConfirmed = draft.teamId != null && draft.teamLeader,
                            mode = when (participantId) {
                                1 -> ParticipationMode.JOIN_TEAM
                                2 -> ParticipationMode.CREATE_TEAM
                                3 -> ParticipationMode.SOLO
                                else -> if (draft.teamId != null && !draft.teamLeader) ParticipationMode.JOIN_TEAM
                                    else if (draft.teamId != null) ParticipationMode.CREATE_TEAM
                                    else if (draft.allowTeam) ParticipationMode.CREATE_TEAM else ParticipationMode.SOLO
                            },
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
        _state.update { current ->
            val fields = (current.create as? CreateState.Ready)?.draft?.questions.orEmpty()
            val answerState = fields.updateAnswer(
                current.answers,
                current.validationErrors,
                questionId,
                value,
            )
            current.copy(
                answers = answerState.answers,
                validationErrors = answerState.validationErrors,
            )
        }
    }

    fun submitDetails(): DetailsResult {
        val current = _state.value
        val draft = (current.create as? CreateState.Ready)?.draft ?: return DetailsResult.INVALID

        val validationErrors = draft.questions.validateAnswers(current.answers)
        if (validationErrors.isNotEmpty()) {
            _state.update { it.copy(validationErrors = validationErrors) }
            return DetailsResult.INVALID
        }
        _state.update { it.copy(step = UserRegistrationStep.PARTICIPATION, validationErrors = emptyMap()) }
        return DetailsResult.NEXT_STEP
    }

    // ── Participation choice ─────────────────────────────────────────────────

    fun selectMode(mode: ParticipationMode) {
        val previousMode = _state.value.mode
        if (previousMode != mode) teamNameCheckVersion++
        _state.update {
            it.copy(
                mode = mode,
                teamNamePromptOpen = mode == ParticipationMode.CREATE_TEAM && !it.teamNameConfirmed,
                isCheckingTeamName = if (previousMode != mode) false else it.isCheckingTeamName,
                teamNameError = null,
                teamNameResultMessage = null,
                teamNameResultAvailable = null,
                teamNameConfirmed = if (previousMode == mode && mode == ParticipationMode.CREATE_TEAM) it.teamNameConfirmed else false,
            )
        }
    }

    fun onTeamNameChanged() = _state.update {
        teamNameCheckVersion++
        it.copy(
            teamNameError = null,
            isCheckingTeamName = false,
            teamNameConfirmed = false,
            teamNameResultMessage = null,
            teamNameResultAvailable = null,
        )
    }

    fun cancelTeamNamePrompt() {
        teamNameCheckVersion++
        _state.update {
            it.copy(
                mode = null,
                teamNamePromptOpen = false,
                teamNameError = null,
                isCheckingTeamName = false,
            )
        }
    }

    fun requestTeamName() {
        _state.update { it.copy(teamNamePromptOpen = true) }
    }

    /** Checks availability only; team creation remains part of the final submission request. */
    fun validateTeamName(name: String) {
        val normalized = name.trim()
        val error = teamNameLengthError(normalized)
        if (error != null) {
            _state.update { it.copy(
                teamNameError = error,
                teamNameConfirmed = false,
                teamNamePromptOpen = false,
                teamNameResultMessage = error,
                teamNameResultAvailable = false,
            ) }
            return
        }
        val id = eventId ?: return
        val checkVersion = ++teamNameCheckVersion
        _state.update { it.copy(
            isCheckingTeamName = true,
            teamNameError = null,
            teamNameConfirmed = false,
            teamNamePromptOpen = false,
            teamNameResultMessage = null,
            teamNameResultAvailable = null,
        ) }
        viewModelScope.launch {
            repository.isTeamNameAvailable(id, normalized).fold(
                onSuccess = availableResult@ { available ->
                    if (checkVersion != teamNameCheckVersion) return@availableResult
                    val message = if (available) "That team name is available." else "That team name is already in use."
                    _state.update { it.copy(
                        isCheckingTeamName = false,
                        teamNameConfirmed = available,
                        teamNameError = if (available) null else message,
                        teamNameResultMessage = message,
                        teamNameResultAvailable = available,
                    ) }
                },
                onFailure = availabilityFailure@ { errorValue ->
                    if (checkVersion != teamNameCheckVersion) return@availabilityFailure
                    val message = errorValue.message ?: "Could not check team name availability."
                    _state.update {
                        it.copy(
                            isCheckingTeamName = false,
                            teamNameConfirmed = false,
                            teamNameError = message,
                            teamNameResultMessage = message,
                            teamNameResultAvailable = false,
                        )
                    }
                },
            )
        }
    }

    fun dismissTeamNameResult() = _state.update {
        it.copy(
            teamNameResultMessage = null,
            teamNamePromptOpen = it.teamNameResultAvailable != true && it.mode == ParticipationMode.CREATE_TEAM,
        )
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
                onSuccess = { data ->
                    _state.update { it.copy(portal = PortalState.Loaded(data), requestedTeamIds = data.pendingTeamIds) }
                },
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
        val hasPendingInvite = (s.portal as? PortalState.Loaded)?.data?.invites?.isNotEmpty() == true
        if (team.id in s.requestedTeamIds || team.id in s.pendingTeamIds
            || s.requestedTeamIds.isNotEmpty() || hasPendingInvite || team.isFull) return

        // Optimistic: only one team request or invitation may be pending for a registration.
        _state.update { it.copy(pendingTeamIds = it.pendingTeamIds + team.id, requestedTeamIds = it.requestedTeamIds + team.id) }
        viewModelScope.launch {
            repository.requestToJoin(id, team.id).fold(
                onSuccess = {
                    _state.update { it.copy(pendingTeamIds = it.pendingTeamIds - team.id) }
                    onRequested(team)
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingTeamIds = it.pendingTeamIds - team.id,
                            requestedTeamIds = it.requestedTeamIds - team.id,
                            transientError = e.message ?: "Couldn't send your request.",
                        )
                    }
                },
            )
        }
    }

    /** Withdraws the join request for [teamId] immediately (no confirmation); restored if the call fails. */
    fun cancelJoinRequest(teamId: Int) {
        val id = eventId ?: return
        if (teamId in _state.value.pendingTeamIds) return
        _state.update { it.copy(pendingTeamIds = it.pendingTeamIds + teamId, requestedTeamIds = it.requestedTeamIds - teamId) }
        viewModelScope.launch {
            repository.cancelJoinRequest(id, teamId).fold(
                onSuccess = { _state.update { it.copy(pendingTeamIds = it.pendingTeamIds - teamId) } },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingTeamIds = it.pendingTeamIds - teamId,
                            requestedTeamIds = it.requestedTeamIds + teamId,
                            transientError = e.message ?: "Couldn't cancel the request.",
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
                    loadTeams()
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

    fun respondToJoinRequest(request: TeamJoinRequest, accept: Boolean) {
        val id = eventId ?: return
        if (request.id in _state.value.pendingJoinRequestIds) return
        _state.update { it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds + request.id) }
        viewModelScope.launch {
            repository.respondToJoinRequest(id, request.id, accept).fold(
                onSuccess = {
                    _state.update { it.copy(pendingJoinRequestIds = it.pendingJoinRequestIds - request.id) }
                    loadTeams()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingJoinRequestIds = it.pendingJoinRequestIds - request.id,
                            transientError = e.message ?: "Couldn't update the join request.",
                        )
                    }
                },
            )
        }
    }

    fun inviteMember(registrationId: Int) {
        val id = eventId ?: return
        _state.update { it.copy(isSendingInvite = true) }
        viewModelScope.launch {
            repository.inviteMember(id, registrationId).fold(
                onSuccess = {
                    _state.update { it.copy(isSendingInvite = false) }
                    loadTeams()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            isSendingInvite = false,
                            transientError = e.message ?: "Couldn't send the invitation.",
                        )
                    }
                },
            )
        }
    }

    fun cancelInvitation(invitation: TeamInvitation) {
        val id = eventId ?: return
        if (invitation.id in _state.value.pendingSentInviteIds) return
        _state.update { it.copy(pendingSentInviteIds = it.pendingSentInviteIds + invitation.id) }
        viewModelScope.launch {
            repository.cancelInvitation(id, invitation.id).fold(
                onSuccess = {
                    _state.update { it.copy(pendingSentInviteIds = it.pendingSentInviteIds - invitation.id) }
                    loadTeams()
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(
                            pendingSentInviteIds = it.pendingSentInviteIds - invitation.id,
                            transientError = e.message ?: "Couldn't cancel the invitation.",
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
