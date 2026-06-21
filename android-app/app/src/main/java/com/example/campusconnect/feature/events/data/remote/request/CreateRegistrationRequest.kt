package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/** Mirrors Registration.kt — body for creating/publishing a registration form. */

data class CreateRegistrationRequest(
    @SerializedName("event_id")
    val eventId      : Int,

    @SerializedName("title")
    val title        : String,

    @SerializedName("is_published")
    val isPublished  : Boolean = false
)