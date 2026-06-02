package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

// user_honor joined with honor_items
data class HonorResponse(
    @SerializedName("honor_id")         val honorId       : Int,
    @SerializedName("title")            val title         : String,
    @SerializedName("subtitle")         val subtitle      : String?,
    @SerializedName("color")            val color         : String,
    @SerializedName("condition")        val condition     : String?,
    @SerializedName("event_id")         val eventId       : Int?,     // null if awarded manually
    @SerializedName("priority")         val priority      : Int,      // user_honor.priority
    @SerializedName("awarded_at")       val awardedAt     : String,
)

// honor_points + RANK() OVER (ORDER BY points DESC)
data class HonorLeaderboardEntryResponse(
    @SerializedName("user_id")          val userId        : Int,
    @SerializedName("username")         val username      : String,
    @SerializedName("full_name")        val fullName      : String,
    @SerializedName("avatar_url")       val avatarUrl     : String?,
    @SerializedName("avatar_color")     val avatarColor   : String,
    @SerializedName("points")           val points        : Int,
    @SerializedName("honor_rank")       val honorRank     : Int,      // derived RANK()
    @SerializedName("is_me")            val isMe          : Boolean,
)