package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.campusconnect.feature.profile.model.*
import com.example.campusconnect.feature.profile.data.repo.*
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider


abstract class BaseProfileViewModel(
    application: Application
) : AndroidViewModel(application) {

    protected open val repository: ProfileRepository =
        ApiProfileRepository(
            courseRepository =
                CourseRepositoryProvider.getRepository(
                    application.applicationContext
                )
        )

    abstract var profile: PublicUserProfile

    val connections = mutableStateListOf<Connection>()

    val clubs = mutableStateListOf<Club>()

    // Keep only this declaration
    val allClubs = mutableStateListOf<Club>()

    var honorRank by mutableIntStateOf(0)
        protected set

    val badges = mutableStateListOf<ProfileHonor>()
    val medals = mutableStateListOf<ProfileHonor>()
    val interests = mutableStateListOf<Interest>()

    var isLoading by mutableStateOf(false)
        protected set

    var errorMessage by mutableStateOf<String?>(null)
        protected set

    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

    val allInterests = mutableStateListOf<Interest>()

    var clubSearchQuery by mutableStateOf("")
        private set

    val filteredClubs: List<Club>
        get() {
            if (clubSearchQuery.isBlank()) {
                return allClubs
            }

            return allClubs.filter { club ->
                club.name.contains(
                    other = clubSearchQuery,
                    ignoreCase = true
                )
            }
        }

    protected suspend fun loadAllInterests() {
        repository
            .getAllInterests()
            .onSuccess { result ->
                allInterests.clear()
                allInterests.addAll(result)
            }
            .onFailure {
                errorMessage = it.message
            }
    }

    protected suspend fun loadAllClubs() {
        repository
            .getAllClubs()
            .onSuccess { result ->
                allClubs.clear()
                allClubs.addAll(result)
            }
            .onFailure {
                errorMessage = it.message
            }
    }

    fun updateClubSearchQuery(query: String) {
        clubSearchQuery = query
    }

    fun togglePanel(panel: StatPanel) {
        activePanel =
            if (activePanel == panel) {
                null
            } else {
                panel
            }
    }
}