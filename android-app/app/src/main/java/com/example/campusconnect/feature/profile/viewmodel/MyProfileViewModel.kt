package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
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

    var editableProfile by mutableStateOf(PublicUserProfile())
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

    val incomingRequests = mutableStateListOf<ConnectionRequest>()

    val sentInvites = mutableStateListOf<ConnectionRequest>()

    init {
        loadMyData()
        loadConnectionRequests()
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

                    val connectionsRequest = async {
                        repository.getMyConnections()
                    }

                    val clubsRequest = async {
                        repository.getMyClubs()
                    }

                    val allClubsRequest = async {
                        repository.getAllClubs()
                    }

                    val honorsRequest = async {
                        repository.getProfileHonors()
                    }

                    val interestsRequest = async {
                        repository.getSelectedInterests()
                    }

                    val allInterestsRequest = async {
                        repository.getAllInterests()
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

                    connectionsRequest.await()
                        .onSuccess {
                            connections.clear()
                            connections.addAll(it)
                        }

                    clubsRequest.await()
                        .onSuccess {
                            clubs.clear()
                            clubs.addAll(it)
                        }

                    allClubsRequest.await()
                        .onSuccess {
                            allClubs.clear()
                            allClubs.addAll(it)
                        }

                    honorsRequest.await()
                        .onSuccess { honors ->
                            honorRank = honors.honorRank

                            badges.clear()
                            badges.addAll(honors.badges)

                            medals.clear()
                            medals.addAll(honors.medals)
                        }

                    interestsRequest.await()
                        .onSuccess {
                            interests.clear()
                            interests.addAll(it)
                        }

                    allInterestsRequest.await()
                        .onSuccess {
                            allInterests.clear()
                            allInterests.addAll(it)
                        }
                }
            } finally {
                isLoading = false
            }
        }
    }


    fun openManagePanel(panel: StatPanel) {
        activeManagePanel = panel
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
        isEditMode = false
    }

    fun saveProfileChanges() {
        viewModelScope.launch {
            repository
                .updateProfile(editableProfile)
                .onSuccess {
                    profile = it
                    isEditMode = false
                }
        }
    }

    fun sendConnectionRequest(userId: Int) {
        viewModelScope.launch {
            repository
                .sendConnectionRequest(userId)
                .onSuccess {
                    val index = connections.indexOfFirst {
                        it.userId == userId
                    }

                    if (index != -1) {
                        connections[index] = connections[index].copy(
                            status = ConnectionStatus.PENDING
                        )
                    }
                }
                .onFailure {
                    // Use your existing error handling here
                }
        }
    }

    fun removeConnection(userId: Int) {
        viewModelScope.launch {
            repository
                .removeConnection(userId)
                .onSuccess {
                    val index = connections.indexOfFirst {
                        it.userId == userId
                    }

                    if (index != -1) {
                        connections[index] = connections[index].copy(
                            status = ConnectionStatus.NOT_CONNECTED
                        )
                    }
                }
                .onFailure {
                    // Use your existing error handling here
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
                }
                .onFailure {
                    // Use your existing error handling here
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
                    // Use your existing error handling here
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
                    // Use your existing error handling here
                }
        }
    }

    fun joinClub(clubId: Int) {
        viewModelScope.launch {
            repository
                .joinClub(clubId)
                .onSuccess {
                    refreshClubs()
                }
        }
    }

    fun leaveClub(clubId: Int) {
        viewModelScope.launch {
            repository
                .leaveClub(clubId)
                .onSuccess {
                    refreshClubs()
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
                }
                .onFailure {
                    // Use your existing error handling here
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
                    // Use your existing error handling here
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

    }

    fun addInterest(interest: Interest) {
        if (interest in interests) return

        viewModelScope.launch {
            repository.addInterest(interest.interestId)
                .onSuccess {
                    interests.add(interest)
                }
        }
    }

    fun removeInterest(interest: Interest) {
        viewModelScope.launch {
            repository.removeInterest(interest.interestId)
                .onSuccess {
                    interests.remove(interest)
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