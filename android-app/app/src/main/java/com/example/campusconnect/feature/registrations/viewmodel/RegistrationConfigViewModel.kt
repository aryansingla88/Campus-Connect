package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.repo.RegistrationConfigRepository
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegistrationConfigUiState(
    val settings: RegistrationSettings = RegistrationSettings(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val configExists: Boolean = false,
    val error: String? = null,
    val loadFailed: Boolean = false,
    val saved: Boolean = false,
)

class RegistrationConfigViewModel(
    private val repository: RegistrationConfigRepository = RegistrationConfigRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationConfigUiState())
    val state = _state.asStateFlow()
    private var loadedQuickFieldMask: Long? = null
    private var loadedSettings: RegistrationSettings? = null
    private var eventId: Int? = null

    fun load(eventId: Int) {
        if (this.eventId == eventId && !_state.value.loading && !_state.value.loadFailed) return
        this.eventId = eventId
        loadCurrentEvent()
    }

    private fun loadCurrentEvent() = viewModelScope.launch {
        val eventId = eventId ?: return@launch
        _state.value = _state.value.copy(loading = true, error = null, loadFailed = false)
        try {
            when (val result = repository.load(eventId)) {
                is RegistrationConfigRepository.LoadResult.Existing -> {
                    loadedQuickFieldMask = result.settings.quickFieldMask
                    loadedSettings = result.settings
                    _state.value = _state.value.copy(settings = result.settings, configExists = true, loading = false)
                }
                RegistrationConfigRepository.LoadResult.Missing -> _state.value = _state.value.copy(
                    loading = false, configExists = false,
                )
                is RegistrationConfigRepository.LoadResult.Failure -> _state.value = _state.value.copy(
                    loading = false, error = result.message, loadFailed = true,
                )
            }
        } catch (error: Exception) {
            _state.value = _state.value.copy(
                loading = false,
                error = error.message ?: "Could not load registration settings.",
                loadFailed = true,
            )
        }
    }

    fun updateSettings(settings: RegistrationSettings) {
        if (settings.minMembers < 1 || settings.maxMembers < settings.minMembers) return
        _state.value = _state.value.copy(settings = settings, saved = false, error = null)
    }

    fun save(onComplete: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val current = _state.value
        if (current.loading || current.saving || current.error != null) return@launch
        _state.value = current.copy(saving = true, error = null, saved = false)
        val eventId = eventId ?: return@launch
        val result = repository.save(eventId, current.settings, current.configExists)
        _state.value = _state.value.copy(
            saving = false,
            configExists = if (result.isSuccess) true else current.configExists,
            saved = result.isSuccess,
            settings = result.getOrNull() ?: current.settings,
            error = result.exceptionOrNull()?.message,
        )
        if (result.isSuccess) {
            loadedQuickFieldMask = result.getOrThrow().quickFieldMask
            loadedSettings = result.getOrThrow()
        }
        onComplete(result.isSuccess)
    }

    fun saveQuickFields(onComplete: (Boolean) -> Unit = {}) = viewModelScope.launch {
        val current = _state.value
        if (current.loading || current.saving || current.loadFailed) return@launch
        _state.value = current.copy(saving = true, error = null, saved = false)
        val baseSettings = loadedSettings
        val nonQuickSettingsChanged = baseSettings == null ||
            current.settings.minMembers != baseSettings.minMembers ||
            current.settings.maxMembers != baseSettings.maxMembers ||
            current.settings.registrationCreatedAt != baseSettings.registrationCreatedAt ||
            current.settings.deadline != baseSettings.deadline
        if (nonQuickSettingsChanged) {
            // Persist actual setup edits (or create the required config row) without
            // including pending Quick Fields changes in the full config request.
            val settingsWithSavedMask = current.settings.copy(
                quickFieldMask = loadedQuickFieldMask ?: 0L,
            )
            val eventId = eventId ?: return@launch
            val configResult = repository.save(eventId, settingsWithSavedMask, current.configExists)
            if (configResult.isFailure) {
                val error = configResult.exceptionOrNull()
                _state.value = _state.value.copy(saving = false, saved = false,
                    error = error?.message ?: "Could not save registration settings.")
                onComplete(false)
                return@launch
            }
            loadedSettings = configResult.getOrThrow()
            loadedQuickFieldMask = configResult.getOrThrow().quickFieldMask
            _state.value = _state.value.copy(configExists = true)
        }
        if (current.settings.quickFieldMask == loadedQuickFieldMask) {
            _state.value = _state.value.copy(saving = false, saved = true, error = null)
            onComplete(true)
            return@launch
        }
        val eventId = eventId ?: return@launch
        repository.saveQuickFields(eventId, current.settings.quickFieldMask).fold(
            onSuccess = { savedSettings ->
                loadedQuickFieldMask = savedSettings.quickFieldMask
                loadedSettings = savedSettings
                _state.value = _state.value.copy(settings = _state.value.settings.copy(
                    quickFieldMask = savedSettings.quickFieldMask,
                ), saving = false, saved = true, error = null)
                onComplete(true)
            },
            onFailure = { error ->
                _state.value = _state.value.copy(saving = false, saved = false,
                    error = error.message ?: "Could not save Quick Fields.")
                onComplete(false)
            },
        )
    }

    fun retryLoad() { if (eventId != null) loadCurrentEvent() }
}
