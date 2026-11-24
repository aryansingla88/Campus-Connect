package com.example.campusconnect.feature.profile.data.remote.request

data class UpdateProfileRequest(

    val bio: String?,

    val hostelId: Int?,

    val clearHostel: Boolean,

    val hometown: String?,

    val phone: String?,

    val github: String?,

    val linkedin: String?,

    val instagram: String?
)
