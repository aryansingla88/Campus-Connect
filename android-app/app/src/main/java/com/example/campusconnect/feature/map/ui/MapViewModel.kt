package com.example.campusconnect.feature.map.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.os.SystemClock
import android.util.Log
import com.example.campusconnect.core.location.LocationTrackingCoordinator
import com.example.campusconnect.core.location.LocationQualityValidator
import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.TrackingModePreferences
import com.example.campusconnect.core.location.android.FusedLocationSource
import com.example.campusconnect.core.location.model.LocationFix
import com.example.campusconnect.core.location.model.LocationStatus
import com.example.campusconnect.core.location.model.displayLocation
import com.example.campusconnect.core.session.SessionManager
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.map.data.repo.ApiMapRepo
import com.example.campusconnect.feature.map.data.repo.MapRepo
import com.example.campusconnect.feature.map.data.repo.PresenceUploadPolicy
import com.example.campusconnect.feature.map.data.PoiRepositoryProvider
import com.example.campusconnect.feature.map.mapengine.MapCalibration
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_HEIGHT
import com.example.campusconnect.feature.map.mapengine.viewport.MAP_IMAGE_WIDTH
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.*
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.random.Random
import java.util.Locale

class MapViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val courseRepository =
        CourseRepositoryProvider.getRepository(
            application.applicationContext
        )

    private val repository: MapRepo =
        ApiMapRepo(
            courseRepository = courseRepository,
            poiRepository = PoiRepositoryProvider.get(application.applicationContext)
        )
    private val coordinateConverter = MapCalibration.converter
    private val locationCoordinator = LocationTrackingCoordinator(
        source = FusedLocationSource(application.applicationContext),
        onFixEvaluated = { mode, fix, rejection ->
            val ageMillis = ((SystemClock.elapsedRealtimeNanos() - fix.elapsedRealtimeNanos)
                .coerceAtLeast(0L) / 1_000_000L)
            Log.d(
                "CampusLocation",
                "coordinator fix mode=$mode decision=${rejection ?: "accepted"} cached=${fix.isCached} ageMs=$ageMillis accuracyM=${fix.accuracyMeters} elapsedNs=${fix.elapsedRealtimeNanos}"
            )
        }
    )
    val trackingMode = TrackingModePreferences.mode
    private var markerLoadJob: Job? = null
    private var markerLoadRequestId = 0L
    private var detailLoadJob: Job? = null
    private var categoryLoadJob: Job? = null
    private var categoryLoadRequestId = 0L
    private val markerCache = mutableMapOf<MarkerType, List<MapMarker>>()
    private val loadedMarkerTypes = mutableSetOf<MarkerType>()
    private val uploadFixes = Channel<LocationFix>(capacity = Channel.CONFLATED)
    private var mapSessionActive = false
    private var presenceSessionStarted = false
    private var presencePollingJob: Job? = null
    private var presenceUploadJob: Job? = null
    private var lastUploadedFix: LocationFix? = null
    private var lastUploadElapsedRealtime: Long? = null
    private var latestValidFix: LocationFix? = null
    private var latestDisplayedFix: LocationFix? = null
    private var currentUserId: Int? = null
    private var currentUsername: String? = null
    private var identitySessionVersion: Long = Long.MIN_VALUE

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init {
        loadMarkersForMode(_uiState.value.mode)
        loadFilterOptions()
        viewModelScope.launch {
            locationCoordinator.status.collect { status ->
                when (status) {
                    is LocationStatus.Available -> {
                        latestValidFix = status.fix
                        latestDisplayedFix = status.fix
                    }
                    is LocationStatus.Approximate -> latestDisplayedFix = status.fix
                    else -> Unit
                }
                val displayedLocation = status.displayLocation(latestDisplayedFix)
                val marker = displayedLocation.fix?.let {
                    createLocalLocationMarker(it, displayedLocation.isStale)
                }
                Log.d(
                    "CampusLocation",
                    "map marker status=${locationStatusName(status)} present=${marker != null} stale=${marker?.isStale ?: false}"
                )
                _uiState.value = _uiState.value.copy(
                    locationStatus = status,
                    localLocationMarker = marker
                )
                if (status is LocationStatus.Available && mapSessionActive) {
                    uploadFixes.trySend(status.fix)
                }
            }
        }
        viewModelScope.launch {
            SessionManager.sessionVersion.collect {
                presenceUploadJob?.cancel()
                while (uploadFixes.tryReceive().isSuccess) Unit
                presencePollingJob?.cancel()
                clearAccountScopedRemoteUsers()
                currentUserId = null
                currentUsername = null
                identitySessionVersion = Long.MIN_VALUE
                lastUploadedFix = null
                lastUploadElapsedRealtime = null
                if (mapSessionActive) {
                    startPresenceUploadWorker()
                    startPresencePolling(refreshImmediately = true)
                }
            }
        }
    }

    fun startPresenceSession() {
        if (mapSessionActive) return
        mapSessionActive = true
        startPresenceUploadWorker()
        startPresencePolling(refreshImmediately = presenceSessionStarted)
    }

    fun stopPresenceSession() {
        if (!mapSessionActive) return
        Log.d("CampusLocation", "tracking stop requested")
        mapSessionActive = false
        presencePollingJob?.cancel()
        presencePollingJob = null
        presenceUploadJob?.cancel()
        presenceUploadJob = null
        while (uploadFixes.tryReceive().isSuccess) Unit
        locationCoordinator.stop()
    }

    fun startLocationTracking(mode: TrackingMode) {
        // Presence and tracking are started by separate Compose effects; do not drop a
        // tracking request if this effect happens to run first.
        Log.d("CampusLocation", "tracking start/reconfigure requested mode=$mode")
        locationCoordinator.start(viewModelScope, mode)
    }

    fun reportLocationPermissionDenied() {
        Log.d("CampusLocation", "tracking stopped: location permission unavailable")
        locationCoordinator.stop()
        locationCoordinator.reportPermissionDenied()
    }

    private fun locationStatusName(status: LocationStatus): String = when (status) {
        LocationStatus.Unavailable -> "unavailable"
        LocationStatus.PermissionDenied -> "permission_denied"
        LocationStatus.PreciseLocationPermissionRequired -> "precise_permission_required"
        LocationStatus.ServicesDisabled -> "services_disabled"
        LocationStatus.Acquiring -> "acquiring"
        is LocationStatus.Available -> "available"
        is LocationStatus.Approximate -> "approximate"
        is LocationStatus.Stale -> "stale"
        is LocationStatus.InsufficientAccuracy -> "insufficient_accuracy"
        is LocationStatus.Error -> "error"
    }

    private fun createLocalLocationMarker(fix: LocationFix, isStale: Boolean): LocalUserMarkerData? =
        runCatching {
            val point = coordinateConverter.latLngToPoint(fix.latitude, fix.longitude)
            if (!point.x.isFinite() || !point.y.isFinite() ||
                point.x !in 0f..MAP_IMAGE_WIDTH || point.y !in 0f..MAP_IMAGE_HEIGHT
            ) return null
            LocalUserMarkerData(
                latitude = fix.latitude,
                longitude = fix.longitude,
                isStale = isStale,
                mapX = point.x,
                mapY = point.y
            )
        }.getOrNull()

    private fun startPresencePolling(refreshImmediately: Boolean) {
        if (presencePollingJob?.isActive == true) return
        presencePollingJob = viewModelScope.launch {
            if (refreshImmediately) {
                refreshVisibleUsers()
            } else {
                markerLoadJob?.join()
                if (MarkerType.USER !in loadedMarkerTypes) refreshVisibleUsers()
            }
            presenceSessionStarted = true

            var waitMillis = PRESENCE_POLL_INTERVAL_MILLIS
            while (currentCoroutineContext().isActive && mapSessionActive) {
                delay(withJitter(waitMillis))
                if (!mapSessionActive) break
                val success = refreshVisibleUsers()
                waitMillis = if (success) {
                    PRESENCE_POLL_INTERVAL_MILLIS
                } else {
                    (waitMillis * 2).coerceAtMost(PRESENCE_MAX_BACKOFF_MILLIS)
                }
            }
        }
    }

    private suspend fun refreshVisibleUsers(): Boolean {
        val sessionVersion = SessionManager.sessionVersion.value
        if (SessionManager.getToken().isNullOrBlank()) return false
        if (!resolveCurrentUserIdentity(sessionVersion)) {
            Log.w("CampusMapMarkers", "Cannot resolve signed-in identity; remote user markers suppressed")
            markerCache[MarkerType.USER] = emptyList()
            loadedMarkerTypes += MarkerType.USER
            return false
        }
        val result = repository.getMarkers(MarkerType.USER)
        if (!mapSessionActive || SessionManager.sessionVersion.value != sessionVersion) return false
        val fetched = result.getOrNull() ?: return false
        // The local blue dot is the only marker for the signed-in account. Some backend
        // responses include the caller in /map/presence, so remove that account from remote users.
        val remoteUsers = filterRemoteUserMarkers(fetched, currentUserId, currentUsername)
        markerCache[MarkerType.USER] = remoteUsers.map(::positionMarker)
        loadedMarkerTypes += MarkerType.USER

        val current = _uiState.value
        val modeMarkers = markerTypesForMode(current.mode).flatMap { markerCache[it].orEmpty() }
        val selectionStillExists = current.selectedMarkerId?.let { selectedId ->
            modeMarkers.any { it.id == selectedId }
        } ?: false
        if (current.mode == MapMode.HOME) {
            if (!selectionStillExists && current.selectedMarkerId != null) detailLoadJob?.cancel()
            updateState(
                markers = modeMarkers,
                mode = current.mode,
                selectedMarkerId = current.selectedMarkerId.takeIf { selectionStillExists },
                activeUserFilterId = current.activeUserFilterId,
                isPoiFilterActive = current.isPoiFilterActive,
                activePoiCategory = current.activePoiCategory,
                activeEventCategoryId = current.activeEventCategoryId,
                selectedMarkerOverride = null,
                selectedUserProfileOverride = current.selectedUserProfile.takeIf { selectionStillExists },
                selectedPoiInfoOverride = current.selectedPoiInfo.takeIf { selectionStillExists },
                selectedEventInfoOverride = current.selectedEventInfo.takeIf { selectionStillExists },
                isDetailLoading = current.isDetailLoading && selectionStillExists,
                detailErrorMessage = current.detailErrorMessage.takeIf { selectionStillExists },
                errorMessage = null
            )
        }
        return true
    }

    private suspend fun resolveCurrentUserIdentity(expectedSessionVersion: Long): Boolean {
        if (identitySessionVersion == expectedSessionVersion) {
            return currentUserId != null || !currentUsername.isNullOrBlank()
        }
        if (SessionManager.sessionVersion.value != expectedSessionVersion) return false

        currentUserId = SessionManager.getAuthenticatedUserId()
        if (currentUserId != null) {
            identitySessionVersion = expectedSessionVersion
            return true
        }

        val user = runCatching { RetrofitClient.authApi.getCurrentUser() }
            .getOrNull()
            ?.takeIf { it.isSuccessful && SessionManager.sessionVersion.value == expectedSessionVersion }
            ?.body()?.data
        if (user != null && SessionManager.sessionVersion.value == expectedSessionVersion) {
            currentUserId = user.id
            currentUsername = user.username
            identitySessionVersion = expectedSessionVersion
            return true
        }
        return false
    }

    private fun startPresenceUploadWorker() {
        if (presenceUploadJob?.isActive == true) return
        presenceUploadJob = viewModelScope.launch {
            while (mapSessionActive && currentCoroutineContext().isActive) {
                val queuedFix = withTimeoutOrNull(UPLOAD_HEARTBEAT_CHECK_MILLIS) {
                    uploadFixes.receive()
                }
                val fix = queuedFix ?: latestValidFix ?: continue
                val token = SessionManager.getToken()?.takeIf { it.isNotBlank() } ?: continue
                val mode = trackingMode.value
                val now = SystemClock.elapsedRealtime()
                if (LocationQualityValidator.rejectionReason(
                        fix = fix,
                        mode = mode,
                        nowElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos(),
                        nowTimeMillis = System.currentTimeMillis(),
                        previous = null
                    ) != null
                ) continue
                if (!PresenceUploadPolicy.shouldUpload(fix, lastUploadedFix, lastUploadElapsedRealtime, now, mode)) {
                    continue
                }

                var uploaded = false
                for (attempt in 0 until MAX_UPLOAD_ATTEMPTS) {
                    if (!mapSessionActive || SessionManager.getToken() != token) break
                    val result = repository.updateMyPresence(
                        latitude = fix.latitude,
                        longitude = fix.longitude,
                        bearerToken = token
                    )
                    if (result.isSuccess && SessionManager.getToken() == token) {
                        lastUploadedFix = fix
                        lastUploadElapsedRealtime = SystemClock.elapsedRealtime()
                        uploaded = true
                        break
                    }
                    if (attempt < MAX_UPLOAD_ATTEMPTS - 1) {
                        delay(withJitter((UPLOAD_RETRY_BASE_MILLIS shl attempt).coerceAtMost(UPLOAD_RETRY_MAX_MILLIS)))
                    }
                }
                // A conflated channel retains only the newest fix while this request/retry completes.
                if (!uploaded) continue
            }
        }
    }

    private fun clearAccountScopedRemoteUsers() {
        markerCache.remove(MarkerType.USER)
        loadedMarkerTypes.remove(MarkerType.USER)
        val current = _uiState.value
        if (current.mode != MapMode.HOME) return
        val retainedMarkers = current.markers.filterNot { it is UserMarkerData }
        val selectedUser = current.selectedMarker is UserMarkerData
        updateState(
            markers = retainedMarkers,
            mode = current.mode,
            selectedMarkerId = current.selectedMarkerId.takeUnless { selectedUser },
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = current.selectedPoiInfo.takeUnless { selectedUser },
            selectedEventInfoOverride = current.selectedEventInfo.takeUnless { selectedUser },
            isDetailLoading = current.isDetailLoading && !selectedUser,
            detailErrorMessage = current.detailErrorMessage.takeUnless { selectedUser }
        )
    }

    private fun withJitter(delayMillis: Long): Long =
        (delayMillis * Random.nextDouble(0.8, 1.2)).toLong().coerceAtLeast(1L)

    override fun onCleared() {
        stopPresenceSession()
        locationCoordinator.stop()
        super.onCleared()
    }

    private companion object {
        const val LOCAL_LOCATION_MARKER_ID = "LOCAL_DEVICE"
        const val PRESENCE_POLL_INTERVAL_MILLIS = 25_000L
        const val PRESENCE_MAX_BACKOFF_MILLIS = 120_000L
        const val MAX_UPLOAD_ATTEMPTS = 5
        const val UPLOAD_RETRY_BASE_MILLIS = 1_000L
        const val UPLOAD_RETRY_MAX_MILLIS = 30_000L
        const val UPLOAD_HEARTBEAT_CHECK_MILLIS = 30_000L
    }

    private fun loadFilterOptions() {
        viewModelScope.launch {
            val events = repository.getEventCategories().getOrDefault(emptyList())
            val pois = repository.getPoiCategories().getOrDefault(emptyList())
            _uiState.value = _uiState.value.copy(eventCategories = events, poiCategories = pois)
        }
    }

    /** All mode changes enter through this transition and clear incompatible UI state. */
    fun selectMode(mode: MapMode) {
        if (_uiState.value.mode == mode) {
            if (_uiState.value.errorMessage != null) loadMarkersForMode(mode)
            return
        }
        loadMarkersForMode(mode)
    }

    private fun loadMarkersForMode(mode: MapMode) {
        markerLoadJob?.cancel()
        categoryLoadJob?.cancel()
        categoryLoadRequestId++
        val requestId = ++markerLoadRequestId
        detailLoadJob?.cancel()
        detailLoadJob = null

        val markerTypes = markerTypesForMode(mode)
        val missingTypes = markerTypes.filterNot { it in loadedMarkerTypes }
        val filterContext = filterContextForMode(mode)
        val accountSessionVersion = SessionManager.sessionVersion.value

        _uiState.value = _uiState.value.copy(
            mode = mode,
            markers = emptyList(),
            renderMarkers = emptyList(),
            selectedMarkerId = null,
            selectedMarker = null,
            selectedUserProfile = null,
            selectedPoiInfo = null,
            selectedEventInfo = null,
            activeUserFilterId = null,
            isPoiFilterActive = false,
            activePoiCategory = null,
            activeEventCategoryId = null,
            filterContext = filterContext,
            isLoading = missingTypes.isNotEmpty(),
            errorMessage = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )

        if (missingTypes.isEmpty()) {
            publishModeMarkers(mode, markerTypes)
            return
        }

        markerLoadJob = viewModelScope.launch {
            val results = coroutineScope {
                missingTypes.map { type ->
                    async {
                        val identityResolved = type != MarkerType.USER ||
                            resolveCurrentUserIdentity(accountSessionVersion)
                        val result = if (identityResolved) {
                            repository.getMarkers(type)
                        } else {
                            Result.failure<List<MapMarker>>(
                                IllegalStateException("Unable to verify signed-in user for map markers")
                            )
                        }
                        type to result
                    }
                }.awaitAll()
                }
            if (requestId != markerLoadRequestId) return@launch

            var errorMessage: String? = null
            results.forEach { (type, result) ->
                if (type == MarkerType.USER &&
                    SessionManager.sessionVersion.value != accountSessionVersion
                ) return@forEach
                result.onSuccess { markers ->
                    val visibleMarkers = if (type == MarkerType.USER) {
                        filterRemoteUserMarkers(markers, currentUserId, currentUsername)
                    } else {
                        markers
                    }
                    markerCache[type] = visibleMarkers.map(::positionMarker)
                    loadedMarkerTypes += type
                }.onFailure { error ->
                    if (errorMessage == null) {
                        errorMessage = error.message ?: "Unable to load map markers"
                    }
                }
            }

            publishModeMarkers(mode, markerTypes, errorMessage)
        }
    }

    private fun publishModeMarkers(
        mode: MapMode,
        markerTypes: List<MarkerType>,
        errorMessage: String? = null
    ) {
        val modeMarkers = markerTypes.flatMap { markerCache[it].orEmpty() }
        updateState(
            markers = modeMarkers,
            mode = mode,
            selectedMarkerId = null,
            activeUserFilterId = null,
            isPoiFilterActive = false,
            activePoiCategory = null,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isLoading = false,
            errorMessage = errorMessage,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    private fun positionMarker(marker: MapMarker): MapMarker {
        val point = coordinateConverter.latLngToPoint(marker.latitude, marker.longitude)
        return marker.withMapPosition(point.x, point.y)
    }

    private fun markerTypesForMode(mode: MapMode): List<MarkerType> = when (mode) {
        MapMode.HOME -> listOf(MarkerType.USER, MarkerType.POI)
        MapMode.EVENT -> listOf(MarkerType.EVENT)
        MapMode.SHOP -> listOf(MarkerType.SHOP)
        MapMode.POSTER -> emptyList()
    }

    private fun filterContextForMode(mode: MapMode): MapFilterContext = when (mode) {
        MapMode.HOME -> MapFilterContext.HOME_MARKERS
        MapMode.EVENT -> MapFilterContext.EVENT_CATEGORIES
        MapMode.SHOP, MapMode.POSTER -> MapFilterContext.NONE
    }

    fun selectMarker(markerId: String) {
        if (markerId == LOCAL_LOCATION_MARKER_ID) return
        val selectedMarker = _uiState.value.renderMarkers.firstOrNull { it.id == markerId }
            ?: run {
                return
            }

        detailLoadJob?.cancel()

        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = markerId,
            selectedMarkerOverride = selectedMarker,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = true,
            detailErrorMessage = null
        )

        detailLoadJob = loadSelectedMarkerDetails(selectedMarker)
    }

    private fun loadSelectedMarkerDetails(marker: MapMarker): Job =
        viewModelScope.launch {
            when (marker) {
                is UserMarkerData -> repository.getUserProfile(marker.sourceId)
                    .onSuccess { profile ->
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            updateSelectedDetails(profile, null, null)
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
                    }

                is PoiMarkerData -> repository.getPoiInfo(marker.sourceId, marker.label)
                    .onSuccess { poi ->
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            updateSelectedDetails(null, poi, null)
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
                    }

                is EventMarkerData -> {
                    repository.getEventInfo(marker.sourceId)
                        .onSuccess { event ->
                            if (_uiState.value.selectedMarker?.id == marker.id) {
                                if (event.id == marker.sourceId) {
                                    updateSelectedDetails(null, null, event)
                                } else {
                                    updateDetailError(
                                        IllegalStateException(
                                            "Event preview ID mismatch: requested ${marker.sourceId}, received ${event.id}"
                                        )
                                    )
                                }
                            }
                        }
                        .onFailure { error ->
                            if (_uiState.value.selectedMarker?.id == marker.id) {
                                updateDetailError(error)
                            }
                        }
                }

                is ShopMarkerData -> repository.getShopInfo(marker.sourceId)
                    .onSuccess {
                        if (_uiState.value.selectedMarker?.id == marker.id) {
                            _uiState.value = _uiState.value.copy(
                                isDetailLoading = false,
                                detailErrorMessage = null,
                                selectedUserProfile = null,
                                selectedPoiInfo = null,
                                selectedEventInfo = null
                            )
                        }
                    }
                    .onFailure { error ->
                        if (_uiState.value.selectedMarker?.id == marker.id) updateDetailError(error)
                    }

                is LocalUserMarkerData -> Unit
            }
        }

    fun clearSelection() {
        updateState(
            markers = _uiState.value.markers,
            selectedMarkerId = null,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    fun setUserFilter(filterId: String?) {
        val state = _uiState.value
        if (state.mode != MapMode.HOME) return

        detailLoadJob?.cancel()
        updateState(
            markers = state.markers,
            mode = state.mode,
            selectedMarkerId = null,
            activeUserFilterId = filterId,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    fun setPoiCategory(category: String?) {
        val state = _uiState.value
        if (state.mode != MapMode.HOME) return
        detailLoadJob?.cancel()
        updateState(
            markers = state.markers,
            isPoiFilterActive = true,
            activePoiCategory = category,
            selectedMarkerId = null,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    fun clearPoiFilter() {
        val state = _uiState.value
        if (state.mode != MapMode.HOME) return
        detailLoadJob?.cancel()
        updateState(
            markers = state.markers,
            isPoiFilterActive = false,
            activePoiCategory = null,
            selectedMarkerId = null,
            selectedMarkerOverride = null,
            selectedUserProfileOverride = null,
            selectedPoiInfoOverride = null,
            selectedEventInfoOverride = null,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    fun setEventCategory(categoryId: Int?) {
        if (_uiState.value.mode != MapMode.EVENT) return
        categoryLoadJob?.cancel()
        val requestId = ++categoryLoadRequestId
        detailLoadJob?.cancel()
        _uiState.value = _uiState.value.copy(
            activeEventCategoryId = categoryId,
            activeUserFilterId = null,
            isPoiFilterActive = false,
            activePoiCategory = null,
            markers = emptyList(),
            renderMarkers = emptyList(),
            selectedMarkerId = null,
            selectedMarker = null,
            selectedEventInfo = null,
            isLoading = true,
            isDetailLoading = false,
            detailErrorMessage = null
        )
        categoryLoadJob = viewModelScope.launch {
            repository.getMarkers(MarkerType.EVENT, eventCategoryId = categoryId)
                .onSuccess { markers ->
                    if (requestId == categoryLoadRequestId && _uiState.value.mode == MapMode.EVENT) {
                        updateState(
                            markers = markers.map(::positionMarker),
                            activeEventCategoryId = categoryId,
                            activeUserFilterId = null,
                            isPoiFilterActive = false,
                            activePoiCategory = null,
                            selectedMarkerId = null,
                            selectedMarkerOverride = null,
                            selectedUserProfileOverride = null,
                            selectedPoiInfoOverride = null,
                            selectedEventInfoOverride = null,
                            isLoading = false,
                            errorMessage = null,
                            isDetailLoading = false,
                            detailErrorMessage = null
                        )
                    }
                }
                .onFailure { error ->
                    if (requestId == categoryLoadRequestId) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unable to load events"
                        )
                    }
                }
        }
    }

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            repository.sendConnectionRequest(userId)
        }
    }

    fun enableEventReminder(eventId: Int) {
        viewModelScope.launch {
            repository.enableEventReminder(eventId)
        }
    }

    fun disableEventReminder(eventId: Int) {
        viewModelScope.launch {
            repository.disableEventReminder(eventId)
        }
    }

    private fun updateSelectedDetails(
        selectedUserProfile: MapUserProfile?,
        selectedPoiInfo: MapPoiInfo?,
        selectedEventInfo: MapEventInfo?
    ) {
        _uiState.value = _uiState.value.copy(
            selectedUserProfile = selectedUserProfile,
            selectedPoiInfo = selectedPoiInfo,
            selectedEventInfo = selectedEventInfo,
            isDetailLoading = false,
            detailErrorMessage = null
        )
    }

    private fun updateDetailError(
        error: Throwable
    ) {
        _uiState.value = _uiState.value.copy(
            isDetailLoading = false,
            detailErrorMessage = error.message ?: "Unable to load marker details"
        )
    }

    private fun updateState(
        markers: List<MapMarker> = _uiState.value.markers,
        mode: MapMode = _uiState.value.mode,
        selectedMarkerId: String? = _uiState.value.selectedMarkerId,
        activeUserFilterId: String? = _uiState.value.activeUserFilterId,
        isPoiFilterActive: Boolean = _uiState.value.isPoiFilterActive,
        activePoiCategory: String? = _uiState.value.activePoiCategory,
        activeEventCategoryId: Int? = _uiState.value.activeEventCategoryId,

        selectedMarkerOverride: MapMarker? = _uiState.value.selectedMarker,
        selectedUserProfileOverride: MapUserProfile? = _uiState.value.selectedUserProfile,
        selectedPoiInfoOverride: MapPoiInfo? = _uiState.value.selectedPoiInfo,
        selectedEventInfoOverride: MapEventInfo? = _uiState.value.selectedEventInfo,

        isLoading: Boolean = _uiState.value.isLoading,
        errorMessage: String? = _uiState.value.errorMessage,

        isDetailLoading: Boolean = _uiState.value.isDetailLoading,
        detailErrorMessage: String? = _uiState.value.detailErrorMessage
    ) {
        val modeMarkerTypes = markerTypesForMode(mode)
        val modeMarkers = markers.filter { it.type in modeMarkerTypes }
        val visibleMarkers = if (mode == MapMode.HOME) {
            modeMarkers.filter { marker ->
                when (marker) {
                    // The current Users option means all user markers; future
                    // user-specific predicates stay scoped to this marker type.
                    is UserMarkerData -> true
                    is PoiMarkerData -> !isPoiFilterActive || activePoiCategory == null ||
                            marker.category.equals(activePoiCategory, ignoreCase = true)
                    else -> true
                }
            }
        } else {
            modeMarkers
        }

        val renderMarkers = visibleMarkers.map { marker ->
            marker.withSelection(marker.id == selectedMarkerId)
        }

        val selectedMarker = selectedMarkerOverride
            ?: renderMarkers.firstOrNull { marker ->
                marker.id == selectedMarkerId
            }

        _uiState.value = _uiState.value.copy(
            mode = mode,
            markers = markers,
            renderMarkers = renderMarkers,
            selectedMarkerId = selectedMarkerId,
            selectedMarker = selectedMarker,
            selectedUserProfile = selectedUserProfileOverride,
            selectedPoiInfo = selectedPoiInfoOverride,
            selectedEventInfo = selectedEventInfoOverride,
            activeUserFilterId = activeUserFilterId.takeIf { mode == MapMode.HOME },
            isPoiFilterActive = isPoiFilterActive && mode == MapMode.HOME,
            activePoiCategory = activePoiCategory.takeIf { mode == MapMode.HOME },
            activeEventCategoryId = activeEventCategoryId.takeIf { mode == MapMode.EVENT },
            filterContext = filterContextForMode(mode),
            isLoading = isLoading,
            errorMessage = errorMessage,
            isDetailLoading = isDetailLoading,
            detailErrorMessage = detailErrorMessage
        )
    }
}

internal fun filterRemoteUserMarkers(
    markers: List<MapMarker>,
    authenticatedUserId: Int?,
    authenticatedUsername: String?
): List<MapMarker> {
    // Fail closed when identity cannot be established: never show a possible self profile
    // marker beside the device-local marker. Other users return when identity resolves.
    if (authenticatedUserId == null && authenticatedUsername.isNullOrBlank()) return emptyList()
    return markers.filterNot { marker ->
        marker is UserMarkerData && if (authenticatedUserId != null) {
            marker.sourceId == authenticatedUserId
        } else {
            marker.username.equals(authenticatedUsername, ignoreCase = true)
        }
    }
}
