package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.RegistrationDraft
import com.example.campusconnect.feature.registrations.model.RegistrationSession
import com.example.campusconnect.feature.registrations.model.UserRegistrationStep
import com.example.campusconnect.feature.registrations.model.isMissing
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CreateState {
    data object Loading : CreateState
    data class Failed(val message: String) : CreateState
    data class Ready(val draft: RegistrationDraft) : CreateState
}

data class ParticipantRegistrationState(
    val eventId: Int? = null,
    val step: UserRegistrationStep = UserRegistrationStep.CREATING,
    val create: CreateState = CreateState.Loading,
    val answers: Map<Int, String> = emptyMap(),
    val showErrors: Boolean = false,
    val savingDetails: Boolean = false,
    val mode: ParticipationMode? = null,
    val session: RegistrationSession? = null,
    val transientError: String? = null,
)

/** Owns registration initialization, individual answers, mode and the authoritative session. */
class ParticipantRegistrationViewModel(
    private val repository: RegistrationRepository,
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(),
) : ViewModel() {
    private val _state = MutableStateFlow(ParticipantRegistrationState())
    val state: StateFlow<ParticipantRegistrationState> = _state.asStateFlow()

    private var initializationGeneration = 0L
    private var initializationJob: Job? = null
    private var answerSaveGeneration = 0L
    private var retryRequested = false

    fun start(eventId: Int) {
        if (_state.value.eventId == eventId && _state.value.create !is CreateState.Failed) return
        if (_state.value.eventId == eventId && _state.value.create is CreateState.Failed && !retryRequested) return
        retryRequested = false
        initializationGeneration += 1
        val generation = initializationGeneration
        initializationJob?.cancel()
        savedStateHandle[KEY_EVENT_ID] = eventId
        _state.value = ParticipantRegistrationState(eventId = eventId)
        initializationJob = viewModelScope.launch {
            val result = repository.createRegistration(eventId)
            delay(500)
            if (generation != initializationGeneration || _state.value.eventId != eventId) return@launch
            result.fold(
                onSuccess = { draft ->
                    val sameRestoredRegistration = savedStateHandle.get<Int>(KEY_EVENT_ID) == eventId &&
                        savedStateHandle.get<Int>(KEY_REGISTRATION_ID) == draft.registrationId
                    val storedProgress = (if (sameRestoredRegistration) savedStateHandle.get<String>(KEY_PROGRESS) else null)
                        ?.let { runCatching { UserRegistrationStep.valueOf(it) }.getOrNull() }
                        ?: UserRegistrationStep.DETAILS
                    val restoredProgress = storedProgress.takeIf {
                        it != UserRegistrationStep.CREATING && (it != UserRegistrationStep.SUBMISSION || sameRestoredRegistration)
                    } ?: UserRegistrationStep.DETAILS
                    val restoredMode = (if (sameRestoredRegistration) savedStateHandle.get<String>(KEY_MODE) else null)
                        ?.let { runCatching { ParticipationMode.valueOf(it) }.getOrNull() }
                        ?.takeIf { (it == ParticipationMode.SOLO && draft.allowSolo) || (it != ParticipationMode.SOLO && draft.allowTeam) }
                        ?: if (draft.allowTeam) ParticipationMode.CREATE_TEAM else ParticipationMode.SOLO
                    val teamId = if (sameRestoredRegistration) savedStateHandle.get<Int>(KEY_TEAM_ID) else null
                    val modeForDraft = restoredMode.takeIf { it == ParticipationMode.SOLO || teamId != null }
                        ?: ParticipationMode.CREATE_TEAM
                    val step = if (restoredProgress == UserRegistrationStep.SUBMISSION) restoredProgress
                    else if (restoredProgress == UserRegistrationStep.TEAM_PORTAL && draft.allowTeam) restoredProgress
                    else if (draft.allowTeam && restoredProgress == UserRegistrationStep.PARTICIPATION) restoredProgress
                    else UserRegistrationStep.DETAILS
                    val session = RegistrationSession(eventId, draft.registrationId, modeForDraft, teamId, step)
                    savedStateHandle[KEY_REGISTRATION_ID] = draft.registrationId
                    savedStateHandle[KEY_MODE] = modeForDraft.name
                    savedStateHandle[KEY_PROGRESS] = step.name
                    _state.value = ParticipantRegistrationState(
                        eventId = eventId,
                        step = step,
                        create = CreateState.Ready(draft),
                        answers = draft.initialAnswers,
                        mode = modeForDraft,
                        session = session,
                    )
                },
                onFailure = { error -> _state.update { it.copy(create = CreateState.Failed(error.message ?: "Couldn't start your registration.")) } },
            )
        }
    }

    fun retryCreate() {
        _state.value.eventId?.let { id ->
            retryRequested = true
            start(id)
        }
    }

    fun openTeamPortalAfterSubmission() {
        val session = _state.value.session ?: return
        val teamStep = session.copy(progress = UserRegistrationStep.TEAM_PORTAL)
        savedStateHandle[KEY_PROGRESS] = UserRegistrationStep.TEAM_PORTAL.name
        _state.update { it.copy(session = teamStep, step = UserRegistrationStep.TEAM_PORTAL) }
    }

    fun resumeSubmission() {
        val session = _state.value.session ?: return
        val updated = session.copy(progress = UserRegistrationStep.SUBMISSION)
        savedStateHandle[KEY_PROGRESS] = UserRegistrationStep.SUBMISSION.name
        _state.update { it.copy(session = updated, step = UserRegistrationStep.SUBMISSION) }
    }

    fun onAnswerChange(questionId: Int, value: String) = _state.update { it.copy(answers = it.answers + (questionId to value)) }

    fun submitDetails(onContinueSolo: () -> Unit = {}) {
        val state = _state.value
        val draft = (state.create as? CreateState.Ready)?.draft ?: return
        val session = state.session ?: return
        if (state.savingDetails) return
        if (draft.questions.any { question -> question.backendId?.let { question.isMissing(state.answers[it]) } ?: question.isRequired }) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        val generation = ++answerSaveGeneration
        val registrationGeneration = initializationGeneration
        _state.update { it.copy(savingDetails = true, showErrors = false, transientError = null) }
        viewModelScope.launch {
            repository.saveIndividualAnswers(draft.registrationId, state.answers).fold(
                onSuccess = {
                    if (generation != answerSaveGeneration || registrationGeneration != initializationGeneration || _state.value.session != session) return@fold
                    val next = when {
                        draft.allowTeam -> UserRegistrationStep.PARTICIPATION
                        draft.allowSolo -> UserRegistrationStep.SUBMISSION
                        else -> UserRegistrationStep.DETAILS
                    }
                    updateSession(session.copy(progress = next), next)
                    _state.update { it.copy(savingDetails = false) }
                    // A solo-only event still continues to the shared questions step.
                },
                onFailure = { error -> if (generation == answerSaveGeneration && registrationGeneration == initializationGeneration && _state.value.session == session) _state.update {
                    it.copy(savingDetails = false, transientError = error.message ?: "Could not save your answers.")
                } },
            )
        }
    }

    fun selectMode(mode: ParticipationMode) {
        val draft = (_state.value.create as? CreateState.Ready)?.draft ?: return
        if (mode == ParticipationMode.SOLO && !draft.allowSolo || mode != ParticipationMode.SOLO && !draft.allowTeam) return
        _state.value.session?.let { session ->
            val updated = session.copy(mode = mode, teamId = if (mode == ParticipationMode.CREATE_TEAM) session.teamId else null)
            savedStateHandle[KEY_MODE] = mode.name
            if (mode != ParticipationMode.JOIN_TEAM) savedStateHandle.remove<Int>(KEY_TEAM_ID)
            _state.update { it.copy(mode = mode, session = updated) }
        }
    }

    fun setProgress(step: UserRegistrationStep) {
        val session = _state.value.session ?: return
        updateSession(session.copy(progress = step), step)
    }

    fun attachTeam(teamId: Int, mode: ParticipationMode, progress: UserRegistrationStep = UserRegistrationStep.SUBMISSION) {
        val session = _state.value.session ?: return
        val updated = session.copy(teamId = teamId, mode = mode, progress = progress)
        savedStateHandle[KEY_TEAM_ID] = teamId
        savedStateHandle[KEY_MODE] = mode.name
        savedStateHandle[KEY_REGISTRATION_ID] = updated.registrationId
        savedStateHandle[KEY_PROGRESS] = progress.name
        _state.update { it.copy(session = updated, mode = mode, step = progress) }
    }

    fun readyForSubmission() {
        val session = _state.value.session ?: return
        val updated = session.copy(progress = UserRegistrationStep.SUBMISSION)
        savedStateHandle[KEY_MODE] = updated.mode.name
        savedStateHandle[KEY_TEAM_ID] = updated.teamId
        savedStateHandle[KEY_REGISTRATION_ID] = updated.registrationId
        updateSession(updated, UserRegistrationStep.SUBMISSION)
    }

    fun soloDetailsSaved() {
        val session = _state.value.session ?: return
        if (session.mode != ParticipationMode.SOLO) return
        updateSession(session.copy(progress = UserRegistrationStep.SUBMISSION), UserRegistrationStep.SUBMISSION)
    }

    private fun updateSession(session: RegistrationSession, step: UserRegistrationStep) {
        savedStateHandle[KEY_PROGRESS] = step.name
        _state.update { it.copy(step = step, session = session.copy(progress = step)) }
    }

    fun consumeError() = _state.update { it.copy(transientError = null) }

    fun back(): Boolean = when (_state.value.step) {
        UserRegistrationStep.PARTICIPATION -> { setProgress(UserRegistrationStep.DETAILS); true }
        UserRegistrationStep.TEAM_PORTAL -> { setProgress(UserRegistrationStep.PARTICIPATION); true }
        else -> false
    }

    private companion object {
        const val KEY_EVENT_ID = "registration_event_id"
        const val KEY_REGISTRATION_ID = "registration_id"
        const val KEY_MODE = "registration_mode"
        const val KEY_TEAM_ID = "registration_team_id"
        const val KEY_PROGRESS = "registration_progress"
    }
}
