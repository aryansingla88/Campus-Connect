package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.profile.data.FakeProfileService
import com.example.campusconnect.feature.profile.data.FakeClubsService
import com.example.campusconnect.feature.profile.data.FakeConnectionsService
import com.example.campusconnect.feature.profile.data.FakeHonorService
import com.example.campusconnect.feature.profile.data.FakeInterestsService
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.model.PublicUserProfile


class MyProfileViewModel : ViewModel() {

    // -- Data -----------------------------------------------------------------
    var profile by mutableStateOf(FakeProfileService.getMyProfile())
        private set

    var editableProfile by mutableStateOf(profile.copy())
        private set
    val connections = FakeConnectionsService.getConnections().toMutableStateList()
    val clubs       = FakeClubsService.getClubs().toMutableStateList()
    val badges      = FakeHonorService.getBadges()
    val medals      = FakeHonorService.getMedals()
    val honorEntries = FakeHonorService.getHonorEntries()
    val interests   = FakeInterestsService.getInterests().toMutableStateList()
    var isEditMode by mutableStateOf(false)
        private set

    // --- UI state -----------------------------------------------------------------
    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set


    // -- Panel Logic -----------------------------------------------------------------
    fun togglePanel(panel: StatPanel) {
        activePanel =
            if (activePanel == panel)
                null
            else panel
    }

    fun openManagePanel(panel: StatPanel) {
        activeManagePanel = panel
    }

    fun closeManagePanel() {
        activeManagePanel = null
    }


    // -- Edit Profile -----------------------------------------------------------------
    fun startEditing() {
        editableProfile = profile.copy()
        isEditMode = true
    }

    fun updateEditableProfile(
        updated: PublicUserProfile
    ) {
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
}