package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class ShopResponse(

    val id: Int,

    val name: String,

    val type: String? = null,

    val category: String? = null,

    val description: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val openingTime: String? = null,

    val closingTime: String? = null,

    val isOpen: Boolean? = null,

    val phone: String? = null,

    val isActive: Boolean? = null
)