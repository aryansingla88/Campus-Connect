package com.example.campusconnect.feature.events.model

data class TeamMember(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val avatarUrl: String? = null,
    val isLeader : Boolean = false,
)
