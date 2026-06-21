package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

/** Mirrors SoloParticipant.kt exactly. initials is computed client-side, not sent. */
data class SoloParticipantResponse(
    @SerializedName("id")
    val id        : Int,

    @SerializedName("name")
    val name      : String,

    @SerializedName("subtitle")
    val subtitle  : String
)