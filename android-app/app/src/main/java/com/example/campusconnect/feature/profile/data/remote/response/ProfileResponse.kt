package com.example.campusconnect.feature.profile.data.remote.response


data class ProfileResponse(

    // User ----------------------------------------------------------

    val userId: Int?,
    val username: String?,
    val email: String?,


    // Profile header -------------------------------------------------

    val fullName: String?,
    val bio: String?,
    val avatarUrl: String?,


    // Academic -------------------------------------------------------

    val courseId: Int?,
    val admissionYear: Int?,


    // Other profile details ------------------------------------------

    val hostelId: Int? = null,
    val hometown: String?,
    val gender: String?,
    val dob: String?,
    val age: Int?,
    val phone: String?,

    val github: String?,
    val linkedin: String?,
    val instagram: String?,

    val memberSince: String?,


    // Preferences ----------------------------------------------------

    val showPhone: Boolean?,
    val showSocials: Boolean?,
    val showHometown: Boolean?,
    val showGender: Boolean?,
    val showAge: Boolean?,
    val showHonor: Boolean?,

    val headerBadges: List<HonorResponse>?,
    val headerMedals: List<HonorResponse>?,

    val relationshipStatus: String?

)
