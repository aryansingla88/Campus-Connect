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

    var editableProfile by mutableStateOf(PublicUserProfile())
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

    val incomingRequests = mutableStateListOf<ConnectionRequest>()

    val sentInvites = mutableStateListOf<ConnectionRequest>()

    init {

        loadData()

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

            repository.getConnectionRequests()
                .getOrNull()
                ?.let { requests ->

                    incomingRequests.addAll(
                        requests.filter {
                            it.type == RequestType.INCOMING
                        }
                    )

                    sentInvites.addAll(
                        requests.filter {
                            it.type == RequestType.OUTGOING
                        }
                    )
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

    fun acceptRequest(userId: Int) {

        incomingRequests.removeAll {
            it.userId == userId
        }
    }

    fun declineRequest(userId: Int) {

        incomingRequests.removeAll {
            it.userId == userId
        }
    }

    fun cancelInvite(userId: Int) {

        sentInvites.removeAll {
            it.userId == userId
        }
    }

}