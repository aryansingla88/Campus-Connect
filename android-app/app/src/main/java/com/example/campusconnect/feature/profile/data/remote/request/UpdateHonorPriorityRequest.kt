package com.example.campusconnect.feature.profile.data.remote.request

import com.google.gson.annotations.SerializedName

data class UpdateHonorPriorityRequest(
    @SerializedName("honor_id")     val honorId  : Int,
    @SerializedName("priority")     val priority : Int,
)