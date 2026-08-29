package com.example.campusconnect.feature.profile.data.remote.response


data class ProfileResponse(

    // User ----------------------------------------------------------

    val userId: Int,
    val username: String,
    val email: String,


    // Profile header -------------------------------------------------

    val fullName: String,
    val bio: String?,
    val avatarUrl: String?,


    // Academic -------------------------------------------------------

    val courseId: Int,
    val admissionYear: Int,


    // Other profile details ------------------------------------------

    val hostel: String,
    val hometown: String?,
    val gender: String?,
    val dob: String?,
    val phone: String?,

    val github: String?,
    val linkedin: String?,
    val instagram: String?,

    val memberSince: String,


    // Preferences ----------------------------------------------------

    val showPhone: Boolean,
    val showSocials: Boolean

)