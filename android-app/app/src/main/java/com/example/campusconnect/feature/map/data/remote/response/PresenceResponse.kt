package com.example.campusconnect.feature.map.data.remote.response


data class PresenceResponse(

    val latitude: Double,

    val longitude: Double,

    val insideCampus: Boolean,

    val visibility: String,

    val lastUpdated: String,

    val gender: String? = null
)