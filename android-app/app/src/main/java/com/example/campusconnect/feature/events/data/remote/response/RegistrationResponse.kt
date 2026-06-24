package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Mirrors Registration.kt exactly:
 *   data class Registration(
 *       val eventId: Int,
 *       val title: String,
 *       val isPublished: Boolean,
 *   )
 */
data class RegistrationResponse(
    @SerializedName("event_id")
    val eventId      : Int,

    @SerializedName("title")
    val title        : String,

    @SerializedName("is_published")
    val isPublished  : Boolean
)