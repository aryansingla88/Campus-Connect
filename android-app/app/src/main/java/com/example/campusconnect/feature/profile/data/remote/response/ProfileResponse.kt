package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(


    // Users table ----------------------------------------------------------

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("username")
    val username: String,

    @SerializedName("email")
    val email: String,


    // User Profile table----------------------------------------------------------

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("bio")
    val bio: String?,

    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @SerializedName("course_id")
    val courseId: String,

    @SerializedName("course_name")
    val courseName: String,

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
    val dob: String?,              // yyyy-MM-dd

    @SerializedName("phone")
    val phone: String?,

    @SerializedName("github")
    val github: String?,

    @SerializedName("linkedin")
    val linkedin: String?,

    @SerializedName("instagram")
    val instagram: String?,

    @SerializedName("member_since")
    val memberSince: String,


    // Preferences affecting profile display----------------------------------------------------------

    @SerializedName("show_phone")
    val showPhone: Boolean,

    @SerializedName("show_socials")
    val showSocials: Boolean,
)