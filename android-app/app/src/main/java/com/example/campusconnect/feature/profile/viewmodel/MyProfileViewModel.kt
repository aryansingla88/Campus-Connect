package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import com.example.campusconnect.feature.profile.data.fake.FakeProfileService
import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.data.fake.FakeRequestsService
import com.example.campusconnect.feature.profile.model.ConnectionStatus
class MyProfileViewModel : BaseProfileViewModel() {

    override var profile by mutableStateOf(FakeProfileService.getMyProfile())

    var editableProfile by mutableStateOf(profile.copy())
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

    val incomingRequests =
        FakeRequestsService
            .getIncomingRequests()
            .toMutableStateList()

    val sentInvites =
        FakeRequestsService
            .getSentInvites()
            .toMutableStateList()

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
        profile = editableProfile
        isEditMode = false
    }

    fun acceptRequest(userId: String) {

        val request =
            incomingRequests.find {
                it.userId == userId
            } ?: return

        incomingRequests.remove(request)

        connections.add(
            request.copy(
                status = ConnectionStatus.CONNECTED
            )
        )
    }

    fun declineRequest(userId: String) {
        incomingRequests.removeAll {
            it.userId == userId
        }
    }

    fun cancelInvite(userId: String) {
        sentInvites.removeAll {
            it.userId == userId
        }
    }
}
