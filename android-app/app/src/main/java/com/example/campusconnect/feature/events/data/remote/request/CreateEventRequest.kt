package com.example.campusconnect.feature.events.data.remote.request

import com.google.gson.annotations.SerializedName

/**
 * Body for POST /events.
 * Mirrors Event.kt exactly — id and createdBy are excluded
 * (id is server-generated, createdBy comes from the auth token).
 */
data class CreateEventRequest(
    @SerializedName("title")
    val title                 : String,

    @SerializedName("description")
    val description           : String? = null,

    @SerializedName("latitude")
    val latitude              : Double,

    @SerializedName("longitude")
    val longitude             : Double,

    @SerializedName("x_ratio")
    val xRatio                : Float = 0.5f,

    @SerializedName("y_ratio")
    val yRatio                : Float = 0.5f,

    @SerializedName("date")
    val date                  : String = "",

    @SerializedName("start_time")
    val startTime             : String = "",

    @SerializedName("end_time")
    val endTime               : String? = null,

    @SerializedName("club_name")
    val clubName              : String = "",

    @SerializedName("is_poster")
    val isPoster              : Boolean = false,

    @SerializedName("poster_url")
    val posterUrl             : String? = null,

    @SerializedName("category")
    val category              : String = "",

    @SerializedName("visibility_type")
    val visibilityType        : String = "",

    @SerializedName("visibility_value")
    val visibilityValue       : String = "",

    @SerializedName("registration_required")
    val registrationRequired  : Boolean = false,

    @SerializedName("registration_link")
    val registrationLink      : String = "",

    @SerializedName("in_app_registration")
    val inAppRegistration     : Boolean = false,

    @SerializedName("venue")
    val venue                 : String = "",

    @SerializedName("enable_chat")
    val enableChat            : Boolean = false,

    @SerializedName("status")
    val status                : String = "UPCOMING"   // EventStatus enum as string: LIVE | PAST | UPCOMING
)