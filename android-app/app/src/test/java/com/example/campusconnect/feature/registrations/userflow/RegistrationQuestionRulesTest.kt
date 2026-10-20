package com.example.campusconnect.feature.registrations.userflow

import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import com.example.campusconnect.feature.registrations.ui.screens.RegistrationQuestionContext
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenConfig
import com.example.campusconnect.feature.registrations.userflow.model.RegistrationQuestionScreenKind
import com.example.campusconnect.feature.registrations.userflow.model.ParticipationMode
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionContext
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MAX_LENGTH
import com.example.campusconnect.feature.registrations.userflow.model.TEAM_NAME_MIN_LENGTH
import com.example.campusconnect.feature.registrations.userflow.model.teamNameLengthError
import com.example.campusconnect.feature.registrations.userflow.model.validationError
import com.example.campusconnect.feature.registrations.userflow.model.updateAnswer
import com.example.campusconnect.feature.registrations.userflow.model.validateAnswers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class RegistrationQuestionRulesTest {
    @Test fun hostFieldTypesAreTheParticipantFieldTypes() {
        assertEquals(setOf("TEXT", "NUMBER", "SELECT", "MULTISELECT", "DATE"), FieldType.entries.map { it.name }.toSet())
        assertEquals(FieldType.MULTISELECT, FormField(7, fieldType = FieldType.MULTISELECT).fieldType)
    }

    @Test fun requiredOptionalAndTypedHostFieldsUseSharedValidation() {
        assertEquals("This field is required", FormField(1, isRequired = true).validationError(" "))
        assertNull(FormField(1).validationError(" "))
        assertEquals("Enter a valid number", FormField(2, fieldType = FieldType.NUMBER).validationError("one"))
        assertEquals("Choose an available option", FormField(3, fieldType = FieldType.SELECT, options = listOf("A", "B")).validationError("C"))
        assertEquals("Choose only available options", FormField(4, fieldType = FieldType.MULTISELECT, options = listOf("A", "B")).validationError("A,C"))
        assertEquals("Choose a valid date", FormField(5, fieldType = FieldType.DATE).validationError("tomorrow"))
        assertNull(FormField(5, fieldType = FieldType.DATE).validationError("2026-10-20"))
        assertNull(FormField(6, isRequired = true, readOnly = true).validationError(""))
    }

    @Test fun questionAnswerUpdatesAndSubmitValidationShareOneRuleSet() {
        val fields = listOf(
            FormField(21, label = "Name", isRequired = true),
            FormField(22, label = "Count", fieldType = FieldType.NUMBER),
        )
        val updated = fields.updateAnswer(emptyMap(), emptyMap(), 21, " ")
        assertEquals(" ", updated.answers[21])
        assertEquals("This field is required", updated.validationErrors[21])

        val corrected = fields.updateAnswer(updated.answers, updated.validationErrors, 21, "Riya")
        assertNull(corrected.validationErrors[21])
        assertEquals(mapOf(22 to "Enter a valid number"), fields.validateAnswers(corrected.answers + (22 to "many")))
    }

    @Test fun teamNameRequiresSensibleLengthAndSubmissionContextKeepsTeamName() {
        assertEquals("Use at least $TEAM_NAME_MIN_LENGTH characters.", teamNameLengthError("xy"))
        assertNull(teamNameLengthError("Team"))
        assertEquals("Use no more than $TEAM_NAME_MAX_LENGTH characters.", teamNameLengthError("x".repeat(TEAM_NAME_MAX_LENGTH + 1)))
        val context = SubmissionContext(
            eventId = 1,
            eventName = "Demo",
            mode = ParticipationMode.CREATE_TEAM,
            teamName = "Bright Sparks",
        )
        assertEquals("Bright Sparks", context.teamName)
    }

    @Test fun individualAndTeamQuestionFieldsUseSameFormModelWithTypedContext() {
        val individual = FormField(
            1,
            label = "Name",
            registrationContext = RegistrationQuestionContext.INDIVIDUAL,
        )
        val sharedTeam = FormField(
            2,
            label = "Goals",
            individual = false,
            registrationContext = RegistrationQuestionContext.TEAM_COMMON,
        )
        val creationTeam = FormField(
            3,
            label = "Domain",
            individual = false,
            registrationContext = RegistrationQuestionContext.TEAM_CREATION,
        )

        assertEquals(FormField::class, individual::class)
        assertEquals(FormField::class, sharedTeam::class)
        assertEquals(FormField::class, creationTeam::class)
        assertEquals(RegistrationQuestionContext.INDIVIDUAL, individual.registrationContext)
        assertEquals(RegistrationQuestionContext.TEAM_COMMON, sharedTeam.registrationContext)
        assertEquals(RegistrationQuestionContext.TEAM_CREATION, creationTeam.registrationContext)
        val individualScreen = RegistrationQuestionScreenConfig(
            RegistrationQuestionScreenKind.INDIVIDUAL, "Participant details", 1, "Next",
        )
        val teamScreen = RegistrationQuestionScreenConfig(
            RegistrationQuestionScreenKind.TEAM, "Team questions", 3, "Submit Registration",
        )
        assertEquals(1, individualScreen.progressStep)
        assertEquals(3, teamScreen.progressStep)
        assertEquals(RegistrationQuestionScreenKind.INDIVIDUAL, individualScreen.kind)
        assertEquals(RegistrationQuestionScreenKind.TEAM, teamScreen.kind)
    }
}
