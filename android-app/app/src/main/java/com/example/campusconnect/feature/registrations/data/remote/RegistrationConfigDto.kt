package com.example.campusconnect.feature.registrations.data.remote

import com.google.gson.annotations.SerializedName

data class RegistrationConfigRequest(
    @SerializedName("minTeamMembers") val minTeamMembers: Int,
    @SerializedName("maxTeamMembers") val maxTeamMembers: Int,
    @SerializedName("quickFieldMask") val quickFieldMask: Long,
    @SerializedName("registrationCreatedAt") val registrationCreatedAt: String? = null,
    @SerializedName("registrationEndAt") val registrationEndAt: String? = null,
)

data class QuickFieldsConfigRequest(
    @SerializedName("quickFieldMask") val quickFieldMask: Long,
)

data class RegistrationConfigResponse(
    @SerializedName("eventId") val eventId: Int,
    @SerializedName("minTeamMembers") val minTeamMembers: Int,
    @SerializedName("maxTeamMembers") val maxTeamMembers: Int,
    @SerializedName("quickFieldMask") val quickFieldMask: Long = 0L,
    @SerializedName("registrationCreatedAt") val registrationCreatedAt: String? = null,
    @SerializedName("registrationEndAt") val registrationEndAt: String? = null,
)
