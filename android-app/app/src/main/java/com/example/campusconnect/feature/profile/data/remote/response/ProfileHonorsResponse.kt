package com.example.campusconnect.feature.profile.data.remote.response

data class ProfileHonorsResponse(

    val honorRank: Int,

    val badges: List<HonorResponse>,

    val medals: List<HonorResponse>,
)