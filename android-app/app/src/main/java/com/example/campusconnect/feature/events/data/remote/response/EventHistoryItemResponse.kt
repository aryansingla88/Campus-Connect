package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventHistoryItemResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("venue")
    val venue: String,

    @SerializedName("startTime")
    val startTime: String,

    @SerializedName("endTime")
    val endTime: String?,

    @SerializedName("posterUrl")
    val posterUrl: String?
)