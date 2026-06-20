package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class CategoryRes(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String
)