package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.domain.repository.SubmissionRepository
import com.example.campusconnect.feature.registrations.model.AttachedFile
import com.example.campusconnect.feature.registrations.model.ParticipationMode
import com.example.campusconnect.feature.registrations.model.SubmissionContext
import com.example.campusconnect.feature.registrations.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.ui.mapper.toParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.ui.model.isMissing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SubmitPhase {
    /** Fetching the shared questions. */
    data object Loading : SubmitPhase
    data class LoadFailed(val message: String) : SubmitPhase
    data object Form : SubmitPhase
    data object Submitting : SubmitPhase
    data class Success(val receipt: SubmissionReceipt) : SubmitPhase
}

data class SubmitUiState(
    val phase: SubmitPhase = SubmitPhase.Loading,
    val questions: List<ParticipantQuestionUiModel> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val files: Map<Int, AttachedFile> = emptyMap(),
    val fileErrors: Map<Int, String> = emptyMap(),
    val showErrors: Boolean = false,
    val transientError: String? = null,
)

class SubmissionViewModel(
    private val repository: SubmissionRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitUiState())
    val state: StateFlow<SubmitUiState> = _state.asStateFlow()

    private var context: SubmissionContext? = null
    private var generation = 0L
    private val inFlightSubmissions = mutableSetOf<String>()

    /** Safe to call on every composition; only (re)starts for a new context or after a load failure. */
    fun start(newContext: SubmissionContext) {
        if (context == newContext && _state.value.phase !is SubmitPhase.LoadFailed) return
        generation += 1
        val requestGeneration = generation
        context = newContext
        _state.value = SubmitUiState()
        loadQuestions(requestGeneration, newContext)
    }

    fun retryLoad() {
        val ctx = context ?: return
        generation += 1
        loadQuestions(generation, ctx)
    }

    private fun loadQuestions(requestGeneration: Long, ctx: SubmissionContext) {
        _state.update { it.copy(phase = SubmitPhase.Loading) }
        viewModelScope.launch {
            // Shared answers are completed by a solo registrant or the team creator.
            // Accepted team members submit only their participant answers.
            if (ctx.mode == ParticipationMode.JOIN_TEAM) {
                if (!isCurrent(requestGeneration, ctx)) return@launch
                _state.update { it.copy(questions = emptyList(), phase = SubmitPhase.Submitting) }
                if (!inFlightSubmissions.add(requestKey(ctx))) return@launch
                repository.submit(SubmissionRequest(ctx, emptyMap(), emptyMap())).fold(
                    onSuccess = { receipt ->
                        inFlightSubmissions.remove(requestKey(ctx))
                        if (isCurrent(requestGeneration, ctx)) _state.update { it.copy(phase = SubmitPhase.Success(receipt)) }
                    },
                    onFailure = { error ->
                        inFlightSubmissions.remove(requestKey(ctx))
                        if (isCurrent(requestGeneration, ctx)) _state.update { it.copy(
                            phase = SubmitPhase.LoadFailed(error.message ?: "Couldn't submit."),
                            transientError = error.message ?: "Couldn't submit your registration. Please try again.",
                        ) }
                    },
                )
                return@launch
            }

            repository.getTeamQuestions(ctx.eventId, ctx.registrationId).fold(
                onSuccess = { questionSet ->
                    if (!isCurrent(requestGeneration, ctx)) return@fold
                    val questions = questionSet.questions.mapNotNull { it.toParticipantQuestionUiModel() }
                    _state.update { it.copy(questions = questions, answers = questionSet.savedAnswers, phase = SubmitPhase.Form) }
                    // Nothing to ask: submit straight away.
                    if (questions.isEmpty()) submit(requestGeneration, ctx)
                },
                onFailure = { e -> if (isCurrent(requestGeneration, ctx)) {
                    _state.update {
                        it.copy(phase = SubmitPhase.LoadFailed(e.message ?: "Couldn't load the registration form."))
                    }
                } },
            )
        }
    }

    fun onAnswerChange(questionId: Int, value: String) {
        _state.update { it.copy(answers = it.answers + (questionId to value)) }
    }

    fun onFilePicked(questionId: Int, file: AttachedFile) {
        val question = _state.value.questions.firstOrNull { it.backendId == questionId } ?: return
        if (file.sizeBytes > question.maxFileBytes) {
            val mb = question.maxFileBytes / (1024 * 1024)
            _state.update { it.copy(fileErrors = it.fileErrors + (questionId to "File is too large (max $mb MB).")) }
            return
        }
        _state.update { it.copy(files = it.files + (questionId to file), fileErrors = it.fileErrors - questionId) }
    }

    fun onFileRemoved(questionId: Int) {
        _state.update { it.copy(files = it.files - questionId, fileErrors = it.fileErrors - questionId) }
    }

    fun submit() {
        val ctx = context ?: return
        val phase = _state.value.phase
        if (phase !is SubmitPhase.Form && !(phase == SubmitPhase.Submitting && _state.value.questions.isEmpty())) return
        submit(generation, ctx)
    }

    private fun submit(requestGeneration: Long, ctx: SubmissionContext) {
        val current = _state.value
        val automaticNoQuestionsSubmit = current.phase == SubmitPhase.Submitting && current.questions.isEmpty()
        if ((current.phase is SubmitPhase.Submitting && !automaticNoQuestionsSubmit) || !isCurrent(requestGeneration, ctx)) return
        val missing = current.questions.any { it.isMissing(current.answers[it.backendId], current.files[it.backendId]) }
        if (missing) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        if (!inFlightSubmissions.add(requestKey(ctx))) return

        _state.update { it.copy(phase = SubmitPhase.Submitting, showErrors = false) }
        viewModelScope.launch {
            repository.submit(SubmissionRequest(ctx, current.answers, current.files)).fold(
                onSuccess = { receipt ->
                    inFlightSubmissions.remove(requestKey(ctx))
                    if (isCurrent(requestGeneration, ctx)) _state.update { it.copy(phase = SubmitPhase.Success(receipt)) }
                },
                onFailure = { e ->
                    inFlightSubmissions.remove(requestKey(ctx))
                    if (!isCurrent(requestGeneration, ctx)) return@fold
                    // Back to the form with answers intact.
                    _state.update {
                        it.copy(
                            phase = if (it.questions.isEmpty()) SubmitPhase.LoadFailed(e.message ?: "Couldn't submit.")
                            else SubmitPhase.Form,
                            transientError = e.message ?: "Couldn't submit your registration. Please try again.",
                        )
                    }
                },
            )
        }
    }

    fun consumeError() = _state.update { it.copy(transientError = null) }

    private fun isCurrent(requestGeneration: Long, expected: SubmissionContext): Boolean =
        requestGeneration == generation && context == expected

    private fun requestKey(ctx: SubmissionContext) = "${ctx.eventId}:${ctx.registrationId}"
}


