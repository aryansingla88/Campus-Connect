package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldOptionRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldRequest
import com.example.campusconnect.feature.registrations.model.FieldType
import com.example.campusconnect.feature.registrations.model.FormField
import com.example.campusconnect.feature.registrations.model.RegistrationQuestionContext

object RegistrationFieldMapper {
    fun toModel(response: RegistrationFieldResponse): FormField {
        val matchingType = FieldType.entries.firstOrNull { it.name.equals(response.fieldType, ignoreCase = true) }
        return FormField(
            id = response.id,
            backendId = response.id,
            label = response.fieldLabel,
            fieldType = matchingType ?: FieldType.TEXT,
            rawFieldType = if (matchingType == null) response.fieldType else null,
            placeholder = response.placeholder.orEmpty(),
            isRequired = response.required,
            options = response.options.sortedBy { it.optionOrder }.map { it.optionValue },
            individual = response.individual,
            fieldOrder = response.fieldOrder,
            // The backend schema exposes the shared/team question bucket as
            // individual=false. Leaders and solo participants answer it; ordinary
            // members don't re-answer fields owned by their team's leader.
            registrationContext = if (response.individual) RegistrationQuestionContext.INDIVIDUAL
                else RegistrationQuestionContext.TEAM_CREATION,
        )
    }

    fun toRequest(field: FormField, order: Int): RegistrationFieldRequest = RegistrationFieldRequest(
        id = field.backendId,
        fieldLabel = field.label,
        fieldType = field.rawFieldType ?: field.fieldType.name,
        required = field.isRequired,
        placeholder = field.placeholder.takeIf(String::isNotBlank),
        fieldOrder = order,
        individual = field.individual,
        options = field.options.mapIndexed { index, value ->
            RegistrationFieldOptionRequest(optionValue = value, optionOrder = index)
        },
    )
}
