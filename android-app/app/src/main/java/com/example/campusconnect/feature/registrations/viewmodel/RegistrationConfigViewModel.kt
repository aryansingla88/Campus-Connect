package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.domain.repository.RegistrationRepository
import com.example.campusconnect.feature.registrations.domain.usecase.GetRegistrationSetup
import com.example.campusconnect.feature.registrations.domain.usecase.SaveQuickFields
import com.example.campusconnect.feature.registrations.domain.usecase.SaveRegistrationSettings
import com.example.campusconnect.feature.registrations.model.RegistrationConfigLoadResult
import com.example.campusconnect.feature.registrations.model.RegistrationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class RegistrationConfigurationStatus { NOT_CREATED, CREATED_EMPTY, EXISTING_VALUES }

data class RegistrationSetupUiState(
    val settings: RegistrationSettings = RegistrationSettings(),
    val loading: Boolean = true,
    val saving: Boolean = false,
    val configExists: Boolean = false,
    val configurationStatus: RegistrationConfigurationStatus = RegistrationConfigurationStatus.NOT_CREATED,
    val dirty: Boolean = false,
    val hasUnsavedChanges: Boolean = false,
    val error: String? = null,
    val loadFailed: Boolean = false,
    val saved: Boolean = false,
    val publishAvailable: Boolean = false,
    val publishing: Boolean = false,
    val publishError: String? = null,
    val publishSucceeded: Boolean = false,
)

typealias RegistrationConfigUiState = RegistrationSetupUiState

