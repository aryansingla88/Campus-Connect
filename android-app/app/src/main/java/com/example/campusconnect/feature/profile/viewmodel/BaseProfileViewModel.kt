package com.example.campusconnect.feature.profile.viewmodel

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
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

    val clubDetails = mutableStateMapOf<Int, ClubDetails>()
    val clubMembers = mutableStateMapOf<Int, List<ClubMember>>()
    val clubDetailsLoadingIds = mutableStateListOf<Int>()
    val clubMembersLoadingIds = mutableStateListOf<Int>()
    val clubDetailsErrors = mutableStateMapOf<Int, String>()
    val clubMembersErrors = mutableStateMapOf<Int, String>()

    var honorRank by mutableIntStateOf(0)
        protected set

    val badges = mutableStateListOf<ProfileHonor>()
    val medals = mutableStateListOf<ProfileHonor>()
    val interests = mutableStateListOf<Interest>()

    var isLoading by mutableStateOf(false)
        protected set

    var errorMessage by mutableStateOf<String?>(null)
        protected set

    // Club ids with a join/leave request in flight (blocks double taps, drives the button spinner)
    val joiningClubIds = mutableStateListOf<Int>()

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

    protected open suspend fun onMembershipChanged() {}

    fun loadClubDetails(clubId: Int, force: Boolean = false) {
        if (clubId in clubDetailsLoadingIds || (!force && clubId in clubDetails)) return
        clubDetailsLoadingIds.add(clubId)
        clubDetailsErrors.remove(clubId)
        viewModelScope.launch {
            repository.getClubDetails(clubId)
                .onSuccess { clubDetails[clubId] = it }
                .onFailure { clubDetailsErrors[clubId] = it.message ?: "Couldn't load club details" }
            clubDetailsLoadingIds.remove(clubId)
        }
    }

    fun loadClubMembers(clubId: Int, force: Boolean = false) {
        if (clubId in clubMembersLoadingIds || (!force && clubId in clubMembers)) return
        clubMembersLoadingIds.add(clubId)
        clubMembersErrors.remove(clubId)
        viewModelScope.launch {
            repository.getClubMembers(clubId)
                .onSuccess { clubMembers[clubId] = it }
                .onFailure { clubMembersErrors[clubId] = it.message ?: "Couldn't load club members" }
            clubMembersLoadingIds.remove(clubId)
        }
    }

    fun joinClub(clubId: Int, onResult: (Result<ClubStatus>) -> Unit = {}) =
        changeMembership(clubId, onResult) { repository.joinClub(clubId) }

    fun leaveClub(clubId: Int, onResult: (Result<ClubStatus>) -> Unit = {}) =
        changeMembership(clubId, onResult) { repository.leaveClub(clubId) }

    private fun changeMembership(
        clubId: Int,
        onResult: (Result<ClubStatus>) -> Unit,
        call: suspend () -> Result<ClubStatus>
    ) {
        if (clubId in joiningClubIds) return
        joiningClubIds.add(clubId)
        viewModelScope.launch {
            val result = call().onSuccess { status ->
                clubDetails[clubId]?.let { current ->
                    clubDetails[clubId] = current.copy(status = status)
                }
                onMembershipChanged()
                loadClubDetails(clubId, force = true)
                if (clubId in clubMembers) loadClubMembers(clubId, force = true)
            }
            joiningClubIds.remove(clubId)
            onResult(result)
        }
    }

    fun updateClubSearchQuery(query: String) {
        clubSearchQuery = query
    }

    open fun togglePanel(panel: StatPanel) {
        activePanel =
            if (activePanel == panel) {
                null
            } else {
                panel
            }
    }
}
