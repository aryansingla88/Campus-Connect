package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.repo.NetworkRegistrationResponseRepository
import com.example.campusconnect.feature.registrations.data.repo.RegistrationResponseRepository
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
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

data class RegistrationListUiState(
    val eventId: Int? = null,
    val loading: Boolean = true,
    val loaded: Boolean = false,
    val error: String? = null,
    val registrations: List<RegistrationResponseItem> = emptyList(),
)

/**
 * Scoped to the registration list's back-stack entry, so the inline preview and
 * the full-screen detail share one cache: opening "Full Response" never refetches.
 */
class RegistrationResponsesViewModel(
    private val repository: RegistrationResponseRepository = NetworkRegistrationResponseRepository(),
) : ViewModel() {

    private val _listState = MutableStateFlow(RegistrationListUiState())
    val listState: StateFlow<RegistrationListUiState> = _listState.asStateFlow()

    private val _details = MutableStateFlow<Map<Int, RegistrationDetailState>>(emptyMap())
    val details: StateFlow<Map<Int, RegistrationDetailState>> = _details.asStateFlow()

    /** Registration id -> selected member's userId. */
    private val _selectedMembers = MutableStateFlow<Map<Int, Int>>(emptyMap())
    val selectedMembers: StateFlow<Map<Int, Int>> = _selectedMembers.asStateFlow()

    /** Only one card is expanded at a time. */
    private val _expandedId = MutableStateFlow<Int?>(null)
    val expandedId: StateFlow<Int?> = _expandedId.asStateFlow()

    fun loadRegistrations(eventId: Int, force: Boolean = false) {
        val current = _listState.value
        if (!force && current.eventId == eventId && (current.loading || current.loaded)) return
        _listState.value = RegistrationListUiState(eventId = eventId, loading = true)
        _details.value = emptyMap()
        _selectedMembers.value = emptyMap()
        _expandedId.value = null
        viewModelScope.launch {
            repository.getRegistrations(eventId).fold(
                onSuccess = { registrations ->
                    if (_listState.value.eventId == eventId) {
                        _listState.value = RegistrationListUiState(
                            eventId = eventId,
                            loading = false,
                            loaded = true,
                            registrations = registrations,
                        )
                    }
                },
                onFailure = { error ->
                    if (_listState.value.eventId == eventId) {
                        _listState.value = RegistrationListUiState(
                            eventId = eventId,
                            loading = false,
                            error = error.message ?: "Could not load registrations.",
                        )
                    }
                },
            )
        }
    }

    fun toggleExpanded(registrationId: Int) {
        val eventId = _listState.value.eventId ?: return
        val next = if (_expandedId.value == registrationId) null else registrationId
        _expandedId.value = next
        if (next != null) loadDetails(eventId, next)
    }

    fun selectMember(registrationId: Int, userId: Int) {
        _selectedMembers.update { it + (registrationId to userId) }
    }

    fun loadDetails(eventId: Int, registrationId: Int, force: Boolean = false) {
        if (_listState.value.eventId != eventId) return
        val current = _details.value[registrationId]
        if (!force && (current is RegistrationDetailState.Loading || current is RegistrationDetailState.Loaded)) return

        _details.update { it + (registrationId to RegistrationDetailState.Loading) }
        viewModelScope.launch {
            val state = repository.getDetails(eventId, registrationId).fold(
                onSuccess = { RegistrationDetailState.Loaded(it) },
                onFailure = { RegistrationDetailState.Failed(it.message ?: "Couldn't load responses.") },
            )
            if (_listState.value.eventId == eventId) {
                _details.update { it + (registrationId to state) }
            }
        }
    }

    fun approve(eventId: Int, registrationId: Int) = review(eventId, registrationId, approve = true)

    fun reject(eventId: Int, registrationId: Int) = review(eventId, registrationId, approve = false)

    private fun review(eventId: Int, registrationId: Int, approve: Boolean) = viewModelScope.launch {
        val result = if (approve) repository.approve(eventId, registrationId) else repository.reject(eventId, registrationId)
        result.fold(
            onSuccess = { loadRegistrations(eventId, force = true) },
            onFailure = { error -> _listState.update { it.copy(error = error.message ?: "Could not update registration review.") } },
        )
    }
}
