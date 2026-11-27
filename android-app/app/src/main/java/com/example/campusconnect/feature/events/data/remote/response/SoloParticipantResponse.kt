package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class SoloParticipantResponse(

    @SerializedName("registrationId")
    val registrationId: Int,

    @SerializedName("userId")
    val userId: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("admissionYear")
    val admissionYear: Int?,

    @SerializedName("academicStatus")
    val academicStatus: String? = null,

    @SerializedName("avatarUrl")
    val avatarUrl: String?
)
