package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventHostResponse(
    @SerializedName("userId")
    val userId: Int, // Int ID to match Backend HostPreview

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("role")
    val role: String? = null
)