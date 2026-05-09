package com.example.campusconnect.feature.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.campusconnect.model.Event

class EventViewModel : ViewModel() {

    private val fakeService = FakeEventService()
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun updateTime(value: String) {
        _uiState.value = _uiState.value.copy(time = value)
    }

    fun setLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = Pair(lat, lng)
        )
    }

    fun createEvent(createdBy: Int) {

        val state = _uiState.value

        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title required")
            return
        }

        if (state.selectedLocation == null) {
            _uiState.value = state.copy(error = "Location required")
            return
        }

        val event = Event(
            id = 0,
            title = state.title,
            description = state.description,
            latitude = state.selectedLocation.first,
            longitude = state.selectedLocation.second,
            eventTime = state.time,
            createdBy = createdBy,
            xRatio = 0.5f,
            yRatio = 0.5f
        )

        val success = fakeService.createEvent(event)

        if (success) {
            _uiState.value = state.copy(
                success = true,
                error = null
            )
        }

        println("CreateEvent called")
    }
}