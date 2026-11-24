package com.example.campusconnect.feature.profile.model


data class PublicUserProfile(

    // Identity
    val userId: Int = 0,
    val fullName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,

    // Academic
    val programName: String = "",
    val academicYear: String = "",
    val batch: String = "",
    val hostelId: Int? = null,
    val clearHostel: Boolean = false,
    val hometown: String = "",

    // Personal
    val gender: String = "",
    val dob: String = "",
    val age: Int? = null,

    // Contact
    val phone: String = "",
    val email: String = "",

    // Social
    val github: String = "",
    val linkedin: String = "",
    val instagram: String = "",

    // Metadata
    val memberSince: String = "",
    val relationshipStatus: ConnectionStatus? = null,

    // Visibility
    val showPhone: Boolean = false,
    val showSocials: Boolean = true,
    val showHometown: Boolean = false,
    val showGender: Boolean = false,
    val showAge: Boolean = false,
    val showHonor: Boolean = false,
    val headerBadges: List<ProfileHonor> = emptyList(),
    val headerMedals: List<ProfileHonor> = emptyList(),
)
