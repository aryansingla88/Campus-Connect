package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class PreferencesResponse(
    @SerializedName("show_phone")           val showPhone         : Boolean,
    @SerializedName("show_socials")         val showSocials       : Boolean,
    @SerializedName("show_presence")        val showPresence      : Boolean,
    @SerializedName("notify_connections")   val notifyConnections : Boolean,
    @SerializedName("notify_events")        val notifyEvents      : Boolean,
    @SerializedName("notify_posts")         val notifyPosts       : Boolean,
    @SerializedName("theme")                val theme             : String,
)