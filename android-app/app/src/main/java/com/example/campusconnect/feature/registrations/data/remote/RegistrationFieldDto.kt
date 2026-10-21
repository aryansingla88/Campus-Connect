package com.example.campusconnect.feature.registrations.data.remote

import com.google.gson.annotations.SerializedName

data class RegistrationFieldRequest(
    @SerializedName("id") val id: Int?,
    @SerializedName("fieldLabel") val fieldLabel: String,
    @SerializedName("fieldType") val fieldType: String,
    @SerializedName("required") val required: Boolean,
    @SerializedName("placeholder") val placeholder: String?,
    @SerializedName("fieldOrder") val fieldOrder: Int,
    @SerializedName("individual") val individual: Boolean,
    @SerializedName("options") val options: List<RegistrationFieldOptionRequest>,
)

data class RegistrationFieldOptionRequest(
    @SerializedName("optionValue") val optionValue: String,
    @SerializedName("optionOrder") val optionOrder: Int,
)

data class RegistrationFieldResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("fieldLabel") val fieldLabel: String,
    @SerializedName("fieldType") val fieldType: String,
    @SerializedName("required") val required: Boolean,
    @SerializedName("placeholder") val placeholder: String?,
    @SerializedName("fieldOrder") val fieldOrder: Int,
    @SerializedName("individual") val individual: Boolean,
    @SerializedName("options") val options: List<RegistrationFieldOptionResponse> = emptyList(),
)

data class RegistrationFieldOptionResponse(
    @SerializedName("id") val id: Int,
    @SerializedName("optionValue") val optionValue: String,
    @SerializedName("optionOrder") val optionOrder: Int,
)
