package com.example.campusconnect.feature.events.model

data class ParticipantTeam(
    val id      : Int,
    val name    : String,
    val members : List<TeamMember>
)