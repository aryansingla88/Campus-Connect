package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Body for PATCH /events/{id}. id is in the URL path, not the body.
 * All fields nullable so only changed fields need to be sent.
 * Mirrors Event.kt exactly.
 */
data class UpdateEventRequest(

    @SerializedName("description")
    val description           : String?  = null,

    @SerializedName("latitude")
    val latitude              : Double?  = null,

    @SerializedName("longitude")
    val longitude             : Double?  = null,

    // TODO(DB):
// Temporary map UI coordinates.
// Remove from API once map uses only latitude/longitude.

    @SerializedName("x_ratio")
    val xRatio                : Float?   = null,

    @SerializedName("y_ratio")
    val yRatio                : Float?   = null,

    @SerializedName("date")
    val date                  : String?  = null,

    @SerializedName("start_time")
    val startTime             : String?  = null,

    @SerializedName("end_time")
    val endTime               : String?  = null,

    // TODO(DB):
// Consider removing is_poster.
// poster_url already implies poster existence.

    @SerializedName("is_poster")
    val isPoster              : Boolean? = null,

    @SerializedName("poster_url")
    val posterUrl             : String?  = null,

    // TODO(DB):
// Review if visibility_value is actually needed.
// May be replaced by club_id later.

    @SerializedName("visibility_type")
    val visibilityType        : String?  = null,

    @SerializedName("visibility_value")
    val visibilityValue       : String?  = null,

    @SerializedName("venue")
    val venue                 : String?  = null,

    // TODO(DB):
// Review if visibility_value is actually needed.
// May be replaced by club_id later.

    @SerializedName("status")
    val status                : String?  = null   // LIVE | PAST | UPCOMING
)