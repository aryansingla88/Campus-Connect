package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventHistoryResponse(
    @SerializedName("live")
    val live: List<EventHistoryItemResponse>,

    @SerializedName("upcoming")
    val upcoming: List<EventHistoryItemResponse>,

    @SerializedName("past")
    val past: List<EventHistoryItemResponse>
)