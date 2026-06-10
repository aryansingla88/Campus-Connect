package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileStatsResponse(
    @SerializedName("connection_count")
    val connectionCount: Int,

    @SerializedName("honor_count")
    val honorCount: Int,

    @SerializedName("club_count")
    val clubCount: Int,

    @SerializedName("interest_count")
    val interestCount: Int,
)