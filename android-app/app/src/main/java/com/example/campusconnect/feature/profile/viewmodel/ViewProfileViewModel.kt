package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

import com.example.campusconnect.feature.profile.model.PublicUserProfile
import com.example.campusconnect.feature.profile.model.ProfileStats
import kotlinx.coroutines.async

class ViewProfileViewModel(
    application: Application,
    private val userId: Int
) : BaseProfileViewModel(application) {

    override var profile by mutableStateOf(PublicUserProfile())

    var stats by mutableStateOf(ProfileStats())
        private set

    init {
        loadViewedProfile()
    }


    private fun loadViewedProfile() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                coroutineScope {

                    val profileRequest = async {
                        repository.getProfile(userId)
                    }

                    val statsRequest = async {
                        repository.getUserStats(userId)
                    }

                    val connectionsRequest = async {
                        repository.getUserConnections(userId)
                    }

                    val clubsRequest = async {
                        repository.getUserClubs(userId)
                    }

                    val interestsRequest = async {
                        repository.getUserInterests(userId)
                    }

                    val honorsRequest = async {
                        repository.getUserHonors(userId)
                    }

                    val allClubsRequest = async {
                        repository.getAllClubs()
                    }

                    val allInterestsRequest = async {
                        repository.getAllInterests()
                    }

                    profileRequest.await()
                        .onSuccess {
                            profile = it
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

                    interestsRequest.await()
                        .onSuccess {
                            interests.clear()
                            interests.addAll(it)
                        }

                    honorsRequest.await()
                        .onSuccess { honors ->
                            honorRank = honors.honorRank

                            badges.clear()
                            badges.addAll(honors.badges)

                            medals.clear()
                            medals.addAll(honors.medals)
                        }

                    allClubsRequest.await()
                        .onSuccess {
                            allClubs.clear()
                            allClubs.addAll(it)
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