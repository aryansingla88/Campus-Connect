package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Mirrors MedalAward.kt exactly.
 * medalType is sent as its enum name string: "GOLD" | "SILVER" | "BRONZE"
 * (matches MedalType.entries.name in Kotlin).
 */

data class MedalAwardResponse(
    @SerializedName("event_id")
    val eventId            : Int,

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