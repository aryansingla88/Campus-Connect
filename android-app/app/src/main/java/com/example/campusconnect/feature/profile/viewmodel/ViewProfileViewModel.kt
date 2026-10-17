package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.ProfileStats
import com.example.campusconnect.feature.profile.model.StatPanel
import com.example.campusconnect.feature.profile.model.ConnectionStatus

class ViewProfileViewModel(
    application: Application,
    private val userId: Int
) : BaseProfileViewModel(application) {

    override var profile by mutableStateOf(PublicUserProfile())

    var stats by mutableStateOf(ProfileStats())
        private set

    // ---------------------------------------------------------
    // Lazy-loading state
    // ---------------------------------------------------------

    private var connectionsLoaded = false
    private var clubsLoaded = false
    private var interestsLoaded = false
    private var honorsLoaded = false

    private var connectionsLoading = false
    private var clubsLoading = false
    private var interestsLoading = false
    private var honorsLoading = false

    init {
        loadViewedProfile()
    }

    // ---------------------------------------------------------
    // Initial profile loading
    // Only profile + stats
    // ---------------------------------------------------------

    private fun loadViewedProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                coroutineScope {

                    val profileRequest =
                        async {
                            repository.getProfile(userId)
                        }

                    val statsRequest =
                        async {
                            repository.getUserStats(userId)
                        }

                    profileRequest.await()
                        .onSuccess {
                            profile = it
                        }
                        .onFailure {
                            errorMessage = it.message
                        }

                    statsRequest.await()
                        .onSuccess {
                            stats = it
                        }
                        .onFailure {
                            errorMessage = it.message
                        }
                }
            } finally {
                isLoading = false
            }
        }
    }

    // ---------------------------------------------------------
    // Connection actions
    // ---------------------------------------------------------

    fun sendConnectionRequest() {
        viewModelScope.launch {
            repository
                .sendConnectionRequest(userId)
                .onSuccess {
                    profile = profile.copy(
                        relationshipStatus =
                            ConnectionStatus.PENDING
                    )
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    fun removeConnection() {
        viewModelScope.launch {
            repository
                .removeConnection(userId)
                .onSuccess {
                    profile = profile.copy(
                        relationshipStatus =
                            ConnectionStatus.NOT_CONNECTED
                    )
                }
                .onFailure {
                    errorMessage = it.message
                }
        }
    }

    // ---------------------------------------------------------
    // Lazy loading - Connections
    // ---------------------------------------------------------

    private fun loadConnections() {

        if (
            connectionsLoaded ||
            connectionsLoading
        ) {
            return
        }

        connectionsLoading = true

        viewModelScope.launch {

            repository
                .getUserConnections(userId)
                .onSuccess {

                    connections.clear()
                    connections.addAll(it)

                    // Mark loaded even if result is empty
                    connectionsLoaded = true
                }
                .onFailure {

                    errorMessage = it.message
                }

            connectionsLoading = false
        }
    }

    // ---------------------------------------------------------
    // Lazy loading - Clubs
    // ---------------------------------------------------------

    private fun loadClubs() {

        if (
            clubsLoaded ||
            clubsLoading
        ) {
            return
        }

        clubsLoading = true

        viewModelScope.launch {

            repository
                .getUserClubs(userId)
                .onSuccess {

                    clubs.clear()
                    clubs.addAll(it)

                    clubsLoaded = true
                }
                .onFailure {

                    errorMessage = it.message
                }

            clubsLoading = false
        }
    }

    // ---------------------------------------------------------
    // Lazy loading - Interests
    // ---------------------------------------------------------

    private fun loadInterests() {

        if (
            interestsLoaded ||
            interestsLoading
        ) {
            return
        }

        interestsLoading = true

        viewModelScope.launch {

            repository
                .getUserInterests(userId)
                .onSuccess {

                    interests.clear()
                    interests.addAll(it)

                    interestsLoaded = true
                }
                .onFailure {

                    errorMessage = it.message
                }

            interestsLoading = false
        }
    }

    // ---------------------------------------------------------
    // Lazy loading - Honors
    // ---------------------------------------------------------

    private fun loadHonor() {

        if (
            honorsLoaded ||
            honorsLoading
        ) {
            return
        }

        honorsLoading = true

        viewModelScope.launch {

            repository
                .getUserHonors(userId)
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

            honorsLoading = false
        }
    }

    // ---------------------------------------------------------
    // Panel handling
    // ---------------------------------------------------------

    override fun togglePanel(
        panel: StatPanel
    ) {

        super.togglePanel(panel)

        // Only load when panel becomes active
        if (activePanel != panel) {
            return
        }

        when (panel) {

            StatPanel.CONNECTIONS -> {
                loadConnections()
            }

            StatPanel.CLUBS -> {
                loadClubs()
            }

            StatPanel.INTERESTS -> {
                loadInterests()
            }

            StatPanel.HONOR -> {
                loadHonor()
            }

            else -> Unit
        }
    }

    // ---------------------------------------------------------
    // Factory
    // ---------------------------------------------------------

    companion object {

        fun factory(
            application: Application,
            userId: Int
        ) = object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(
                modelClass: Class<T>
            ): T {

                @Suppress("UNCHECKED_CAST")

                return ViewProfileViewModel(
                    application = application,
                    userId = userId
                ) as T
            }
        }
    }
}