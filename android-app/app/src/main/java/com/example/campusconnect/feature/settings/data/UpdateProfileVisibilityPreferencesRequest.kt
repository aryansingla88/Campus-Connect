package com.example.campusconnect.feature.settings.data

data class UpdateProfileVisibilityPreferencesRequest(
    val showPhone: Boolean,
    val showSocials: Boolean,
    val showHometown: Boolean,
    val showGender: Boolean,
    val showAge: Boolean,
    val showHonor: Boolean
)
