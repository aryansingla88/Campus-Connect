package com.example.campusconnect.feature.profile.viewmodel

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.profile.model.*
import com.example.campusconnect.feature.profile.data.repo.*
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


abstract class BaseProfileViewModel : ViewModel() {

    protected open val repository: ProfileRepository =
        FakeProfileRepository()

    abstract var profile: PublicUserProfile

    val connections = mutableStateListOf<Connection>()

    val clubs = mutableStateListOf<Club>()

    var honorRank by mutableIntStateOf(0)
        private set

    val badges = mutableStateListOf<ProfileHonor>()

    val medals = mutableStateListOf<ProfileHonor>()


    val interests = mutableStateListOf<Interest>()
    var allInterests by mutableStateOf<List<Interest>>(emptyList())
        private set


    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

    protected fun loadData(){

        viewModelScope.launch {

            repository.getMyConnections()
                .getOrNull()
                ?.let { connections.addAll(it) }

            repository.getMyClubs()
                .getOrNull()
                ?.let { clubs.addAll(it) }

            repository.getProfileHonors()
                .getOrNull()
                ?.let { honors ->

                    honorRank = honors.honorRank

                    badges.addAll(honors.badges)

                    medals.addAll(honors.medals)
                }

            repository.getSelectedInterests()
                .getOrNull()
                ?.let { interests.addAll(it) }

            allInterests =
                repository
                    .getAllInterests()
                    .getOrDefault(emptyList())
        }
    }

    fun togglePanel(panel: StatPanel) {
        activePanel = if (activePanel == panel) null else panel
    }

    fun moveBadgeUp(index: Int) {
        if (index <= 0) return

        val item = badges.removeAt(index)
        badges.add(index - 1, item)
    }

    fun moveBadgeDown(index: Int) {
        if (index >= badges.lastIndex) return

        val item = badges.removeAt(index)
        badges.add(index + 1, item)
    }

    fun moveMedalUp(index: Int) {
        if (index <= 0) return

        val item = medals.removeAt(index)
        medals.add(index - 1, item)
    }

    fun moveMedalDown(index: Int) {
        if (index >= medals.lastIndex) return

        val item = medals.removeAt(index)
        medals.add(index + 1, item)
    }


    fun addInterest(interest: Interest) {

        if (interest !in interests) {
            interests.add(interest)
        }
    }

    fun removeInterest(interest: Interest) {
        interests.remove(interest)
    }

    fun moveBadgeTo(
        from: Int,
        to: Int
    ) {

        val targetIndex =
            to.coerceIn(
                0,
                badges.lastIndex
            )

        if (from == targetIndex) return

        val item = badges.removeAt(from)
        badges.add(targetIndex, item)
    }

    fun moveMedalTo(
        from: Int,
        to: Int
    ) {

        val targetIndex =
            to.coerceIn(
                0,
                medals.lastIndex
            )

        if (from == targetIndex) return

        val item = medals.removeAt(from)
        medals.add(targetIndex, item)
    }
}