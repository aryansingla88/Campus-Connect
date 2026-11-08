package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.usecase.RegistrationQuestionUseCases
import com.example.campusconnect.feature.registrations.domain.usecase.GetRegistrationQuestions
import com.example.campusconnect.feature.registrations.domain.usecase.SaveRegistrationQuestions
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FormBuilderUiState(
    val eventId: Int? = null,
    val questions: List<RegistrationQuestion> = emptyList(),
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val validationError: String? = null,
    val saved: Boolean = false,
    val dirty: Boolean = false,
    val selectedQuestionId: String? = null,
    val quickFieldMask: Long = 0L,
)

data class RegistrationFormsUiState(
    val eventId: Int? = null,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
)

/** Owns both scopes as one ordered question set and persists them in one request. */
class RegistrationFormViewModel(useCases: RegistrationQuestionUseCases) : ViewModel() {
    constructor(repository: RegistrationRepository) : this(
        RegistrationQuestionUseCases(
            GetRegistrationQuestions(repository), SaveRegistrationQuestions(repository),
        ),
    )

    private val getQuestions = useCases.get
    private val saveQuestions = useCases.save

    private val _state = MutableStateFlow(FormBuilderUiState())
    val builderState = _state.asStateFlow()
    private val _legacyState = MutableStateFlow(RegistrationFormsUiState())
    val state = _legacyState.asStateFlow()

    val individualFields = _state.map { state -> state.questions.filter { it.scope == QuestionScope.INDIVIDUAL } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<RegistrationQuestion>())
    val teamFields = _state.map { state -> state.questions.filter { it.scope == QuestionScope.TEAM } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList<RegistrationQuestion>())

    private var loadJob: Job? = null
    private var saveJob: Job? = null

    fun load(eventId: Int) {
        if (_state.value.eventId == eventId && _state.value.loaded) return
        loadJob?.cancel()
        _state.value = FormBuilderUiState(eventId = eventId, loading = true)
        _legacyState.value = RegistrationFormsUiState(eventId = eventId, loading = true)
        loadJob = viewModelScope.launch {
            val result = try { getQuestions(eventId) } catch (error: Exception) { Result.failure(error) }
            if (_state.value.eventId != eventId) return@launch
            result.fold(
                onSuccess = { fields ->
                    _state.value = _state.value.withQuestions(fields).copy(loading = false, loaded = true)
                    syncLegacy()
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(loading = false,
                        error = error.message ?: "Could not load registration questions.")
                    syncLegacy()
                },
            )
        }
    }

    fun setIndividualFields(fields: List<RegistrationQuestion>) {
        val current = _state.value.questions.filter { it.scope != QuestionScope.INDIVIDUAL }
        updateQuestions(current + fields.map { it.copy(scope = QuestionScope.INDIVIDUAL) })
    }

    fun setTeamFields(fields: List<RegistrationQuestion>) {
        val current = _state.value.questions.filter { it.scope != QuestionScope.TEAM }
        updateQuestions(current + fields.map { it.copy(scope = QuestionScope.TEAM) })
    }

    fun moveToTeam(localIds: List<String>) {
        val ids = localIds.toSet()
        val current = _state.value
        val moving = current.questions.filter { it.scope == QuestionScope.INDIVIDUAL && it.localId in ids }
        if (moving.isEmpty()) return
        updateQuestions(current.questions.map { if (it.localId in ids) it.copy(scope = QuestionScope.TEAM) else it })
    }

    fun setSelectedQuestion(localId: String?) { _state.value = _state.value.copy(selectedQuestionId = localId) }

    fun setQuickFieldMask(mask: Long) {
        val changed = _state.value.quickFieldMask != mask
        _state.value = _state.value.copy(quickFieldMask = mask,
            dirty = _state.value.dirty || changed, saved = if (changed) false else _state.value.saved)
        syncLegacy()
    }

    fun save(eventId: Int, onComplete: (Boolean) -> Unit = {}) {
        val current = _state.value
        if (current.eventId != eventId || !current.loaded || current.loading || current.saving || saveJob?.isActive == true) {
            onComplete(false)
            return
        }
        if (!current.dirty) {
            _state.value = current.copy(saved = true, error = null)
            onComplete(true)
            return
        }
        _state.value = current.copy(saving = true, error = null, saved = false)
        val questions = current.questions
        saveJob = viewModelScope.launch {
            val result = try { saveQuestions(eventId, questions) } catch (error: Exception) { Result.failure(error) }
            if (_state.value.eventId != eventId) return@launch
            result.fold(
                onSuccess = { savedQuestions ->
                    // Keep the submitted edits and stable local IDs. Do not replace them with
                    // newly generated IDs if the server response ordering is incomplete.
                    val idsByBackend = questions.filter { it.backendId != null }.associateBy { it.backendId }
                    val reconciled = savedQuestions.mapIndexed { index, saved ->
                        val submitted = saved.backendId?.let(idsByBackend::get)
                            ?: questions.getOrNull(index)
                        if (submitted != null) saved.copy(localId = submitted.localId) else saved
                    }
                    val latest = _state.value
                    val persistedByLocalId = reconciled.associateBy { it.localId }
                    val latestWithPersistedIds = latest.questions.map { question ->
                        val persisted = persistedByLocalId[question.localId]
                        if (persisted == null) question else question.copy(backendId = persisted.backendId)
                    }
                    val unchangedSinceSubmit = latest.questions == questions
                    val displayedQuestions = if (unchangedSinceSubmit) reconciled else latestWithPersistedIds
                    _state.value = latest.withQuestions(displayedQuestions).copy(
                        saving = false,
                        loaded = true,
                        saved = unchangedSinceSubmit,
                        dirty = !unchangedSinceSubmit,
                        error = if (unchangedSinceSubmit) null else "Questions changed while saving. Save again to persist the latest edits.",
                        validationError = null,
                    )
                    syncLegacy()
                    onComplete(unchangedSinceSubmit)
                },
                onFailure = { error ->
                    _state.value = _state.value.copy(saving = false, error = error.message ?: "Could not save registration questions.")
                    syncLegacy()
                    onComplete(false)
                },
            )
        }
    }

    fun retry(eventId: Int) { if (_state.value.loaded) save(eventId) else load(eventId) }

    private fun updateQuestions(questions: List<RegistrationQuestion>) {
        _state.value = _state.value.copy(questions = questions,
            saved = false, dirty = true, error = null, validationError = null)
        syncLegacy()
    }

    private fun FormBuilderUiState.withQuestions(questions: List<RegistrationQuestion>) = copy(questions = questions.sortedBy { it.fieldOrder })

    private fun syncLegacy() {
        val current = _state.value
        _legacyState.value = RegistrationFormsUiState(
            eventId = current.eventId, loading = current.loading, loaded = current.loaded,
            saving = current.saving, error = current.error, saved = current.saved,
            hasUnsavedChanges = current.dirty,
        )
    }
}
