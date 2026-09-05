package com.example.campusconnect.feature.events.data.remote.response

data class EventResponse(

    // =========================
    // Backend supported fields
    // =========================

    val id: Int,

    val title: String,

    val description: String?,

    val latitude: Double,

    val longitude: Double,

    val startTime: String,

    val endTime: String,

    val createdBy: Int,

    val clubId: Int?,

    val hostName: String,

    val venue: String,

    val visibilityType: String,

    val visibilityValue: String?,

    val registrationType: String,

    val registrationLink: String?,

    val approvalStatus: String,

    val eventState: String,

    val priority: Int,

    val posterUrl: String? = null,

    val categoryId: Int?,

    val categoryName: String?
)