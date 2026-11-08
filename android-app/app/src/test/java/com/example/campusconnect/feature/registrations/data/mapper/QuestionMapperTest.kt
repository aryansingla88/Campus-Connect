package com.example.campusconnect.feature.registrations.data.mapper

import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantQuestionDto
import com.example.campusconnect.feature.registrations.data.remote.dto.ParticipantQuestionOptionDto
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldOptionResponse
import com.example.campusconnect.feature.registrations.data.remote.dto.RegistrationFieldResponse
import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.google.gson.Gson
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestionMapperTest {
    @Test
    fun participantDtoMapsScopeOptionsOrderAndBackendIdentity() {
        val dto = ParticipantQuestionDto(
            id = 41,
            fieldLabel = "Dietary requirements",
            fieldType = "MULTISELECT",
            required = true,
            placeholder = "Choose all that apply",
            fieldOrder = 7,
            individual = false,
            options = listOf(
                ParticipantQuestionOptionDto("Vegan", 2),
                ParticipantQuestionOptionDto("Vegetarian", 1),
            ),
        )

        val question = requireNotNull(dto.toRegistrationQuestionOrNull())

        assertEquals(41, question.backendId)
        assertEquals("backend:41", question.localId)
        assertEquals("Dietary requirements", question.label)
        assertEquals(QuestionType.Known.MULTISELECT, question.type)
        assertEquals(QuestionScope.TEAM, question.scope)
        assertEquals(listOf("Vegetarian", "Vegan"), question.options)
        assertEquals(7, question.fieldOrder)
        assertTrue(question.isRequired)
    }

    @Test
    fun registrationFieldMapperPreservesFileAndUnknownTypesAndSeparatesLocalId() {
        val fileQuestion = fieldResponse(5, "FILE")
            .toRegistrationQuestion()
            .copy(localId = "editor-key")
        val unknownQuestion = fieldResponse(6, "NEW_SERVER_TYPE")
            .toRegistrationQuestion()

        assertEquals(5, fileQuestion.backendId)
        assertEquals("editor-key", fileQuestion.localId)
        assertEquals(QuestionType.Known.FILE, fileQuestion.type)
        assertEquals(QuestionType.Unknown("NEW_SERVER_TYPE"), unknownQuestion.type)
    }

    @Test
    fun requestMapperKeepsApiShapeAndOmitsOnlyUnpersistedBackendId() {
        val question = com.example.campusconnect.feature.registrations.model.RegistrationQuestion(
            backendId = null,
            localId = "client-only-uuid",
            label = "Project idea",
            type = QuestionType.Unknown("CUSTOM_TEXT"),
            isRequired = true,
            placeholder = "Describe it",
            options = listOf("A", "B"),
            scope = QuestionScope.TEAM,
            fieldOrder = 3,
        )
        val payload = Gson().toJson(question.toRegistrationFieldRequest())

        assertFalse(payload.contains("client-only-uuid"))
        // Gson's Retrofit converter omits null-valued fields by default, preserving the existing API payload.
        assertFalse(payload.contains("\"id\""))
        assertTrue(payload.contains("\"fieldLabel\":\"Project idea\""))
        assertTrue(payload.contains("\"fieldType\":\"CUSTOM_TEXT\""))
        assertTrue(payload.contains("\"individual\":false"))
        assertTrue(payload.contains("\"optionOrder\":0"))
        assertTrue(payload.contains("\"optionValue\":\"B\""))
    }

    @Test
    fun missingParticipantQuestionIdIsNotPresentedAsAQuestion() {
        val question = ParticipantQuestionDto(
            id = null,
            fieldLabel = "No backend identity",
            fieldType = "TEXT",
            required = false,
            placeholder = null,
            fieldOrder = null,
            individual = true,
            options = null,
        )

        assertNull(question.toRegistrationQuestionOrNull())
    }

    private fun fieldResponse(id: Int, type: String) = RegistrationFieldResponse(
        id = id,
        fieldLabel = "Question",
        fieldType = type,
        required = false,
        placeholder = null,
        fieldOrder = id,
        individual = true,
        options = listOf(RegistrationFieldOptionResponse(1, "First", 0)),
    )
}
