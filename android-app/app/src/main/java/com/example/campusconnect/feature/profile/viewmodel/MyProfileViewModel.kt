package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.campusconnect.feature.profile.model.*

class MyProfileViewModel(
    application: Application
) : BaseProfileViewModel(application) {

    override var profile by mutableStateOf(PublicUserProfile())

    var stats by mutableStateOf(ProfileStats())
        private set

    val allClubs = mutableStateListOf<Club>()

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

            repository.getMyProfile()
                .getOrNull()
                ?.let {
                    profile = it
                    editableProfile = it.copy()
                }

            repository.getMyStats()
                .getOrNull()
                ?.let {
                    stats = it
                }

            repository.getMyConnections()
                .getOrNull()
                ?.let {
                    connections.clear()
                    connections.addAll(it)
                }

            refreshClubs()

            repository.getProfileHonors()
                .getOrNull()
                ?.let { honors ->
                    honorRank = honors.honorRank

                    badges.clear()
                    badges.addAll(honors.badges)

                    medals.clear()
                    medals.addAll(honors.medals)
                }

            repository.getSelectedInterests()
                .getOrNull()
                ?.let {
                    interests.clear()
                    interests.addAll(it)
                }

            loadAllInterests()
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

        repository
            .getAllClubs()
            .onSuccess { result ->
                allClubs.clear()
                allClubs.addAll(result)
            }
    }
}