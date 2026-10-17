package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class MedalCandidateResponse(

    @SerializedName("registrationId")
    val registrationId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("admissionYear")
    val admissionYear: Int?,

    @SerializedName("team")
    val team: Boolean,

    @SerializedName("avatarUrl")
    val avatarUrl: String?
)