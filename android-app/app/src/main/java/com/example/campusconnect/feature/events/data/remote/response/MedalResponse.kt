package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class MedalResponse(

    @SerializedName("title")
    val title: String,

    @SerializedName("awarded")
    val awarded: Boolean,

    @SerializedName("recipient")
    val recipient: MedalRecipientResponse?
)