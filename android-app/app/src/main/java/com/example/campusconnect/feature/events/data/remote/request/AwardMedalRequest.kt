package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/** Mirrors MedalAward.kt — body for awarding a medal. */
data class AwardMedalRequest(
    @SerializedName("medal_type")
    val medalType          : String,   // GOLD | SILVER | BRONZE

    @SerializedName("recipient_id")
    val recipientId        : Int,

    @SerializedName("recipient_name")
    val recipientName      : String,

    @SerializedName("recipient_subtitle")
    val recipientSubtitle  : String,

    @SerializedName("is_team")
    val isTeam             : Boolean = false
)