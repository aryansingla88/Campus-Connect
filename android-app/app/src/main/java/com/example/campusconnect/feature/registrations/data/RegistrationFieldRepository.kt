package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.events.data.remote.EventsApi
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldOptionRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField

class RegistrationFieldRepository(
    private val api: EventsApi = RetrofitClient.eventsApi,
) {
    suspend fun load(eventId: Int): Result<List<FormField>> = try {
        val response = api.getRegistrationFields(eventId)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not load registration questions (HTTP ${response.code()})."
            ))
        } else {
            val data = body.data
            if (data == null) Result.failure(IllegalStateException("The server returned no registration questions."))
            else Result.success(data.sortedBy { it.fieldOrder }.map { it.toFormField() })
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    /** The PUT endpoint updates fields with IDs, creates fields without IDs, and soft-deletes omitted IDs. */
    suspend fun save(eventId: Int, fields: List<FormField>): Result<List<FormField>> = try {
        val ordered = fields.withIndex().sortedWith(
            compareBy<IndexedValue<FormField>> { if (it.value.backendId == null) 1 else 0 }
                .thenBy { if (it.value.backendId == null) it.index else it.value.fieldOrder }
        ).mapIndexed { order, entry -> entry.value.toRequest(order) }
        val response = api.updateRegistrationFields(eventId, ordered)
        val body = response.body()
        if (!response.isSuccessful || body?.success != true || body.data == null) {
            Result.failure(IllegalStateException(
                body?.message?.takeIf(String::isNotBlank)
                    ?: "Could not save registration questions (HTTP ${response.code()})."
            ))
        } else {
            Result.success(body.data.sortedBy { it.fieldOrder }.map { it.toFormField() })
        }
    } catch (error: Exception) {
        Result.failure(error)
    }

    private fun RegistrationFieldResponse.toFormField(): FormField {
        val matchingType = FieldType.entries.firstOrNull { it.name.equals(fieldType, ignoreCase = true) }
        return FormField(
            id = id,
            backendId = id,
            label = fieldLabel,
            fieldType = matchingType ?: FieldType.TEXT,
            rawFieldType = if (matchingType == null) fieldType else null,
            placeholder = placeholder.orEmpty(),
            isRequired = required,
            options = options.sortedBy { it.optionOrder }.map { it.optionValue },
            individual = individual,
            fieldOrder = fieldOrder,
        )
    }

    private fun FormField.toRequest(order: Int) = RegistrationFieldRequest(
        id = backendId,
        fieldLabel = label,
        fieldType = rawFieldType ?: fieldType.name,
        required = isRequired,
        placeholder = placeholder.takeIf(String::isNotBlank),
        fieldOrder = order,
        individual = individual,
        options = options.mapIndexed { index, value ->
            RegistrationFieldOptionRequest(optionValue = value, optionOrder = index)
        },
    )
}
