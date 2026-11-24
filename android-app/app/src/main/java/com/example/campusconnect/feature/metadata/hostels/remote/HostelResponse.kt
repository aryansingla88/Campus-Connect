package com.example.campusconnect.feature.metadata.hostels.remote

data class HostelResponse(
    val id: Int,
    val hostel: String,
    val name: String,
    val active: Boolean
)
