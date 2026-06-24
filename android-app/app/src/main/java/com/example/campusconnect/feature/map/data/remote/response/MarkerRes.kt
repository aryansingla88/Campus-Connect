package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class MarkerRes(
    @SerializedName("entityId")
    val entityId: String,

    @SerializedName("type")
    val type: String,

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("label")
    val label: String,

    @SerializedName("gender")
    val gender: String? = null,

    @SerializedName("size")
    val size: String? = null,

    @SerializedName("priority")
    val priority: Int? = null,

    @SerializedName("isHighlighted")
    val isHighlighted: Boolean? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null
)