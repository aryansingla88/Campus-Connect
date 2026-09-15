package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventMarkerResponse(


    val id: Int,


    val title: String,


    val latitude: Double? = null,


    val longitude: Double? = null,


    val priority: Int? = null
)
