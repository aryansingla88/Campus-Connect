package com.example.campusconnect.feature.profile.data.remote.response

data class ClubResponse(

    val clubId: Int,

    val name: String,

    val logoUrl: String?,

    val memberCount: Int,

    val memberStatus: String?
)