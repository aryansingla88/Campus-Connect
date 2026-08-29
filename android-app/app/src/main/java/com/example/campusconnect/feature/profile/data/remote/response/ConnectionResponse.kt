package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ConnectionResponse(

    // User -------------------------------------------------------------

    @SerializedName("user_id")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("full_name")
    val fullName: String,

    @SerializedName("avatar_url")
    val avatarUrl: String?,

    @SerializedName("avatar_color")
    val avatarColor: String,

    @SerializedName("course_name")
    val courseName: String,

    @SerializedName("admission_year")
    val admissionYear: Int,

    // Relationship -----------------------------------------------------

    @SerializedName("status")
    val status: String,         // not_connected | pending | connected
)