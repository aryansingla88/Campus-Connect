package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.mapengine.model.MapMarker
import com.example.campusconnect.feature.map.mapengine.model.MarkerSize
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.google.gson.annotations.SerializedName

data class UserMapRes(

    val userId: Int,

    val username: String,

    val latitude: Double,

    val longitude: Double,

    val insideCampus: Boolean? = null,

    val gender: String? = null
)

fun UserMapRes.toMarker(): MapMarker {
    return MapMarker(
        id = "USER_$userId",
        sourceId = userId,
        type = MarkerType.USER,
        latitude = latitude,
        longitude = longitude,
        label = username,
        userId = userId,
        insideCampus = insideCampus,
        gender = gender?.lowercase(),
        size = MarkerSize.MEDIUM
    )
}