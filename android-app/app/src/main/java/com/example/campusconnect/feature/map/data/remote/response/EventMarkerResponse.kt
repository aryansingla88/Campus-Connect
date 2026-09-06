package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventMarkerResponse(

    @SerializedName("id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("priority")
    val priority: Int? = null
)
