package com.example.campusconnect.feature.settings.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusconnect.feature.settings.data.ProfileVisibilityRepository
import com.example.campusconnect.feature.settings.data.UpdateProfileVisibilityPreferencesRequest
import com.example.campusconnect.core.location.TrackingMode
import com.example.campusconnect.core.location.TrackingModePreferences
import com.example.campusconnect.feature.settings.model.*
import kotlinx.coroutines.launch

// --- Which modal sheet (if any) is currently showing over the Settings list -----------
// LOCATION_VISIBILITY and SELECT_CLUBS are treated as a small chained stack: opening
// Select Clubs from within Location Visibility, then closing it, returns to Location
// Visibility rather than closing everything.
enum class SettingsSheet {
    LOCATION_VISIBILITY, SELECT_CLUBS, LOCATION_TRACKING_MODE, APP_APPEARANCE, LANGUAGE, DELETE_ACCOUNT
}

// --- Settings screen state holder --------------------------------------------------
// Profile visibility is loaded and saved through the existing backend preferences API.
class SettingsViewModel(
    private val profileVisibilityRepository: ProfileVisibilityRepository = ProfileVisibilityRepository()
) : ViewModel() {

    // --- Main list ---------------------------------------------------------------
    val sections: List<SettingsSection> = SettingsContent.sections()
    val dangerItem: SettingsItem = SettingsContent.dangerItem()

    var activeSheet by mutableStateOf<SettingsSheet?>(null)
        private set

    fun openSheet(sheet: SettingsSheet) { activeSheet = sheet }
    fun closeSheet() { activeSheet = null }

    val trackingMode = TrackingModePreferences.mode

    fun selectTrackingMode(mode: TrackingMode) {
        TrackingModePreferences.setMode(mode)
    }

    // --- Profile Visibility (full screen) -------------------------------------------
    val alwaysVisibleFields: List<AlwaysVisibleField> = ProfileVisibilityDefaults.alwaysVisibleFields()
    val visibilityToggles = mutableStateListOf(*ProfileVisibilityDefaults.controlFields().toTypedArray())
    var visibilitySaving by mutableStateOf(false)
        private set
    var visibilityError by mutableStateOf<String?>(null)
        private set

    init {
        viewModelScope.launch {
            profileVisibilityRepository.get().onSuccess(::applyVisibilityPreferences)
                .onFailure { visibilityError = it.message }
        }
    }

    fun setVisibilityToggle(id: VisibilityFieldId, isOn: Boolean) {
        val index = visibilityToggles.indexOfFirst { it.id == id }
        if (index != -1) {
            visibilityToggles[index] = visibilityToggles[index].copy(isOn = isOn)
        }
    }

    fun saveProfileVisibility(onSaved: () -> Unit) {
        if (visibilitySaving) return
        val values = visibilityToggles.associate { it.id to it.isOn }
        visibilitySaving = true
        visibilityError = null
        viewModelScope.launch {
            profileVisibilityRepository.update(
                UpdateProfileVisibilityPreferencesRequest(
                    showPhone = values[VisibilityFieldId.PHONE_NUMBER] ?: false,
                    showSocials = values[VisibilityFieldId.SOCIAL_LINKS] ?: true,
                    showHometown = values[VisibilityFieldId.HOMETOWN] ?: false,
                    showGender = values[VisibilityFieldId.GENDER] ?: false,
                    showAge = values[VisibilityFieldId.AGE] ?: false,
                    showHonor = values[VisibilityFieldId.HONORS_ACHIEVEMENTS] ?: false
                )
            ).onSuccess { response ->
                applyVisibilityPreferences(response)
                onSaved()
            }.onFailure { visibilityError = it.message }
            visibilitySaving = false
        }
    }

    private fun applyVisibilityPreferences(
        response: com.example.campusconnect.feature.profile.data.remote.response.ProfileVisibilityPreferencesResponse
    ) {
        val values = mapOf(
            VisibilityFieldId.PHONE_NUMBER to (response.showPhone ?: false),
            VisibilityFieldId.SOCIAL_LINKS to (response.showSocials ?: true),
            VisibilityFieldId.HOMETOWN to (response.showHometown ?: false),
            VisibilityFieldId.GENDER to (response.showGender ?: false),
            VisibilityFieldId.AGE to (response.showAge ?: false),
            VisibilityFieldId.HONORS_ACHIEVEMENTS to (response.showHonor ?: false)
        )
        visibilityToggles.indices.forEach { index ->
            val toggle = visibilityToggles[index]
            visibilityToggles[index] = toggle.copy(isOn = values[toggle.id] ?: false)
        }
        visibilityError = null
    }

    // --- Manage Social Links (full screen) -------------------------------------------
    val socialLinks = mutableStateListOf(*SocialLinkDefaults.defaultLinks().toTypedArray())
    private var nextSocialLinkId = socialLinks.maxOf { it.id } + 1

    fun updateSocialLinkUrl(id: Int, url: String) {
        val index = socialLinks.indexOfFirst { it.id == id }
        if (index != -1) {
            socialLinks[index] = socialLinks[index].copy(url = url)
        }
    }

    fun addSocialLink() {
        socialLinks.add(SocialLinkDefaults.blankLink(nextSocialLinkId))
        nextSocialLinkId += 1
    }

    fun removeSocialLink(id: Int) {
        socialLinks.removeAll { it.id == id }
    }

    // --- Notifications (full screen) -------------------------------------------------
    val activityNotifications = mutableStateListOf(*NotificationDefaults.activityToggles().toTypedArray())
    val systemNotifications = mutableStateListOf(*NotificationDefaults.systemToggles().toTypedArray())

    fun setNotificationToggle(
        list: SnapshotStateList<NotificationToggle>,
        id: NotificationId,
        isOn: Boolean
    ) {
        val index = list.indexOfFirst { it.id == id }
        if (index != -1) {
            list[index] = list[index].copy(isOn = isOn)
        }
    }

    // --- Location Visibility + Select Clubs (chained bottom sheets) -----------------
    val locationOptions: List<LocationVisibilityOption> = LocationVisibilityDefaults.options()
    var selectedLocationOption by mutableStateOf(LocationVisibilityId.PUBLIC)
        private set

    val clubs = mutableStateListOf(*LocationVisibilityDefaults.clubs().toTypedArray())
    var clubSearchQuery by mutableStateOf("")
        private set

    val filteredClubs: List<SelectableClub>
        get() = if (clubSearchQuery.isBlank()) {
            clubs
        } else {
            clubs.filter { it.name.contains(clubSearchQuery, ignoreCase = true) }
        }

    fun selectLocationOption(id: LocationVisibilityId) {
        selectedLocationOption = id
    }

    fun toggleClubSelection(id: Int) {
        val index = clubs.indexOfFirst { it.id == id }
        if (index != -1) {
            clubs[index] = clubs[index].copy(isSelected = !clubs[index].isSelected)
        }
    }

    fun updateClubSearchQuery(query: String) {
        clubSearchQuery = query
    }

    // --- App Appearance (bottom sheet) -----------------------------------------------
    val themeOptions: List<ThemeOption> = AppearanceDefaults.options()
    var selectedTheme by mutableStateOf(ThemeOptionId.LIGHT)
        private set

    fun selectTheme(id: ThemeOptionId) { selectedTheme = id }

    // --- Language (bottom sheet) -------------------------------------------------
    val languageOptions: List<LanguageOption> = LanguageDefaults.options()
    var selectedLanguage by mutableStateOf(LanguageOptionId.ENGLISH)
        private set

    fun selectLanguage(id: LanguageOptionId) { selectedLanguage = id }
}
