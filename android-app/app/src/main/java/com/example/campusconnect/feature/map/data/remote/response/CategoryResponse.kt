package com.example.campusconnect.feature.map.data.remote.response

import com.google.gson.annotations.SerializedName

data class CategoryResponse(
    @SerializedName("id")
    val id: Int, // Changed String to Int

    @SerializedName("name")
    val name: String
)