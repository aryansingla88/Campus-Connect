package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ClubResponse(

    // Club -------------------------------------------------------------

    @SerializedName("club_id")
    val clubId: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("logo_url")
    val logoUrl: String?,

    @SerializedName("icon_color")
    val iconColor: String,

    // Membership -------------------------------------------------------

    @SerializedName("member_count")
    val memberCount: Int,

    @SerializedName("mem_status")
    val memStatus: String?,     // pending | approved | rejected | null

    @SerializedName("role")
    val role: String?,          // admin | mod | member | null
)