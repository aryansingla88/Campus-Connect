package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class InterestResponse(

    @SerializedName("interest_id")
    val interestId: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("category")
    val category: String,
)