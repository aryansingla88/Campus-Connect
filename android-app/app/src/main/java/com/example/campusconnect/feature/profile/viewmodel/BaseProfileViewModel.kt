package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.profile.data.FakeClubsService
import com.example.campusconnect.feature.profile.data.FakeConnectionsService
import com.example.campusconnect.feature.profile.data.FakeHonorService
import com.example.campusconnect.feature.profile.data.FakeInterestsService
import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.StatPanel

abstract class BaseProfileViewModel : ViewModel() {

    abstract var profile: PublicUserProfile

    val connections  = FakeConnectionsService.getConnections().toMutableStateList()
    val clubs        = FakeClubsService.getClubs().toMutableStateList()
    val badges       = FakeHonorService.getBadges()
    val medals       = FakeHonorService.getMedals()
    val honorEntries = FakeHonorService.getHonorEntries()
    val interests    = FakeInterestsService.getInterests().toMutableStateList()

    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) null else panel
    }
}