/** Owns event registration settings and coordinates settings/Quick Fields persistence. */
class RegistrationConfigViewModel(
    repository: RegistrationRepository,
) : ViewModel() {
    private val getSetup = GetRegistrationSetup(repository)
    private val saveSettings = SaveRegistrationSettings(repository)
    private val saveQuickFields = SaveQuickFields(repository)

    private val _state = MutableStateFlow(RegistrationSetupUiState())
    val state = _state.asStateFlow()

    private var eventId: Int? = null
    private var settingsBaseline: RegistrationSettings? = null
    private var quickFieldsBaseline: Long? = null
    private var lastConfigSaveSucceeded = false

    fun load(eventId: Int) {
        if (this.eventId == eventId && !_state.value.loadFailed && !_state.value.loading) return
        if (this.eventId == eventId && _state.value.loading) return
        this.eventId = eventId
        settingsBaseline = null
        quickFieldsBaseline = null
        _state.value = RegistrationSetupUiState(loading = true)
        viewModelScope.launch {
            val result = try { getSetup(eventId) } catch (error: Exception) {
                RegistrationConfigLoadResult.Failure(error.message ?: "Could not load registration settings.")
            }
            if (this@RegistrationConfigViewModel.eventId != eventId) return@launch
            when (result) {
                is RegistrationConfigLoadResult.Existing -> {
                    settingsBaseline = result.settings
                    quickFieldsBaseline = result.settings.quickFieldMask
                    val hasValues = result.settings != RegistrationSettings()
                    _state.value = RegistrationSetupUiState(
                        settings = result.settings,
                        loading = false,
                        configExists = true,
                        configurationStatus = if (hasValues) RegistrationConfigurationStatus.EXISTING_VALUES
                            else RegistrationConfigurationStatus.CREATED_EMPTY,
                        publishAvailable = result.settings.allowIndividual || result.settings.allowTeam,
                    )
                }
                RegistrationConfigLoadResult.Missing -> {
                    settingsBaseline = null
                    quickFieldsBaseline = null
                    _state.value = RegistrationSetupUiState(
                        settings = RegistrationSettings(), loading = false,
                        configurationStatus = RegistrationConfigurationStatus.NOT_CREATED,
                        publishAvailable = false,
                    )
                }
                is RegistrationConfigLoadResult.Failure -> _state.value = RegistrationSetupUiState(
                    loading = false, error = result.message, loadFailed = true,
                )
            }
        }
    }

    fun updateSettings(settings: RegistrationSettings) {
        if (settings.minMembers < 1 || settings.maxMembers < settings.minMembers) return
        val current = _state.value
        val baseline = settingsBaseline
        val changed = baseline == null || settings != baseline
        _state.value = current.copy(
            settings = settings,
            dirty = changed,
            hasUnsavedChanges = changed,
            saved = false,
            error = null,
            publishError = null,
            publishSucceeded = false,
            publishAvailable = settings.allowIndividual || settings.allowTeam,
        )
    }

    /** Persist each changed operation independently; report partial success and retain dirty data. */
    fun saveAll(onComplete: (Boolean) -> Unit = {}) {
        val current = _state.value
        if (current.loading || current.saving || current.loadFailed) { onComplete(false); return }
        val event = eventId ?: run { onComplete(false); return }
        val requestedSettings = current.settings
        viewModelScope.launch {
            lastConfigSaveSucceeded = false
            _state.value = _state.value.copy(saving = true, saved = false, error = null)
            var configExists = current.configExists
            var configSaved = false

            val baseline = settingsBaseline
            val settingsChanged = baseline == null || requestedSettings.minMembers != baseline.minMembers ||
                requestedSettings.maxMembers != baseline.maxMembers || requestedSettings.autoConfirm != baseline.autoConfirm ||
                requestedSettings.registrationCreatedAt != baseline.registrationCreatedAt || requestedSettings.deadline != baseline.deadline
            if (settingsChanged || !configExists) {
                val savedSettings = requestedSettings.copy(quickFieldMask = quickFieldsBaseline ?: 0L)
                val result = try { saveSettings(event, savedSettings, configExists) }
                catch (error: Exception) { Result.failure(error) }
                result.fold(
                    onSuccess = {
                        lastConfigSaveSucceeded = true
                        configSaved = true
                        configExists = true
                        settingsBaseline = savedSettings
                        quickFieldsBaseline = savedSettings.quickFieldMask
                        _state.value = _state.value.copy(
                            configExists = true,
                            configurationStatus = if (savedSettings == RegistrationSettings()) RegistrationConfigurationStatus.CREATED_EMPTY
                                else RegistrationConfigurationStatus.EXISTING_VALUES,
                        )
                    },
                    onFailure = { error ->
                        _state.value = _state.value.copy(saving = false, configExists = configExists,
                            dirty = true, hasUnsavedChanges = true,
                            error = "Registration settings could not be saved: ${error.message ?: "request failed"}")
                        onComplete(false)
                        return@launch
                    },
                )
            }

            if (requestedSettings.quickFieldMask != quickFieldsBaseline) {
                if (!configExists) {
                    _state.value = _state.value.copy(saving = false, dirty = true,
                        error = "Registration settings must be saved before Quick Fields.")
                    onComplete(false)
                    return@launch
                }
                val result = try { saveQuickFields(event, requestedSettings.quickFieldMask) }
                catch (error: Exception) { Result.failure(error) }
                result.fold(
                    onSuccess = { savedSettings ->
                        lastConfigSaveSucceeded = true
                        quickFieldsBaseline = savedSettings.quickFieldMask
                        settingsBaseline = settingsBaseline?.copy(quickFieldMask = savedSettings.quickFieldMask)
                    },
                    onFailure = { error ->
                        val message = "Quick Fields could not be saved: ${error.message ?: "request failed"}"
                        val dirty = _state.value.settings != settingsBaseline
                        _state.value = _state.value.copy(saving = false, configExists = configExists,
                            configurationStatus = if (!configExists) RegistrationConfigurationStatus.NOT_CREATED
                                else if (settingsBaseline == RegistrationSettings()) RegistrationConfigurationStatus.CREATED_EMPTY
                                else RegistrationConfigurationStatus.EXISTING_VALUES,
                            dirty = dirty, hasUnsavedChanges = dirty,
                            error = if (configSaved) "Settings saved; $message" else message)
                        onComplete(false)
                        return@launch
                    },
                )
            }

            val latest = _state.value.settings
            val dirty = latest != settingsBaseline
            _state.value = _state.value.copy(
                settings = latest,
                saving = false,
                configExists = configExists,
                configurationStatus = if (!configExists) RegistrationConfigurationStatus.NOT_CREATED
                    else if (settingsBaseline == RegistrationSettings()) RegistrationConfigurationStatus.CREATED_EMPTY
                    else RegistrationConfigurationStatus.EXISTING_VALUES,
                dirty = dirty,
                hasUnsavedChanges = dirty,
                saved = configSaved || !dirty,
                error = null,
            )
            onComplete(!dirty)
        }
    }

    fun updateQuickFields(mask: Long) {
        updateSettings(_state.value.settings.copy(quickFieldMask = mask))
    }

    fun markPublishedSaveSucceeded() {
        _state.value = _state.value.copy(publishing = false, publishSucceeded = true, publishError = null, saved = true)
    }

    fun markPublishedSaveFailed() {
        _state.value = _state.value.copy(publishing = false, publishSucceeded = false,
            publishError = if (lastConfigSaveSucceeded && _state.value.error != null)
                "Settings saved; ${_state.value.error}"
            else _state.value.error ?: "Registration changes could not be saved.")
    }

    fun retryLoad() { eventId?.let(::load) }
}
