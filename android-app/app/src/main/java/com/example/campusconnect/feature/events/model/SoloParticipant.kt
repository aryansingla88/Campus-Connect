package com.example.campusconnect.feature.events.model

data class SoloParticipant(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val initials : String = name
        .split(" ").take(2).joinToString("") { it.take(1).uppercase() }
)