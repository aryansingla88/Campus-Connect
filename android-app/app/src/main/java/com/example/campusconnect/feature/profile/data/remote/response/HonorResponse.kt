package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class HonorResponse(

    // Honor Item -------------------------------------------------------

    @SerializedName("honor_id")
    val honorId: String,

    @SerializedName("type")
    val type: String,                 // badge | medal

    @SerializedName("title")
    val title: String,

    @SerializedName("subtitle")
    val subtitle: String?,

    @SerializedName("icon_url")
    val iconUrl: String?,

    @SerializedName("condition")
    val condition: String?,

    @SerializedName("event_id")
    val eventId: String?,

    // User Honor -------------------------------------------------------

    @SerializedName("priority")
    val priority: Int,

    @SerializedName("awarded_at")
    val awardedAt: String,
)