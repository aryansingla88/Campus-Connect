package com.example.campusconnect.feature.registrations.userflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.userflow.data.FakeRegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.data.RegistrationSubmitRepository
import com.example.campusconnect.feature.registrations.userflow.model.AttachedFile
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionRequest
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.model.isMissing
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
    val questions: List<UserQuestion> = emptyList(),
    val answers: Map<Int, String> = emptyMap(),
    val files: Map<Int, AttachedFile> = emptyMap(),
    val fileErrors: Map<Int, String> = emptyMap(),
    val showErrors: Boolean = false,
    val transientError: String? = null,
)

class RegistrationSubmitViewModel(
    private val repository: RegistrationSubmitRepository = FakeRegistrationSubmitRepository(),
) : ViewModel() {

    private val _state = MutableStateFlow(SubmitUiState())
    val state: StateFlow<SubmitUiState> = _state.asStateFlow()

    private var context: SubmissionContext? = null

    /** Safe to call on every composition; only (re)starts for a new context or after a load failure. */
    fun start(newContext: SubmissionContext) {
        if (context == newContext && _state.value.phase !is SubmitPhase.LoadFailed) return
        context = newContext
        _state.value = SubmitUiState()
        loadQuestions()
    }

    fun retryLoad() = loadQuestions()

    private fun loadQuestions() {
        val ctx = context ?: return
        _state.update { it.copy(phase = SubmitPhase.Loading) }
        viewModelScope.launch {
            // Team-level answers belong to the participant creating the team.
            // Solo participants and accepted team members should not fill them again.
            val questionsResult: Result<List<UserQuestion>> = if (ctx.mode == ParticipationMode.CREATE_TEAM) {
                repository.getTeamQuestions(ctx.eventId)
            } else {
                Result.success(emptyList())
            }
            questionsResult.fold(
                onSuccess = { questions ->
                    _state.update { it.copy(questions = questions, phase = SubmitPhase.Form) }
                    // Nothing to ask: submit straight away.
                    if (questions.isEmpty()) submit()
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
        _state.update { it.copy(answers = it.answers + (questionId to value)) }
    }

    fun onFilePicked(questionId: Int, file: AttachedFile) {
        val question = _state.value.questions.firstOrNull { it.id == questionId } ?: return
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
        val current = _state.value
        if (current.phase is SubmitPhase.Submitting) return

        val missing = current.questions.any { it.isMissing(current.answers[it.id], current.files[it.id]) }
        if (missing) {
            _state.update { it.copy(showErrors = true) }
            return
        }

        _state.update { it.copy(phase = SubmitPhase.Submitting, showErrors = false) }
        viewModelScope.launch {
            repository.submit(SubmissionRequest(ctx, current.answers, current.files)).fold(
                onSuccess = { receipt -> _state.update { it.copy(phase = SubmitPhase.Success(receipt)) } },
                onFailure = { e ->
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
}
