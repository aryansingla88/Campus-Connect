package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

/**
 * Server response for a single event.
 * Mirrors Event.kt field-for-field, plus the server-generated id.
 * Deserialize this, then map 1:1 into your existing Event data class.
 */

data class EventResponse(
    @SerializedName("id")
    val id                     : Int,

    @SerializedName("title")
    val title                  : String,

    @SerializedName("description")
    val description            : String? = null,

    @SerializedName("latitude")
    val latitude               : Double,

    @SerializedName("longitude")
    val longitude              : Double,

     // TODO(DB):
    // Temporary map UI coordinates.
    // Remove once backend sends real latitude/longitude only.

    @SerializedName("x_ratio")
    val xRatio                 : Float = 0.5f,

    @SerializedName("y_ratio")
    val yRatio                 : Float = 0.5f,

    @SerializedName("date")
    val date                   : String = "",

    @SerializedName("start_time")
    val startTime              : String = "",

    @SerializedName("end_time")
    val endTime                : String? = null,

    @SerializedName("created_by")
    val createdBy              : Int = 0,

    @SerializedName("club_name")
    val clubName               : String = "",

    @SerializedName("is_poster")
    val isPoster               : Boolean = false,

    @SerializedName("poster_url")
    val posterUrl              : String? = null,

    @SerializedName("category")
    val category               : String = "",

    @SerializedName("visibility_type")
    val visibilityType         : String = "",

    // TODO(DB):
// Temporary map UI coordinates.
// Remove once backend sends real latitude/longitude only.

    @SerializedName("visibility_value")
    val visibilityValue        : String = "",

    @SerializedName("registration_required")
    val registrationRequired   : Boolean = false,

    @SerializedName("registration_link")
    val registrationLink       : String = "",

    @SerializedName("in_app_registration")
    val inAppRegistration      : Boolean = false,

    @SerializedName("venue")
    val venue                  : String = "",

    @SerializedName("enable_chat")
    val enableChat             : Boolean = false,

    // TODO(DB):
// Backend values should match EventStatus
// LIVE | PAST | UPCOMING

    @SerializedName("status")
    val status                 : String = "UPCOMING"   // LIVE | PAST | UPCOMING
)