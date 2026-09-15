package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class EventHostResponse(

    val userId: Int,


    val fullName: String,


    val avatarUrl: String? = null,


    val role: String? = null
)