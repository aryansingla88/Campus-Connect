package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class PresenceResponse(

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("insideCampus")
    val insideCampus: Boolean,

    @SerializedName("visibility")
    val visibility: String,

    @SerializedName("lastUpdated")
    val lastUpdated: String,

    @SerializedName("gender")
    val gender: String? = null
)