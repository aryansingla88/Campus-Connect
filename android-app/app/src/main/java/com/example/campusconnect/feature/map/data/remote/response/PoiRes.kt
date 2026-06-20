package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class PoiRes(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("iconType")
    val iconType: String? = null,

    @SerializedName("visibility")
    val visibility: Boolean? = null
)