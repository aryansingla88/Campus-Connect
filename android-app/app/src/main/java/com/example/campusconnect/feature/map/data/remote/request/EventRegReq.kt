package com.example.campusconnect.feature.map.data.remote.request

import com.google.gson.annotations.SerializedName

data class EventRegReq(
    @SerializedName("answers")
    val answers: List<EventRegAnswerReq> = emptyList()
)

data class EventRegAnswerReq(
    @SerializedName("fieldId")
    val fieldId: Int, // Changed String to Int

    @SerializedName("value")
    val value: String
)