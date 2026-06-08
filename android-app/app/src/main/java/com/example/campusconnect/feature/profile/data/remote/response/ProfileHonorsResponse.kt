package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ProfileHonorsResponse(

    @SerializedName("honor_rank")
    val honorRank: Int,

    @SerializedName("badges")
    val badges: List<HonorResponse>,

    @SerializedName("medals")
    val medals: List<HonorResponse>,
)