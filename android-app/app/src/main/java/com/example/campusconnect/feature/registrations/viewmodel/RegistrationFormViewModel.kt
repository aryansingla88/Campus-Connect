package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.RegistrationFieldRepository
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegistrationFormsUiState(
    val eventId: Int? = null,
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val saving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
)

/** Shared individual/team form state backed by the host registration-fields API. */
class RegistrationFormViewModel(
    private val repository: RegistrationFieldRepository = RegistrationFieldRepository(),
) : ViewModel() {
    private val _individualFields = MutableStateFlow<List<FormField>>(emptyList())
    val individualFields = _individualFields.asStateFlow()

    private val _teamFields = MutableStateFlow<List<FormField>>(emptyList())
    val teamFields = _teamFields.asStateFlow()

    private val _state = MutableStateFlow(RegistrationFormsUiState())
    val state = _state.asStateFlow()
    private var individualRegistrationMode = true

    fun setRegistrationMode(individual: Boolean) {
        individualRegistrationMode = individual
        if (!individual && _state.value.loaded && _individualFields.value.isNotEmpty()) {
            _teamFields.value = _teamFields.value + _individualFields.value.map { it.copy(individual = false) }
            _individualFields.value = emptyList()
            _state.value = _state.value.copy(saved = false, hasUnsavedChanges = true, error = null)
        }
    }

    fun load(eventId: Int) {
        if (_state.value.eventId == eventId && (_state.value.loaded || _state.value.loading)) return
        _state.value = RegistrationFormsUiState(eventId = eventId, loading = true)
        viewModelScope.launch {
            repository.load(eventId).onSuccess { fields ->
                if (individualRegistrationMode) {
                    _individualFields.value = fields.filter { it.individual }
                    _teamFields.value = fields.filterNot { it.individual }
                } else {
                    // Keep every existing question while switching the registration to team mode.
                    _individualFields.value = emptyList()
                    _teamFields.value = fields.map { it.copy(individual = false) }
                }
                _state.value = RegistrationFormsUiState(eventId = eventId, loaded = true)
            }.onFailure { error ->
                _state.value = RegistrationFormsUiState(
                    eventId = eventId,
                    error = error.message ?: "Could not load registration questions.",
                )
            }
        }
    }

    fun setIndividualFields(fields: List<FormField>) {
        _individualFields.value = fields.map { it.copy(individual = true) }
        _state.value = _state.value.copy(saved = false, hasUnsavedChanges = true, error = null)
    }

    fun setTeamFields(fields: List<FormField>) {
        _teamFields.value = fields.map { it.copy(individual = false) }
        _state.value = _state.value.copy(saved = false, hasUnsavedChanges = true, error = null)
    }

    fun moveToTeam(ids: List<Int>) {
        val idSet = ids.toSet()
        val moving = _individualFields.value.filter { it.id in idSet }.map { it.copy(individual = false) }
        if (moving.isEmpty()) return
        _individualFields.value = _individualFields.value.filterNot { it.id in idSet }
        _teamFields.value = _teamFields.value + moving
        _state.value = _state.value.copy(saved = false, hasUnsavedChanges = true, error = null)
    }

    fun save(eventId: Int) {
        val current = _state.value
        if (current.eventId != eventId || !current.loaded || current.loading || current.saving) return
        if (!current.hasUnsavedChanges) {
            _state.value = current.copy(saved = true, error = null)
            return
        }
        _state.value = current.copy(saving = true, error = null, saved = false)
        val fields = _individualFields.value + _teamFields.value
        viewModelScope.launch {
            repository.save(eventId, fields).onSuccess { savedFields ->
                _individualFields.value = savedFields.filter { it.individual }
                _teamFields.value = savedFields.filterNot { it.individual }
                repository.load(eventId).onSuccess { reloadedFields ->
                    _individualFields.value = reloadedFields.filter { it.individual }
                    _teamFields.value = reloadedFields.filterNot { it.individual }
                    _state.value = _state.value.copy(saving = false, loaded = true, saved = true, hasUnsavedChanges = false, error = null)
                }.onFailure { refreshError ->
                    _state.value = _state.value.copy(
                        saving = false,
                        loaded = true,
                        saved = true,
                        hasUnsavedChanges = false,
                        error = "Changes saved, but the form could not be reloaded: ${refreshError.message ?: "request failed"}",
                    )
                }
            }.onFailure { error ->
                // Keep the edited lists intact so the host can correct or retry the save.
                _state.value = _state.value.copy(
                    saving = false,
                    error = error.message ?: "Could not save registration questions.",
                )
            }
        }
    }

    fun retry(eventId: Int) {
        if (_state.value.loaded) save(eventId) else load(eventId)
    }
}
