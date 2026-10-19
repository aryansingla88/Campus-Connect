package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.FakeRegistrationResponseRepository
import com.example.campusconnect.feature.registrations.data.RegistrationResponseRepository
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RegistrationDetailState {
    object Loading : RegistrationDetailState
    data class Loaded(val detail: RegistrationDetailResponse) : RegistrationDetailState
    data class Failed(val message: String) : RegistrationDetailState
}

/**
 * Scoped to the registration list's back-stack entry, so the inline preview and
 * the full-screen detail share one cache: opening "Full Response" never refetches.
 */
class RegistrationResponsesViewModel(
    private val repository: RegistrationResponseRepository = FakeRegistrationResponseRepository(),
) : ViewModel() {

    private val _details = MutableStateFlow<Map<Int, RegistrationDetailState>>(emptyMap())
    val details: StateFlow<Map<Int, RegistrationDetailState>> = _details.asStateFlow()

    /** Registration id -> selected member's userId. */
    private val _selectedMembers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedMembers: StateFlow<Map<Int, Int>> = _selectedMembers.asStateFlow()

    /** Only one card is expanded at a time. */
    private val _expandedId = MutableStateFlow<Int?>(null)
    val expandedId: StateFlow<Int?> = _expandedId.asStateFlow()

    fun toggleExpanded(registrationId: Int) {
        val next = if (_expandedId.value == registrationId) null else registrationId
        _expandedId.value = next
        if (next != null) loadDetails(next)
    }

    fun selectMember(registrationId: Int, userId: Int) {
        _selectedMembers.update { it + (registrationId to userId) }
    }

    fun loadDetails(registrationId: Int, force: Boolean = false) {
        val current = _details.value[registrationId]
        if (!force && (current is RegistrationDetailState.Loading || current is RegistrationDetailState.Loaded)) return

        _details.update { it + (registrationId to RegistrationDetailState.Loading) }
        viewModelScope.launch {
            val state = repository.getDetails(registrationId).fold(
                onSuccess = { RegistrationDetailState.Loaded(it) },
                onFailure = { RegistrationDetailState.Failed(it.message ?: "Couldn't load responses.") },
            )
            _details.update { it + (registrationId to state) }
        }
    }
}
