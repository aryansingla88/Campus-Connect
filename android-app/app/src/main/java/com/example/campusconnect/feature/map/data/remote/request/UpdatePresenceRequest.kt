package com.example.campusconnect.feature.map.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdatePresenceRequest(

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double
)