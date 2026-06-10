package com.example.campusconnect.feature.profile.model

data class Interest(
    val interestId : String,
    val label      : String,
    val category   : String = "",
)