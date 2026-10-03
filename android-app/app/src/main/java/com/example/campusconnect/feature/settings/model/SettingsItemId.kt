package com.example.campusconnect.feature.settings.model

// --- Stable identifiers for every row rendered in the Settings screen ------------------
// Kept as an enum (rather than raw strings) so ViewModel / Nav / Analytics can all
// switch over a closed set instead of comparing strings.
enum class SettingsItemId {

    // Account
    EDIT_PROFILE,
    CHANGE_PASSWORD,
    PROFILE_VISIBILITY,
    LOCATION_VISIBILITY,
    MANAGE_SOCIAL_LINKS,

    // Preferences
    NOTIFICATIONS,
    APP_APPEARANCE,
    LANGUAGE,

    // Notifications screen sub-row (not on the main list, not routed anywhere —
    // handled locally by NotificationsScreen's own onQuietHoursClick callback)
    QUIET_HOURS,

    // Data & Privacy
    MANAGE_DATA,
    PRIVACY_SECURITY,

    // Support
    HELP_FAQ,
    CONTACT_US,
    TERMS_POLICIES,

    // About
    ABOUT_APP,
    DELETE_ACCOUNT
}

// --- Route constants for rows that are NOT built yet ------------------------------------
// Change Password, Manage Data, Privacy & Security, Help & FAQ, Contact Us, Terms &
// Policies and About are all still just route stubs — per instructions, those screens
// aren't guessed at here and will be wired up separately. Edit Profile reuses the
// "edit_profile" route ProfileNav already defines. Everything else in this enum either
// opens a full screen or a bottom sheet directly from SettingsScreen (see
// SettingsScreen.kt's `handleClick`) and never reaches this map.
object SettingsDestinations {

    private const val BASE = "settings"

    fun routeFor(id: SettingsItemId): String = when (id) {
        SettingsItemId.EDIT_PROFILE     -> "edit_profile"
        SettingsItemId.CHANGE_PASSWORD  -> "$BASE/change_password"
        SettingsItemId.MANAGE_DATA      -> "$BASE/manage_data"
        SettingsItemId.PRIVACY_SECURITY -> "$BASE/privacy_security"
        SettingsItemId.HELP_FAQ         -> "$BASE/help_faq"
        SettingsItemId.CONTACT_US       -> "$BASE/contact_us"
        SettingsItemId.TERMS_POLICIES   -> "$BASE/terms_policies"
        SettingsItemId.ABOUT_APP        -> "$BASE/about"
        else -> "$BASE/unknown" // unreachable for ids handled locally by SettingsScreen
    }
}
