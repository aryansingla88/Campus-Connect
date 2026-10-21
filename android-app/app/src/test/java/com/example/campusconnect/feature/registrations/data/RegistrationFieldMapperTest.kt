package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.data.mapper.RegistrationFieldMapper
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldOptionRequest
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldOptionResponse
import com.example.campusconnect.feature.registrations.data.remote.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.model.FieldType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestionContext
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationFieldMapperTest {
    @Test fun responseMappingSortsOptionsAndPreservesUnknownBackendFieldTypes() {
        val known = RegistrationFieldMapper.toModel(
            RegistrationFieldResponse(
                id = 8,
                fieldLabel = "Area",
                fieldType = "select",
                required = true,
                placeholder = null,
                fieldOrder = 2,
                individual = false,
                options = listOf(
                    RegistrationFieldOptionResponse(2, "Second", 1),
                    RegistrationFieldOptionResponse(1, "First", 0),
                ),
            ),
        )
        assertEquals(FieldType.SELECT, known.fieldType)
        assertEquals(listOf("First", "Second"), known.options)
        assertEquals(RegistrationQuestionContext.TEAM_CREATION, known.registrationContext)
        assertEquals("", known.placeholder)

        val unknown = RegistrationFieldMapper.toModel(
            RegistrationFieldResponse(9, "Upload", "FILE_UPLOAD", false, null, 3, true),
        )
        assertEquals(FieldType.TEXT, unknown.fieldType)
        assertEquals("FILE_UPLOAD", unknown.rawFieldType)
    }

    @Test fun requestMappingPreservesExplicitUnknownTypeAndUsesBackendJsonKeys() {
        val model = RegistrationFieldMapper.toModel(
            RegistrationFieldResponse(9, "Upload", "FILE_UPLOAD", false, "Add file", 0, true),
        )
        val request = RegistrationFieldMapper.toRequest(model, 4)
        assertEquals("FILE_UPLOAD", request.fieldType)
        assertEquals(4, request.fieldOrder)
        assertEquals(emptyList<RegistrationFieldOptionRequest>(), request.options)
        assertNull(RegistrationFieldMapper.toRequest(model.copy(placeholder = ""), 0).placeholder)

        val json = Gson().toJson(request)
        val fields = Gson().fromJson(json, Map::class.java)
        assertEquals("Upload", fields["fieldLabel"])
        assertEquals("FILE_UPLOAD", fields["fieldType"])
        assertEquals(false, fields["required"])
    }
}
