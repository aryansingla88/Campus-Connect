package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class ParticipantTeamResponse(

    @SerializedName("teamId")
    val teamId: Int,

    @SerializedName("teamName")
    val teamName: String,

    @SerializedName("leaderName")
    val leaderName: String?,

    @SerializedName("memberCount")
    val memberCount: Int,

    @SerializedName("members")
    val members: List<TeamMemberResponse>
)