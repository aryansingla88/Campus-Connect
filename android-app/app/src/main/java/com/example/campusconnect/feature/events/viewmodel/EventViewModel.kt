package com.example.campusconnect.feature.events.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.events.data.repo.EventRepository
import com.example.campusconnect.feature.events.data.repo.FakeEventRepository
import com.example.campusconnect.feature.events.model.Event
import com.example.campusconnect.feature.events.model.EventStatus
import com.example.campusconnect.feature.events.model.EventUiState
import com.example.campusconnect.feature.events.model.MedalAward
import com.example.campusconnect.feature.events.model.MedalType
import com.example.campusconnect.feature.events.model.ParticipantTeam
import com.example.campusconnect.feature.events.model.SoloParticipant
import com.example.campusconnect.feature.events.model.UserAccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class EventViewModel : ViewModel() {

    private val repository: EventRepository =
        FakeEventRepository()

    private val _uiState = MutableStateFlow(EventUiState())
    val uiState: StateFlow<EventUiState> = _uiState

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events

    private val _teams =
        MutableStateFlow<List<ParticipantTeam>>(emptyList())
    val teams: StateFlow<List<ParticipantTeam>> = _teams

    private val _soloParticipants =
        MutableStateFlow<List<SoloParticipant>>(emptyList())
    val soloParticipants: StateFlow<List<SoloParticipant>> = _soloParticipants

    private val _participantsCount =
        MutableStateFlow(0)
    val participantsCount: StateFlow<Int> = _participantsCount

    fun loadParticipants(eventId: Int) {
        viewModelScope.launch {

            _teams.value =
                repository.getTeams(eventId)
                    .getOrDefault(emptyList())

            _soloParticipants.value =
                repository.getSoloParticipants(eventId)
                    .getOrDefault(emptyList())

            _participantsCount.value =
                repository.getParticipantsCount(eventId)
                    .getOrDefault(0)
        }
    }

    private val _accessUsers =
        MutableStateFlow<List<UserAccess>>(emptyList())

    val accessUsers: StateFlow<List<UserAccess>> =
        _accessUsers

    private val _searchResults =
        MutableStateFlow<List<UserAccess>>(emptyList())

    val searchResults = _searchResults

    fun loadAccessUsers(eventId: Int) {
        viewModelScope.launch {

            _accessUsers.value =
                repository.getUsersWithAccess(eventId)
                    .getOrDefault(emptyList())
        }
    }

    fun searchAccessUsers(
        eventId: Int,
        query: String
    ) {

        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {

            _searchResults.value =
                repository.searchUsers(eventId, query)
                    .getOrDefault(emptyList())
        }
    }

    private val _medals =
        MutableStateFlow<List<MedalAward>>(emptyList())

    val medals: StateFlow<List<MedalAward>> =
        _medals

    fun loadMedals(eventId: Int) {
        viewModelScope.launch {

            val eventMedals =
                repository.getMedalsForEvent(eventId)
                    .getOrDefault(emptyList())

            _medals.value =
                _medals.value
                    .filter { it.eventId != eventId } +
                        eventMedals
        }
    }

    fun awardMedal(
        award: MedalAward
    ) {
        viewModelScope.launch {

            repository.awardMedal(award)

            loadMedals(award.eventId)
        }
    }

    fun removeMedal(
        eventId: Int,
        medalType: MedalType
    ) {
        viewModelScope.launch {

            repository.removeMedal(
                eventId,
                medalType
            )

            loadMedals(eventId)
        }
    }

    // ── Preview sheet state ───────────────────────────────────────────────────

    private val _activeEventIndex = MutableStateFlow(-1)
    val activeEventIndex: StateFlow<Int> = _activeEventIndex

    private val _showPreview = MutableStateFlow(false)
    val showPreview: StateFlow<Boolean> = _showPreview

    // ── Edit / delete state ───────────────────────────────────────────────────

    /** True while the dialog is open in edit mode (vs create mode). */
    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode

    /** The id of the event currently being edited. -1 when not editing. */
    private val _editingEventId = MutableStateFlow(-1)
    val editingEventId: StateFlow<Int> = _editingEventId

    /** Non-null while the delete confirmation dialog should be shown. */
    private val _pendingDeleteEvent = MutableStateFlow<Event?>(null)
    val pendingDeleteEvent: StateFlow<Event?> = _pendingDeleteEvent

    /** Pulses true after a successful delete so EventScreen can show a toast. */
    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess

    fun clearDeleteSuccess() {
        _deleteSuccess.value = false
    }

    init {
        viewModelScope.launch {
            _events.value =
                repository.getEvents()
                    .getOrDefault(emptyList())
        }
    }

    // ── Marker / preview ──────────────────────────────────────────────────────

    fun onMarkerTapped(index: Int) {
        _activeEventIndex.value = index
        _showPreview.value = true
    }

    fun onPreviewPageChanged(index: Int) {
        _activeEventIndex.value = index
    }

    fun closePreview() {
        _showPreview.value = false
        _activeEventIndex.value = -1
    }

    // ── Edit mode ─────────────────────────────────────────────────────────────

    /**
     * Load an existing event into the form state and switch to edit mode.
     * Called when the user taps Edit in the preview sheet.
     */
    fun loadEventForEdit(event: Event) {
        _editingEventId.value = event.id
        _isEditMode.value = true
        _uiState.value = EventUiState(
            title = event.title,
            description = event.description ?: "",
            isPoster = event.isPoster,
            posterUrl = event.posterUrl ?: "",
            selectedRatio = Pair(event.xRatio, event.yRatio),
            date = event.date,
            venue = event.venue,
            startTime = event.startTime,
            endTime = event.endTime ?: "",
            clubName = event.clubName,
            category = event.category,
            visibilityType = event.visibilityType,
            visibilityValue = event.visibilityValue,
            registrationType = when {
                event.inAppRegistration -> "In-App"
                event.registrationRequired -> "Link"
                else -> "No"
            },
            registrationLink = event.registrationLink,
            enableChat = event.enableChat
        )
    }

    /** Save edits to an existing event. */
    fun updateEvent() {
        val state = _uiState.value
        val id    = _editingEventId.value

        // Same validation as createEvent
        if (state.title.isBlank())           { _uiState.value = state.copy(error = "Title required");            return }
        if (state.description.isBlank())     { _uiState.value = state.copy(error = "Description required");      return }
        if (state.date.isBlank())            { _uiState.value = state.copy(error = "Date required");             return }
        if (state.venue.isBlank())           { _uiState.value = state.copy(error = "Venue required");            return }
        if (state.startTime.isBlank())       { _uiState.value = state.copy(error = "Start time required");       return }
        if (state.clubName.isBlank())        { _uiState.value = state.copy(error = "Club name required");        return }
        if (state.category.isBlank())        { _uiState.value = state.copy(error = "Category required");         return }
        if (state.visibilityType.isBlank())  { _uiState.value = state.copy(error = "Visibility type required");  return }
        if (state.visibilityValue.isBlank()) { _uiState.value = state.copy(error = "Visibility value required"); return }
        if (state.registrationType == "Link" && state.registrationLink.isBlank()) {
            _uiState.value = state.copy(error = "Registration link required"); return
        }

        val registrationRequired = state.registrationType != "No"
        val inAppRegistration    = state.registrationType == "In-App"

        val updated = Event(
            id = id,
            title = state.title,
            description = state.description,
            latitude = 0.0,
            longitude = 0.0,
            xRatio = state.selectedRatio?.first ?: 0.5f,
            yRatio = state.selectedRatio?.second ?: 0.5f,
            date = state.date,
            startTime = state.startTime,
            endTime = if (state.endTime.isBlank()) null else state.endTime,
            createdBy = 1,
            clubName = state.clubName,
            isPoster = state.isPoster,
            posterUrl = if (state.posterUrl.isBlank()) null else state.posterUrl,
            category = state.category,
            visibilityType = state.visibilityType,
            visibilityValue = state.visibilityValue,
            registrationRequired = registrationRequired,
            registrationLink = state.registrationLink,
            inAppRegistration = inAppRegistration,
            venue = state.venue,
            enableChat = state.enableChat,
            status = EventStatus.LIVE
        )

        viewModelScope.launch {

            repository.updateEvent(updated)

            _events.value =
                repository.getEvents()
                    .getOrDefault(emptyList())

            _uiState.value = EventUiState(success = true)

            _isEditMode.value = false
            _editingEventId.value = -1
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    /** Show the confirmation dialog for deleting [event]. */
    fun requestDelete(event: Event) {
        _pendingDeleteEvent.value = event
    }

    /** User confirmed deletion. */
    @RequiresApi(Build.VERSION_CODES.N)
    fun confirmDelete() {
        val event = _pendingDeleteEvent.value ?: return
        viewModelScope.launch {

            repository.deleteEvent(event.id)

            _events.value =
                repository.getEvents()
                    .getOrDefault(emptyList())
        }
        _pendingDeleteEvent.value = null
        _deleteSuccess.value = true   // trigger toast in EventScreen
        closePreview()
    }

    /** User cancelled deletion. */
    fun cancelDelete() {
        _pendingDeleteEvent.value = null
    }

    // ── Form field updaters ───────────────────────────────────────────────────

    fun updateTitle(value: String)       { _uiState.value = _uiState.value.copy(title = value) }
    fun updateDescription(value: String) { _uiState.value = _uiState.value.copy(description = value) }
    fun updateDate(value: String)        { _uiState.value = _uiState.value.copy(date = value) }
    fun updateVenue(value: String)       { _uiState.value = _uiState.value.copy(venue = value) }
    fun updateStartTime(value: String)   { _uiState.value = _uiState.value.copy(startTime = value) }
    fun updateEndTime(value: String)     { _uiState.value = _uiState.value.copy(endTime = value) }
    fun updatePosterEnabled(value: Boolean) { _uiState.value = _uiState.value.copy(isPoster = value) }
    fun updatePosterUrl(value: String)   { _uiState.value = _uiState.value.copy(posterUrl = value) }
    fun updateClubName(value: String)    { _uiState.value = _uiState.value.copy(clubName = value) }
    fun updateCategory(value: String)    { _uiState.value = _uiState.value.copy(category = value) }
    fun updateVisibilityType(value: String)  { _uiState.value = _uiState.value.copy(visibilityType = value) }
    fun updateVisibilityValue(value: String) { _uiState.value = _uiState.value.copy(visibilityValue = value) }
    fun updateEnableChat(value: Boolean) { _uiState.value = _uiState.value.copy(enableChat = value) }

    fun updateRegistrationType(value: String) {
        _uiState.value = _uiState.value.copy(
            registrationType = value,
            registrationLink = if (value != "Link") "" else _uiState.value.registrationLink
        )
    }
    fun updateRegistrationLink(value: String) { _uiState.value = _uiState.value.copy(registrationLink = value) }

    fun setLocation(lat: Double, lng: Double) {
        _uiState.value = _uiState.value.copy(selectedLocation = Pair(lat, lng))
    }
    fun setScreenLocation(xRatio: Float, yRatio: Float) {
        _uiState.value = _uiState.value.copy(selectedRatio = Pair(xRatio, yRatio))
    }

    fun resetForm() {
        _uiState.value = EventUiState(success = false)
        _isEditMode.value = false
        _editingEventId.value = -1
    }

    // ── Create event ──────────────────────────────────────────────────────────

    fun createEvent(createdBy: Int) {
        val state = _uiState.value

        if (state.title.isBlank())           { _uiState.value = state.copy(error = "Title required");            return }
        if (state.description.isBlank())     { _uiState.value = state.copy(error = "Description required");      return }
        if (state.date.isBlank())            { _uiState.value = state.copy(error = "Date required");             return }
        if (state.venue.isBlank())           { _uiState.value = state.copy(error = "Venue required");            return }
        if (state.startTime.isBlank())       { _uiState.value = state.copy(error = "Start time required");       return }
        if (state.clubName.isBlank())        { _uiState.value = state.copy(error = "Club name required");        return }
        if (state.category.isBlank())        { _uiState.value = state.copy(error = "Category required");         return }
        if (state.visibilityType.isBlank())  { _uiState.value = state.copy(error = "Visibility type required");  return }
        if (state.visibilityValue.isBlank()) { _uiState.value = state.copy(error = "Visibility value required"); return }
        if (state.registrationType == "Link" && state.registrationLink.isBlank()) {
            _uiState.value = state.copy(error = "Registration link required"); return
        }

        val registrationRequired = state.registrationType != "No"
        val inAppRegistration    = state.registrationType == "In-App"

        val event = Event(
            id = 0,
            title = state.title,
            description = state.description,
            latitude = 0.0,
            longitude = 0.0,
            xRatio = state.selectedRatio?.first ?: 0.5f,
            yRatio = state.selectedRatio?.second ?: 0.5f,
            date = state.date,
            startTime = state.startTime,
            endTime = if (state.endTime.isBlank()) null else state.endTime,
            createdBy = createdBy,
            clubName = state.clubName,
            isPoster = state.isPoster,
            posterUrl = if (state.posterUrl.isBlank()) null else state.posterUrl,
            category = state.category,
            visibilityType = state.visibilityType,
            visibilityValue = state.visibilityValue,
            registrationRequired = registrationRequired,
            registrationLink = state.registrationLink,
            inAppRegistration = inAppRegistration,
            venue = state.venue,
            enableChat = state.enableChat,
            status = EventStatus.LIVE
        )

        viewModelScope.launch {

            repository.createEvent(event)

            _events.value =
                repository.getEvents()
                    .getOrDefault(emptyList())

            _uiState.value = EventUiState(success = true)
        }

        println("CreateEvent called: $event")
    }
}