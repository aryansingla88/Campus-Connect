package com.example.campusconnect.feature.profile.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @SerializedName("course_id")
    val courseId: String,

    @SerializedName("branch")
    val branch: String?,

    @SerializedName("admission_year")
    val admissionYear: Int,

    @SerializedName("hostel")
    val hostel: String,

    @SerializedName("hometown")
    val hometown: String?,

    @SerializedName("gender")
    val gender: String?,

    @SerializedName("dob")
    val dob: String?,

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("github")
    val github: String?,

    @SerializedName("linkedin")
    val linkedin: String?,

    @SerializedName("instagram")
    val instagram: String?
)