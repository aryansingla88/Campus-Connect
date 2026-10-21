package com.example.campusconnect.feature.registrations.data.remote

import com.google.gson.annotations.SerializedName

data class RegistrationSubmissionResponse(
    @SerializedName("registrationId") val registrationId: Int?,
    @SerializedName("type") val type: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("teamCount") val teamCount: Int?,
    @SerializedName("email") val email: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("submittedAt") val submittedAt: String?,
)

data class RegistrationSubmissionDetailResponse(
    @SerializedName("teamAnswers") val teamAnswers: List<RegistrationAnswerDto>?,
    @SerializedName("members") val members: List<RegistrationMemberDto?>?,
)

data class RegistrationMemberDto(
    @SerializedName("registrationId") val registrationId: Int?,
    @SerializedName("userId") val userId: Int?,
    @SerializedName("quickFields") val quickFields: List<QuickFieldDto>?,
    @SerializedName("individualAnswers") val individualAnswers: List<RegistrationAnswerDto>?,
)

data class QuickFieldDto(
    @SerializedName("field") val field: String?,
    @SerializedName("value") val value: String?,
)

data class RegistrationAnswerDto(
    @SerializedName("fieldId") val fieldId: Int?,
    @SerializedName("fieldLabel") val fieldLabel: String?,
    @SerializedName("fieldType") val fieldType: String?,
    @SerializedName("answer") val answer: String?,
)
