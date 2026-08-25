package com.example.campusconnect.feature.map.data.remote.response

import com.example.campusconnect.feature.map.mapengine.MapMarker
import com.example.campusconnect.feature.map.mapengine.MarkerSize
import com.example.campusconnect.feature.map.mapengine.MarkerType
import com.google.gson.annotations.SerializedName

data class UserMapRes(
    @SerializedName("userId")
    val userId: Int,

    @SerializedName("username")
    val username: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("insideCampus")
    val insideCampus: Boolean? = null,

    @SerializedName("gender")
    val gender: String? = null
)

fun UserMapRes.toMarker(): MapMarker {
    return MapMarker(
        id = "USER_$userId",
        sourceId = userId.toString(),
        type = MarkerType.USER,
        latitude = latitude,
        longitude = longitude,
        label = username,
        userId = userId,
        insideCampus = insideCampus,
        gender = gender,
        size = MarkerSize.MEDIUM
    )
}