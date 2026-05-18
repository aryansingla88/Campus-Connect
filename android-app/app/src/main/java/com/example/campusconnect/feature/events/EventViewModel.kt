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

    // ── Preview sheet state ───────────────────────────────────────────────────

    /** Index into [events] of the currently highlighted / previewed event.
     *  -1 means no marker is selected (sheet is hidden). */
    private val _activeEventIndex = MutableStateFlow(-1)
    val activeEventIndex: StateFlow<Int> = _activeEventIndex

    /** True while the preview sheet is visible. */
    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    init {
        _events.value = fakeService.getEvents()
    }

    // ── Marker / preview interactions ─────────────────────────────────────────

    /** Called when the user taps a marker on the map. */
    fun onMarkerTapped(index: Int) {
        _activeEventIndex.value = index
        _showPreview.value = true
    }

    /** Called when the pager page changes (user swipes the preview sheet). */
    fun onPreviewPageChanged(index: Int) {
        _activeEventIndex.value = index
    }

    /** Called when the preview sheet × button is pressed. */
    fun closePreview() {
        _showPreview.value = false
        _activeEventIndex.value = -1
    }

    // ── Form fields ───────────────────────────────────────────────────────────

    fun updateTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    fun updateDescription(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun setLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(selectedLocation = Pair(lat, lng))
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

    fun updateEndTime(value: String) {
        _uiState.value = _uiState.value.copy(endTime = value)
    }

    fun setScreenLocation(xRatio: Float, yRatio: Float) {
        _uiState.value = _uiState.value.copy(selectedRatio = Pair(xRatio, yRatio))
    }

    fun updatePosterEnabled(value: Boolean) {
        _uiState.value = _uiState.value.copy(isPoster = value)
    }

    fun updatePosterUrl(value: String) {
        _uiState.value = _uiState.value.copy(posterUrl = value)
    }

    fun updateClubName(value: String) {
        _uiState.value = _uiState.value.copy(clubName = value)
    }

    fun updateCategory(value: String) {
        _uiState.value = _uiState.value.copy(category = value)
    }

    fun updateVisibilityType(value: String) {
        _uiState.value = _uiState.value.copy(visibilityType = value)
    }

    fun updateVisibilityValue(value: String) {
        _uiState.value = _uiState.value.copy(visibilityValue = value)
    }

    // value is one of: "In-App" | "Link" | "No"
    fun updateRegistrationType(value: String) {
        _uiState.value = _uiState.value.copy(
            registrationType = value,
            registrationLink = if (value != "Link") "" else _uiState.value.registrationLink
        )
    }

    fun updateRegistrationLink(value: String) {
        _uiState.value = _uiState.value.copy(registrationLink = value)
    }

    fun updateEnableChat(value: Boolean) {
        _uiState.value = _uiState.value.copy(enableChat = value)
    }

    fun resetForm() {
        _uiState.value = EventUiState(success = false)
    }

    // ── Create event ──────────────────────────────────────────────────────────

    fun createEvent(createdBy: Int) {

        val state = _uiState.value

        // Validation
        if (state.title.isBlank())          { _uiState.value = state.copy(error = "Title required");            return }
        if (state.description.isBlank())    { _uiState.value = state.copy(error = "Description required");      return }
        if (state.date.isBlank())           { _uiState.value = state.copy(error = "Date required");             return }
        if (state.venue.isBlank())          { _uiState.value = state.copy(error = "Venue required");            return }
        if (state.startTime.isBlank())      { _uiState.value = state.copy(error = "Start time required");       return }
        if (state.clubName.isBlank())       { _uiState.value = state.copy(error = "Club name required");        return }
        if (state.category.isBlank())       { _uiState.value = state.copy(error = "Category required");         return }
        if (state.visibilityType.isBlank()) { _uiState.value = state.copy(error = "Visibility type required");  return }
        if (state.visibilityValue.isBlank()){ _uiState.value = state.copy(error = "Visibility value required"); return }
        if (state.registrationType == "Link" && state.registrationLink.isBlank()) {
            _uiState.value = state.copy(error = "Registration link required"); return
        }

        val registrationRequired = state.registrationType != "No"
        val inAppRegistration    = state.registrationType == "In-App"

        val event = Event(
            id                   = 0,
            title                = state.title,
            description          = state.description,
            latitude             = 0.0,
            longitude            = 0.0,
            xRatio               = state.selectedRatio?.first  ?: 0.5f,
            yRatio               = state.selectedRatio?.second ?: 0.5f,
            date                 = state.date,
            startTime            = state.startTime,
            endTime              = if (state.endTime.isBlank()) null else state.endTime,
            createdBy            = createdBy,
            clubName             = state.clubName,
            isPoster             = state.isPoster,
            posterUrl            = if (state.posterUrl.isBlank()) null else state.posterUrl,
            category             = state.category,
            visibilityType       = state.visibilityType,
            visibilityValue      = state.visibilityValue,
            registrationRequired = registrationRequired,
            registrationLink     = state.registrationLink,
            inAppRegistration    = inAppRegistration,
            venue                = state.venue,
            enableChat           = state.enableChat
        )

        val success = fakeService.createEvent(event)

        if (success) {
            _events.value = fakeService.getEvents()
            _uiState.value = EventUiState(success = true)
        }

        println("CreateEvent called: $event")
    }
}