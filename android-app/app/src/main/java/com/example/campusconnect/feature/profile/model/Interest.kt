package com.example.campusconnect.feature.profile.model

data class Interest(
    val interestId : Int,
    val label      : String,
    val category   : String = "",
)