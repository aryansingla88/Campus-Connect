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

    val connections =
        FakeConnectionsService
            .getConnections()
            .toMutableStateList()

    val clubs =
        FakeClubsService
            .getClubs()
            .toMutableStateList()

    val badges =
        FakeHonorService
            .getBadges()
            .toMutableStateList()

    val medals =
        FakeHonorService
            .getMedals()
            .toMutableStateList()

    val honorEntries =
        FakeHonorService
            .getHonorEntries()

    val interests =
        FakeInterestsService
            .getInterests()
            .toMutableStateList()

    val allInterests =
        FakeInterestsService
            .getAllInterests()
    var activePanel by mutableStateOf<StatPanel?>(null)
        private set

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

    fun addInterest(interest: String) {

        if (interest !in interests) {
            interests.add(interest)
        }
    }

    fun removeInterest(interest: String) {
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