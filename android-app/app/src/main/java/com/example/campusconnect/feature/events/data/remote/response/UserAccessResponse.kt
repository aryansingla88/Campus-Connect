package com.example.campusconnect.feature.events.data.remote.response

import com.google.gson.annotations.SerializedName

data class UserAccessResponse(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("courseId")
    val courseId: Int?,

    @SerializedName("admissionYear")
    val admissionYear: Int?
)