package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.registrations.data.repo.RegistrationEventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class RegistrationEventUiState(
    val title: String? = null,
    val isPublished: Boolean = false,
    val loading: Boolean = true,
    val error: String? = null,
)

/** Event header data used by registration screens; loaded through the existing event endpoint. */
class RegistrationEventViewModel(
    private val repository: RegistrationEventRepository = RegistrationEventRepository(),
) : ViewModel() {
    private val _state = MutableStateFlow(RegistrationEventUiState())
    val state = _state.asStateFlow()
    private var requestedEventId: Int? = null

    fun load(eventId: Int) {
        if (requestedEventId == eventId) return
        requestedEventId = eventId
        viewModelScope.launch {
            _state.value = RegistrationEventUiState(loading = true)
            repository.getHeader(eventId).fold(
                onSuccess = { header ->
                    _state.value = RegistrationEventUiState(
                        title = header.title,
                        isPublished = header.isPublished,
                        loading = false,
                    )
                },
                onFailure = { error ->
                    _state.value = RegistrationEventUiState(
                        loading = false,
                        error = error.message ?: "Could not load event details.",
                    )
                },
            )
        }
    }
}
