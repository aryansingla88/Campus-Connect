package com.example.campusconnect.feature.profile.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @SerializedName("hostel")
    val hostel: String?,

    @SerializedName("hometown")
    val hometown: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("github")
    val github: String?,

    @SerializedName("linkedin")
    val linkedin: String?,

    @SerializedName("instagram")
    val instagram: String?
)