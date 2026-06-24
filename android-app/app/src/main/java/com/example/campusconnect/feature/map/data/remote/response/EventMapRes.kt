package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventMapRes(
    @SerializedName("id")
    val id: String,

    @SerializedName("title")
    val title: String,

    @SerializedName("hostName")
    val hostName: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("venue")
    val venue: String? = null,

    @SerializedName("date")
    val date: String? = null,

    @SerializedName("time")
    val time: String? = null,

    @SerializedName("startTime")
    val startTime: String? = null,

    @SerializedName("endTime")
    val endTime: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("posterUrl")
    val posterUrl: String? = null,

    @SerializedName("priority")
    val priority: Int? = null,

    @SerializedName("isJoined")
    val isJoined: Boolean? = null,

    @SerializedName("isReminderEnabled")
    val isReminderEnabled: Boolean? = null,

    @SerializedName("membersCount")
    val membersCount: Int? = null
)