package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventHostRes(
    @SerializedName("userId")
    val userId: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String? = null
)