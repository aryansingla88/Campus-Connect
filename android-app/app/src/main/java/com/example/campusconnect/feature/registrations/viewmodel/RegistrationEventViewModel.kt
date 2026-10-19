package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.core.network.RetrofitClient
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
class RegistrationEventViewModel : ViewModel() {
    private val _state = MutableStateFlow(RegistrationEventUiState())
    val state = _state.asStateFlow()
    private var requestedEventId: Int? = null

    fun load(eventId: Int) {
        if (requestedEventId == eventId) return
        requestedEventId = eventId
        viewModelScope.launch {
            _state.value = RegistrationEventUiState(loading = true)
            try {
                val response = RetrofitClient.eventsApi.getEvent(eventId)
                val body = response.body()
                val event = body?.data
                if (response.isSuccessful && body?.success == true && event != null) {
                    val approvalStatus = event.approvalStatus.trim().uppercase()
                    _state.value = RegistrationEventUiState(
                        title = event.title,
                        isPublished = approvalStatus == "VERIFIED" || approvalStatus == "APPROVED",
                        loading = false,
                    )
                } else {
                    _state.value = RegistrationEventUiState(
                        loading = false,
                        error = body?.message?.takeIf(String::isNotBlank)
                            ?: "Could not load event details (HTTP ${response.code()}).",
                    )
                }
            } catch (error: Exception) {
                _state.value = RegistrationEventUiState(
                    loading = false,
                    error = error.message ?: "Could not load event details.",
                )
            }
        }
    }
}
