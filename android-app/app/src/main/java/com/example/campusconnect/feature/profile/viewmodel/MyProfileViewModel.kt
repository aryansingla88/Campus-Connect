package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import com.example.campusconnect.feature.profile.model.*

class MyProfileViewModel(
    application: Application
) : BaseProfileViewModel(application) {

    override var profile by mutableStateOf(PublicUserProfile())

    var stats by mutableStateOf(ProfileStats())
        private set

    // per‑panel loaded flags – in‑memory cache
    private var connectionsLoaded = false
    private var clubsLoaded = false
    private var honorsLoaded = false
    private var interestsLoaded = false

    var editableProfile by mutableStateOf(PublicUserProfile())
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

    val incomingRequests = mutableStateListOf<ConnectionRequest>()

    val sentInvites = mutableStateListOf<ConnectionRequest>()

    val searchResults = mutableStateListOf<Connection>()

    var selectedAvatarUri by mutableStateOf<Uri?>(null)
        private set

    init {
        loadMyData()
    }

    fun updateAvatar(uri: Uri) {
        selectedAvatarUri = uri
    }

    private fun loadMyData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                coroutineScope {
                    val profileRequest = async {
                        repository.getMyProfile()
                    }

                    val statsRequest = async {
                        repository.getMyStats()
                    }

                    profileRequest.await()
                        .onSuccess {
                            profile = it
                            editableProfile = it.copy()
                        }
                        .onFailure {
                            errorMessage = it.message
                        }

                    statsRequest.await()
                        .onSuccess {
                            stats = it
                        }
                }
            } finally {
                isLoading = false
            }
        }
    }

    override fun togglePanel(panel: StatPanel) {
        super.togglePanel(panel)
        if (activePanel == panel) {
            loadPanelData(panel)
        }
    }

    private fun loadPanelData(panel: StatPanel) {
        when (panel) {
            StatPanel.CONNECTIONS -> if (!connectionsLoaded) loadConnections()
            StatPanel.CLUBS -> if (!clubsLoaded) loadClubs()
            StatPanel.HONOR -> if (!honorsLoaded) loadHonors()
            StatPanel.INTERESTS -> if (!interestsLoaded) loadInterests()
        }
    }

    fun openManagePanel(panel: StatPanel) {
        activeManagePanel = panel
        when (panel) {
            StatPanel.CONNECTIONS -> loadConnectionRequests()
            StatPanel.INTERESTS -> loadAllInterestsIfNeeded()
            else -> {}
        }
    }

    fun closeManagePanel() {
        activeManagePanel = null
    }

    fun startEditing() {
        editableProfile = profile.copy()
        isEditMode = true
    }

    fun updateEditableProfile(updated: PublicUserProfile) {
        editableProfile = updated
    }

    fun cancelEditing() {
        editableProfile = profile.copy()
        selectedAvatarUri = null
        isEditMode = false
    }

    fun saveProfileChanges() {
        viewModelScope.launch {
            repository
                .updateProfile(
                    profile = editableProfile,
                    imageUri = selectedAvatarUri
                )
                .onSuccess {
                    profile = it
                    editableProfile = it.copy()
                    selectedAvatarUri = null
                    isEditMode = false
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            repository
                .sendConnectionRequest(userId)
                .onSuccess {

                    val connectionIndex = connections.indexOfFirst {
                        it.userId == userId
                    }

                    if (connectionIndex != -1) {
                        connections[connectionIndex] =
                            connections[connectionIndex].copy(
                                status = ConnectionStatus.PENDING
                            )
                    }

                    val searchIndex = searchResults.indexOfFirst {
                        it.userId == userId
                    }

                    if (searchIndex != -1) {
                        searchResults[searchIndex] =
                            searchResults[searchIndex].copy(
                                status = ConnectionStatus.PENDING
                            )
                    }
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun cancelConnectionRequest(userId: Int) {
        viewModelScope.launch {
            repository
                .removeConnectionRequest(userId)
                .onSuccess {
                    val connectionIndex =
                        connections.indexOfFirst { it.userId == userId }

                    if (connectionIndex != -1) {
                        connections[connectionIndex] =
                            connections[connectionIndex].copy(
                                status = ConnectionStatus.NOT_CONNECTED
                            )
                    }

                    val searchIndex =
                        searchResults.indexOfFirst { it.userId == userId }

                    if (searchIndex != -1) {
                        searchResults[searchIndex] =
                            searchResults[searchIndex].copy(
                                status = ConnectionStatus.NOT_CONNECTED
                            )
                    }
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }
    fun removeConnection(userId: Int) {
        viewModelScope.launch {
            repository
                .removeConnection(userId)
                .onSuccess {

                    val connectionIndex = connections.indexOfFirst {
                        it.userId == userId
                    }

                    if (connectionIndex != -1) {
                        connections[connectionIndex] =
                            connections[connectionIndex].copy(
                                status = ConnectionStatus.NOT_CONNECTED
                            )
                    }

                    val searchIndex = searchResults.indexOfFirst {
                        it.userId == userId
                    }

                    if (searchIndex != -1) {
                        searchResults[searchIndex] =
                            searchResults[searchIndex].copy(
                                status = ConnectionStatus.NOT_CONNECTED
                            )
                    }
                    loadStats()
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun acceptRequest(userId: Int) {
        viewModelScope.launch {
            repository
                .acceptConnectionRequest(userId)
                .onSuccess {
                    incomingRequests.removeAll {
                        it.userId == userId
                    }

                    loadConnections()
                    loadConnectionRequests()
                    loadStats()
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun declineRequest(userId: Int) {
        viewModelScope.launch {
            repository
                .removeConnectionRequest(userId)
                .onSuccess {
                    incomingRequests.removeAll {
                        it.userId == userId
                    }

                    loadConnectionRequests()
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun cancelInvite(userId: Int) {
        viewModelScope.launch {
            repository
                .removeConnectionRequest(userId)
                .onSuccess {
                    sentInvites.removeAll {
                        it.userId == userId
                    }

                    loadConnectionRequests()
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun searchUsers(query: String) {
        if (query.isBlank()) {
            searchResults.clear()
            return
        }

        viewModelScope.launch {
            repository
                .searchUsers(query)
                .onSuccess { result ->
                    searchResults.clear()
                    searchResults.addAll(result)
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun joinClub(clubId: Int) {
        viewModelScope.launch {
            repository
                .joinClub(clubId)
                .onSuccess {
                    refreshClubs()
                    loadStats()
                }
        }
    }

    fun leaveClub(clubId: Int) {
        viewModelScope.launch {
            repository
                .leaveClub(clubId)
                .onSuccess {
                    refreshClubs()
                    loadStats()
                }
        }
    }

    private fun loadConnections() {
        viewModelScope.launch {
            repository
                .getMyConnections()
                .onSuccess { result ->
                    connections.clear()
                    connections.addAll(result)
                    connectionsLoaded = true
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun loadClubs() {
        viewModelScope.launch {
            repository
                .getMyClubs()
                .onSuccess { result ->
                    clubs.clear()
                    clubs.addAll(result)
                    clubsLoaded = true
                }
            if (allClubs.isEmpty()) {
                loadAllClubs()
            }
        }
    }

    private fun loadHonors() {
        viewModelScope.launch {
            repository
                .getProfileHonors()
                .onSuccess { honors ->
                    honorRank = honors.honorRank
                    badges.clear()
                    badges.addAll(honors.badges)
                    medals.clear()
                    medals.addAll(honors.medals)
                    honorsLoaded = true
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun loadInterests() {
        viewModelScope.launch {
            repository
                .getSelectedInterests()
                .onSuccess { result ->
                    interests.clear()
                    interests.addAll(result)
                    interestsLoaded = true
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private fun loadAllInterestsIfNeeded() {
        viewModelScope.launch {
            if (allInterests.isEmpty()) {
                loadAllInterests()
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            repository
                .getMyStats()
                .onSuccess {
                    stats = it
                }
        }
    }

    private fun loadConnectionRequests() {
        viewModelScope.launch {
            repository
                .getConnectionRequests()
                .onSuccess { requests ->

                    incomingRequests.clear()
                    incomingRequests.addAll(
                        requests.filter {
                            it.type == RequestType.INCOMING
                        }
                    )

                    sentInvites.clear()
                    sentInvites.addAll(
                        requests.filter {
                            it.type == RequestType.OUTGOING
                        }
                    )
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    private suspend fun refreshClubs() {
        repository
            .getMyClubs()
            .onSuccess { result ->
                clubs.clear()
                clubs.addAll(result)
            }
        if (allClubs.isNotEmpty()) {
            loadAllClubs()
        }
    }

    fun addInterest(interest: Interest) {
        if (interest in interests) return

        viewModelScope.launch {
            repository.addInterest(interest.interestId)
                .onSuccess {
                    interests.add(interest)
                    loadStats()
                }
        }
    }

    fun removeInterest(interest: Interest) {
        viewModelScope.launch {
            repository.removeInterest(interest.interestId)
                .onSuccess {
                    interests.remove(interest)
                    loadStats()
                }
        }
    }

    // ----------------------------------------------------
// Honor collection reordering
// ----------------------------------------------------

    fun moveBadgeUp(index: Int) {
        moveBadgeTo(index, index - 1)
    }

    fun moveBadgeDown(index: Int) {
        moveBadgeTo(index, index + 1)
    }

    fun moveBadgeTo(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in badges.indices) return
        if (toIndex !in badges.indices) return
        if (fromIndex == toIndex) return

        val updated = badges.toMutableList()
        val item = updated.removeAt(fromIndex)
        updated.add(toIndex, item)

        badges.clear()
        badges.addAll(updated)

        saveHonorPriorities()
    }

    fun moveMedalUp(index: Int) {
        moveMedalTo(index, index - 1)
    }

    fun moveMedalDown(index: Int) {
        moveMedalTo(index, index + 1)
    }

    fun moveMedalTo(fromIndex: Int, toIndex: Int) {
        if (fromIndex !in medals.indices) return
        if (toIndex !in medals.indices) return
        if (fromIndex == toIndex) return

        val updated = medals.toMutableList()
        val item = updated.removeAt(fromIndex)
        updated.add(toIndex, item)

        medals.clear()
        medals.addAll(updated)

        saveHonorPriorities()
    }

    private fun saveHonorPriorities() {
        viewModelScope.launch {
            val reorderedHonors = badges + medals

            reorderedHonors.forEachIndexed { index, honor ->
                repository
                    .updateHonorPriority(
                        honorId = honor.honorId,
                        priority = index
                    )
                    .onFailure {
                        errorMessage = it.message
                    }
            }
        }
    }
}