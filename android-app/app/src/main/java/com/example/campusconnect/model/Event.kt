package com.example.campusconnect.model

data class Event(
    val id: Int,
    val title: String,
    val description: String? = null,
    val latitude: Double,
    val longitude: Double,
    val eventTime: String,
    val createdBy: Int
)