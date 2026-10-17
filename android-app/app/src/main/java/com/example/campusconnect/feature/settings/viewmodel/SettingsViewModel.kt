package com.example.campusconnect.feature.settings.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.settings.model.*

// --- Which modal sheet (if any) is currently showing over the Settings list -----------
// LOCATION_VISIBILITY and SELECT_CLUBS are treated as a small chained stack: opening
// Select Clubs from within Location Visibility, then closing it, returns to Location
// Visibility rather than closing everything.
enum class SettingsSheet {
    LOCATION_VISIBILITY, SELECT_CLUBS, APP_APPEARANCE, LANGUAGE, DELETE_ACCOUNT
}

// --- Settings screen state holder --------------------------------------------------
// There is no backing repository — every row is either static copy (rows that aren't
// built yet, see SettingsDestinations) or local, in-memory state for the screens that
// ARE built here. Once account/preference data needs to be read or written for real, a
// SettingsRepository can be introduced the same way ProfileRepository was, and the
// fields below would move from local mutableStateOf to loaded/saved state.
class SettingsViewModel : ViewModel() {

    // --- Main list ---------------------------------------------------------------
    val sections: List<SettingsSection> = SettingsContent.sections()
    val dangerItem: SettingsItem = SettingsContent.dangerItem()

    var activeSheet by mutableStateOf<SettingsSheet?>(null)
        private set

    fun openSheet(sheet: SettingsSheet) { activeSheet = sheet }
    fun closeSheet() { activeSheet = null }

    // --- Profile Visibility (full screen) -------------------------------------------
    val alwaysVisibleFields: List<AlwaysVisibleField> = ProfileVisibilityDefaults.alwaysVisibleFields()
    val visibilityToggles = mutableStateListOf(*ProfileVisibilityDefaults.controlFields().toTypedArray())

    fun setVisibilityToggle(id: VisibilityFieldId, isOn: Boolean) {
        val index = visibilityToggles.indexOfFirst { it.id == id }
        if (index != -1) {
            visibilityToggles[index] = visibilityToggles[index].copy(isOn = isOn)
        }
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
