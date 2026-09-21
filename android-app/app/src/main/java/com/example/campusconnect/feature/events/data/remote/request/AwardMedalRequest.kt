package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

data class AwardMedalRequest(

    @SerializedName("registrationId")
    val registrationId: Int,

    @SerializedName("medalType")
    val medalType: String
)