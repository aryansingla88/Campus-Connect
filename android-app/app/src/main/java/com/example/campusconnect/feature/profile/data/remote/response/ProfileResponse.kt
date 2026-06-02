package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("user_id")          val userId        : Int,
    @SerializedName("username")         val username      : String,
    @SerializedName("email")            val email         : String,
    @SerializedName("role")             val role          : String,

    // from user_profile
    @SerializedName("full_name")        val fullName      : String,
    @SerializedName("bio")              val bio           : String?,
    @SerializedName("avatar_url")       val avatarUrl     : String?,

    @SerializedName("course_id")        val courseId      : Int,
    @SerializedName("course_name")      val courseName    : String,    // joined from courses
    @SerializedName("branch")           val branch        : String?,
    @SerializedName("admission_year")   val admissionYear : Int,

    @SerializedName("hostel")           val hostel        : String,
    @SerializedName("hometown")         val hometown      : String?,

    @SerializedName("gender")           val gender        : String?,
    @SerializedName("dob")              val dob           : String?,   // "yyyy-MM-dd"

    @SerializedName("phone")            val phone         : String?,
    @SerializedName("github")           val github        : String?,
    @SerializedName("linkedin")         val linkedin      : String?,
    @SerializedName("instagram")        val instagram     : String?,

    @SerializedName("member_since")     val memberSince   : String,    // users.created_at

    // from user_preferences — merged for convenience
    @SerializedName("show_phone")       val showPhone     : Boolean,
    @SerializedName("show_socials")     val showSocials   : Boolean,

    // derived — RANK() OVER (ORDER BY points DESC)
    @SerializedName("honor_rank")       val honorRank     : Int?,
    @SerializedName("honor_points")     val honorPoints   : Int?,
)