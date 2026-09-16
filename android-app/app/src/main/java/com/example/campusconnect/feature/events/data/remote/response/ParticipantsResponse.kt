package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class ParticipantsResponse(

    @SerializedName("teams")
    val teams: List<ParticipantTeamResponse>,

    @SerializedName("soloParticipants")
    val soloParticipants: List<SoloParticipantResponse>,

    @SerializedName("totalParticipants")
    val totalParticipants: Int
)