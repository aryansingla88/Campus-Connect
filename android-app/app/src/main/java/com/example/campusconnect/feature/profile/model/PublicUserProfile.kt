package com.example.campusconnect.feature.profile.model

data class PublicUserProfile(

    // Identity
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val initials: String = "",
    val bio: String = "",

    // Academic
    val course: String = "",
    val year: String = "",
    val hostel: String = "",
    val hometown: String = "",

    // Personal
    val gender: String = "",
    val age: Int = 0,

    // Contact
    val phone: String = "",
    val email: String = "",

    // Social
    val github: String = "",
    val linkedin: String = "",
    val instagram: String = "",

    // Metadata
    val memberSince: String = "",

    // Stats
    val connectionCount: Int = 0,
    val honorRank: Int = 0,
    val clubCount: Int = 0,
    val interestCount: Int = 0,

    // Visibility
    val showPhone: Boolean = false,
    val showSocials: Boolean = true,
)