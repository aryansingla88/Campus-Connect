package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import com.example.campusconnect.feature.profile.data.FakeProfileService
import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.StatPanel

class MyProfileViewModel : BaseProfileViewModel() {

    override var profile by mutableStateOf(FakeProfileService.getMyProfile())

    var editableProfile by mutableStateOf(profile.copy())
        private set

    var isEditMode by mutableStateOf(false)
        private set

    var activeManagePanel by mutableStateOf<StatPanel?>(null)
        private set

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
}