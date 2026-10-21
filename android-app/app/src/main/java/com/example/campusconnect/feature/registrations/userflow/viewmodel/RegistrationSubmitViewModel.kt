package com.example.campusconnect.feature.registrations.userflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.userflow.data.ParticipantRegistrationRepositories
import com.example.campusconnect.feature.registrations.userflow.data.RegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.SavedRegistrationDetails
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenConfig
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenKind
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationQuestionContext
import com.example.campusconnect.feature.registrations.userflow.model.updateAnswer
import com.example.campusconnect.feature.registrations.userflow.model.validateAnswers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SubmitPhase {
    /** Fetching the team questions. */
    object Loading : SubmitPhase
    data class LoadFailed(val message: String) : SubmitPhase
    object Form : SubmitPhase
    object Submitting : SubmitPhase
    data class Success(val receipt: SubmissionReceipt) : SubmitPhase
}

data class SubmitUiState(
    val phase: SubmitPhase = SubmitPhase.Loading,
    val questions: List<FormField> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val validationErrors: Map<Int, String> = emptyMap(),
    val transientError: String? = null,
    val questionScreenConfig: RegistrationQuestionScreenConfig = RegistrationQuestionScreenConfig(
        kind = RegistrationQuestionScreenKind.TEAM,
        subtitle = "Answer the shared participation questions.",
        progressStep = 3,
        actionLabel = "Submit Registration",
    ),
)

class RegistrationSubmitViewModel(
    private val repository: RegistrationSubmitRepository = ParticipantRegistrationRepositories.submit,
) : ViewModel() {

    fun savedRegistration(participantId: Int): SavedRegistrationDetails? = repository.getSavedRegistration(participantId)

    private val _state = MutableStateFlow(SubmitUiState())
    val state: StateFlow<SubmitUiState> = _state.asStateFlow()

    private var context: SubmissionContext? = null

    /** Safe to call on every composition; only (re)starts for a new context or after a load failure. */
    fun start(newContext: SubmissionContext) {
        if (context == newContext && _state.value.phase !is SubmitPhase.LoadFailed) return
        context = newContext
        _state.value = SubmitUiState(questionScreenConfig = screenConfig(newContext))
        loadQuestions()
    }

    fun retryLoad() = loadQuestions()

    private fun screenConfig(ctx: SubmissionContext) = RegistrationQuestionScreenConfig(
        kind = RegistrationQuestionScreenKind.TEAM,
        subtitle = when (ctx.mode) {
            ParticipationMode.SOLO -> "Answer the shared participation questions."
            ParticipationMode.JOIN_TEAM -> "Answer the questions for your team registration."
            ParticipationMode.CREATE_TEAM -> "Answer the shared questions for ${ctx.teamName.orEmpty()}."
        },
        progressStep = 3,
        actionLabel = "Submit Registration",
    )

    private fun loadQuestions() {
        val ctx = context ?: return
        _state.update { it.copy(phase = SubmitPhase.Loading) }
        viewModelScope.launch {
            runCatching {
                val questions = repository.getTeamQuestions(ctx.eventId).getOrThrow()
                val savedAnswers = repository.getSavedTeamAnswers(ctx.eventId).getOrThrow()
                questions to savedAnswers
            }.fold(
                onSuccess = { (questions, savedAnswers) ->
                    val applicableQuestions = questions.filter { question ->
                        when (question.registrationContext) {
                            RegistrationQuestionContext.TEAM_COMMON -> ctx.mode != ParticipationMode.JOIN_TEAM
                            RegistrationQuestionContext.TEAM_CREATION -> ctx.mode != ParticipationMode.JOIN_TEAM
                            RegistrationQuestionContext.INDIVIDUAL -> false
                            null -> !question.individual
                        }
                    }
                    _state.update {
                        it.copy(
                            questions = applicableQuestions,
                            answers = (savedAnswers + ctx.initialTeamAnswers)
                                .filterKeys { id -> applicableQuestions.any { it.id == id } },
                            phase = SubmitPhase.Form,
                        )
                    }
                },
                onFailure = { e ->
                    _state.update {
                        it.copy(phase = SubmitPhase.LoadFailed(e.message ?: "Couldn't load the registration form."))
                    }
                },
            )
        }
    }

    fun onAnswerChange(questionId: Int, value: String) {
        _state.update { current ->
            val answerState = current.questions.updateAnswer(
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

    fun submit() {
        val ctx = context ?: return
        val current = _state.value
        if (current.phase is SubmitPhase.Submitting) return

        val validationErrors = current.questions.validateAnswers(current.answers)
        if (validationErrors.isNotEmpty()) {
            _state.update { it.copy(validationErrors = validationErrors) }
            return
        }

        _state.update { it.copy(phase = SubmitPhase.Submitting, validationErrors = emptyMap()) }
        viewModelScope.launch {
            runCatching { repository.submit(SubmissionRequest(ctx, current.answers)).getOrThrow() }.fold(
                onSuccess = { receipt -> _state.update { it.copy(phase = SubmitPhase.Success(receipt)) } },
                onFailure = { e ->
                    // Back to the form with answers intact.
                    _state.update {
                        it.copy(
                            phase = SubmitPhase.Form,
                            transientError = e.message ?: "Couldn't submit your registration. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun consumeError() = _state.update { it.copy(transientError = null) }
}
