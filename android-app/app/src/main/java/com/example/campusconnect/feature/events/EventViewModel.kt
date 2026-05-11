package com.example.campusconnect.feature.events

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import com.example.campusconnect.model.Event

class EventViewModel : ViewModel() {

    private val fakeService = FakeEventService()
    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    init {
        _events.value = fakeService.getEvents()
    }

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }


    fun setLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(
            selectedLocation = Pair(lat, lng)
        )
    }

    fun updateDate(value: String) {
        _uiState.value = _uiState.value.copy(date = value)
    }

    fun updateVenue(value: String) {
        _uiState.value = _uiState.value.copy(venue = value)
    }

    fun updateStartTime(value: String) {
        _uiState.value = _uiState.value.copy(startTime = value)
    }

    fun setScreenLocation(xRatio: Float, yRatio: Float) {
        _uiState.value = _uiState.value.copy(
            selectedRatio = Pair(xRatio, yRatio)
        )
    }

    fun createEvent(createdBy: Int) {

        val state = _uiState.value

        // 🔹 VALIDATION
        if (state.title.isBlank()) {
            _uiState.value = state.copy(error = "Title required")
            return
        }

        if (state.date.isBlank()) {
            _uiState.value = state.copy(error = "Date required")
            return
        }

        if (state.startTime.isBlank()) {
            _uiState.value = state.copy(error = "Start time required")
            return
        }

        // 🔹 CREATE EVENT
        val event = Event(
            id = 0,

            // BASIC
            title = state.title,
            description = state.description,

            // LOCATION
            latitude = 0.0,
            longitude = 0.0,

            xRatio = state.selectedRatio?.first ?: 0.5f,
            yRatio = state.selectedRatio?.second ?: 0.5f,

            // TIME
            date = state.date,
            startTime = state.startTime,
            endTime = if (state.endTime.isBlank()) null else state.endTime,

            // ORGANIZATION
            createdBy = createdBy,
            clubName = state.clubName,

            // MEDIA
            isPoster = state.isPoster,
            posterUrl = if (state.posterUrl.isBlank()) null else state.posterUrl,

            // CLASSIFICATION
            category = state.category,

            // VISIBILITY
            visibilityType = state.visibilityType,
            visibilityValue = state.visibilityValue,

            // REGISTRATION
            registrationRequired = state.registrationRequired,
            registrationLink = state.registrationLink,
            inAppRegistration = state.inAppRegistration,

            // EXTRA
            venue = state.venue
        )

        // 🔹 SAVE
        val success = fakeService.createEvent(event)

        if (success) {
            _events.value = fakeService.getEvents()

            _uiState.value = state.copy(
                success = true
            )
        }

        println("CreateEvent called: $event")
    }
}