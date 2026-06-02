package com.example.campusconnect.feature.profile.data.remote.response

import com.google.gson.annotations.SerializedName

data class ClubResponse(
    @SerializedName("club_id")          val clubId        : Int,
    @SerializedName("name")             val name          : String,
    @SerializedName("description")      val description   : String?,
    @SerializedName("logo_url")         val logoUrl       : String?,
    @SerializedName("icon_color")       val iconColor     : String,   // server-assigned hex
    @SerializedName("member_count")     val memberCount   : Int,      // COUNT of approved members
    @SerializedName("mem_status")       val memStatus     : String?,  // "pending"|"approved"|null
    @SerializedName("role")             val role          : String?,  // "admin"|"mod"|"member"|null
)