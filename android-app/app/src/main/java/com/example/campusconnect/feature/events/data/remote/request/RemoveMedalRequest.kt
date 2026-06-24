package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/** Body for un-awarding a medal — matches FakeMedalService.removeAward(eventId, medalType). */
data class RemoveMedalRequest(
    @SerializedName("medal_type")
    val medalType  : String   // GOLD | SILVER | BRONZE
)