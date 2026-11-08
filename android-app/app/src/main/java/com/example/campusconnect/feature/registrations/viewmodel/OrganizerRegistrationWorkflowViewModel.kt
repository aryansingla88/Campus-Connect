package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class OrganizerSaveFailure { SETTINGS, QUICK_FIELDS, QUESTIONS }

data class OrganizerRegistrationWorkflowUiState(
    val saving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val failedOperation: OrganizerSaveFailure? = null,
)

/** Coordinates the non-atomic setup and form APIs without owning duplicate editable data. */
class OrganizerRegistrationWorkflowViewModel : ViewModel() {
    private val _state = MutableStateFlow(OrganizerRegistrationWorkflowUiState())
    val state = _state.asStateFlow()

    fun saveFormChanges(
        config: RegistrationConfigViewModel,
        form: RegistrationFormViewModel,
        eventId: Int,
        onComplete: (Boolean) -> Unit = {},
    ) {
        if (_state.value.saving) { onComplete(false); return }
        _state.value = OrganizerRegistrationWorkflowUiState(saving = true)
        config.saveAll { settingsSaved ->
            if (!settingsSaved) {
                _state.value = OrganizerRegistrationWorkflowUiState(
                    saving = false,
                    error = config.state.value.error ?: "Registration settings could not be saved.",
                    failedOperation = if (config.state.value.error?.contains("Quick Fields", ignoreCase = true) == true)
                        OrganizerSaveFailure.QUICK_FIELDS else OrganizerSaveFailure.SETTINGS,
                )
                onComplete(false)
                return@saveAll
            }
            form.save(eventId) { questionsSaved ->
                _state.value = OrganizerRegistrationWorkflowUiState(
                    saving = false,
                    saved = questionsSaved,
                    error = if (questionsSaved) null else "Settings and Quick Fields saved; individual or team questions could not be saved: ${form.builderState.value.error ?: "request failed"}",
                    failedOperation = if (questionsSaved) null else OrganizerSaveFailure.QUESTIONS,
                )
                onComplete(questionsSaved)
            }
        }
    }

    fun publishAfterSave(
        config: RegistrationConfigViewModel,
        form: RegistrationFormViewModel,
        eventId: Int,
        onComplete: (Boolean) -> Unit = {},
    ) {
        saveFormChanges(config, form, eventId, onComplete)
    }
}
