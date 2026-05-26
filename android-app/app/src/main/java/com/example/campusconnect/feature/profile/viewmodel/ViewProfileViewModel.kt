package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.campusconnect.feature.profile.data.FakeClubsService
import com.example.campusconnect.feature.profile.data.FakeConnectionsService
import com.example.campusconnect.feature.profile.data.FakeHonorService
import com.example.campusconnect.feature.profile.data.FakeInterestsService
import com.example.campusconnect.feature.profile.data.FakeProfileService
import com.example.campusconnect.feature.profile.model.StatPanel

class ViewProfileViewModel(userId: String) : ViewModel() {

    // -- Data -----------------------------------------------------------------
    var profile      by mutableStateOf(FakeProfileService.getProfile(userId))
        private set
    val connections  = FakeConnectionsService.getConnections().toMutableStateList()
    val clubs        = FakeClubsService.getClubs().toMutableStateList()
    val badges       = FakeHonorService.getBadges()
    val medals       = FakeHonorService.getMedals()
    val honorEntries = FakeHonorService.getHonorEntries()
    val interests    = FakeInterestsService.getInterests()

    // -- UI state -----------------------------------------------------------------
    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) null else panel
    }

    // -- Factory -----------------------------------------------------------------
    companion object {
        fun factory(userId: String) = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ViewProfileViewModel(userId) as T
            }
        }
    }
}