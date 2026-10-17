package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class MedalRecipientResponse(

    @SerializedName("honorId")
    val honorId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("team")
    val team: Boolean
)