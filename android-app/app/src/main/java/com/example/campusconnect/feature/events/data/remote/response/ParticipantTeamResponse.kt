package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

/** Mirrors ParticipantTeam.kt exactly. */
data class ParticipantTeamResponse(
    @SerializedName("id")
    val id       : Int,

    @SerializedName("name")
    val name     : String,

    @SerializedName("members")
    val members  : List<TeamMemberResponse>
)