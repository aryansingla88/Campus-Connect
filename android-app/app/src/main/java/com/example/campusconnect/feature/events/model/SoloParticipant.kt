package com.example.campusconnect.feature.events.model

data class SoloParticipant(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val avatarUrl: String? = null
)
