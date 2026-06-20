package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class ShopRes(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("type")
    val type: String? = null,

    @SerializedName("category")
    val category: String? = null,

    @SerializedName("description")
    val description: String? = null,

    @SerializedName("latitude")
    val latitude: Double? = null,

    @SerializedName("longitude")
    val longitude: Double? = null,

    @SerializedName("openingTime")
    val openingTime: String? = null,

    @SerializedName("closingTime")
    val closingTime: String? = null,

    @SerializedName("isOpen")
    val isOpen: Boolean? = null,

    @SerializedName("phone")
    val phone: String? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null
)