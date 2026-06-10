package com.example.campusconnect.core.network

import com.google.gson.annotations.SerializedName


data class ApiResponse<T>(
    @SerializedName("success") val success : Boolean,
    @SerializedName("data")    val data    : T?,
    @SerializedName("message") val message : String?,
)