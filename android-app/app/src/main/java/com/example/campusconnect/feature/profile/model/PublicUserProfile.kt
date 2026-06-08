package com.example.campusconnect.feature.profile.model

import com.example.campusconnect.core.utils.toInitials

data class PublicUserProfile(

    // Identity
    val userId: String = "",
    val fullName: String = "",
    val username: String = "",
    val bio: String = "",
    val avatarUrl: String? = null,

    // Academic
    val course: String = "",
    val branch: String = "",
    val academicYear: String = "",
    val batch: String = "",
    val hostel: String = "",
    val hometown: String = "",

    // Personal
    val gender: String = "",
    val dob: String = "",

    // Contact
    val phone: String = "",
    val email: String = "",

    // Social
    val github: String = "",
    val linkedin: String = "",
    val instagram: String = "",

    // Metadata
    val memberSince: String = "",

    // Visibility
    val showPhone: Boolean = false,
    val showSocials: Boolean = true,
)