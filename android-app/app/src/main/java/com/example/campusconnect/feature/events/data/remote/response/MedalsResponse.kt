package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class MedalsResponse(

    @SerializedName("gold")
    val gold: MedalResponse,

    @SerializedName("silver")
    val silver: MedalResponse,

    @SerializedName("bronze")
    val bronze: MedalResponse,

    @SerializedName("awardedCount")
    val awardedCount: Int
)