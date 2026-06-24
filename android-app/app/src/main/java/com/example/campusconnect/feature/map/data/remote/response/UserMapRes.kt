package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserMapRes(
    @SerializedName("id")
    val id: String,

    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("course")
    val course: String? = null,

    @SerializedName("startYear")
    val startYear: Int? = null,

    @SerializedName("endYear")
    val endYear: Int? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("badges")
    val badges: List<String>? = null,

    @SerializedName("medals")
    val medals: List<Int>? = null,

    @SerializedName("mutualFriendsCount")
    val mutualFriendsCount: Int? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("connectionStatus")
    val connectionStatus: String? = null
)