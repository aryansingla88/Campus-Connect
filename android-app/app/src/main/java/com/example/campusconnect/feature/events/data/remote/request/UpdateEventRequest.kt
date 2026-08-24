package com.example.campusconnect.feature.events.data.remote.request

data class UpdateEventRequest(

    // =========================
    // Backend-supported fields
    // =========================

    val description: String? = null,

    val latitude: Double? = null,

    val longitude: Double? = null,

    val startTime: String? = null,

    val endTime: String? = null,

    val venue: String? = null,

    val visibilityType: String? = null,

    val visibilityValue: String? = null,

    val registrationLink: String? = null,


    // =========================
    // TODO: Backend support
    // =========================



    // TODO: Derived from startTime
    val date: String? = null,

    // TODO: Backend currently uses clubId and does not
    // allow clubName to be updated directly
    val clubName: String? = null,

    // TODO: Add poster support to backend later
    val isPoster: Boolean? = null,

    // TODO: Add posterUrl to backend later
    val posterUrl: String? = null,

    // TODO: Add category to backend later
    val category: String? = null,


)