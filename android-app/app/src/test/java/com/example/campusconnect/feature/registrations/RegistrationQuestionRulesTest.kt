package com.example.campusconnect.feature.registrations

import com.example.campusconnect.feature.registrations.model.QuestionScope
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import com.example.campusconnect.feature.registrations.model.TEAM_NAME_MAX_LENGTH
import com.example.campusconnect.feature.registrations.model.TEAM_NAME_MIN_LENGTH
import com.example.campusconnect.feature.registrations.model.isMissing
import com.example.campusconnect.feature.registrations.model.teamNameLengthError
import com.example.campusconnect.feature.registrations.ui.mapper.toParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RegistrationQuestionRulesTest {
    @Test
    fun participantAndSharedQuestionsUseOneCanonicalModelWithExplicitScope() {
        val participant = RegistrationQuestion(backendId = 1, label = "Name", isRequired = true)
        val shared = RegistrationQuestion(
            backendId = 2,
            label = "Project idea",
            isRequired = true,
            scope = QuestionScope.TEAM,
        )

        assertEquals(QuestionScope.INDIVIDUAL, participant.scope)
        assertEquals(QuestionScope.TEAM, shared.scope)
        assertEquals("Name", participant.toParticipantQuestionUiModel()?.label)
        assertEquals(ParticipantQuestionKind.TEXT, shared.toParticipantQuestionUiModel()?.kind)
    }

    @Test
    fun requiredOptionalAndFileQuestionsValidateFromDomainRules() {
        val required = RegistrationQuestion(isRequired = true)
        val optional = RegistrationQuestion(isRequired = false)
        val requiredFile = RegistrationQuestion(type = QuestionType.Known.FILE, isRequired = true)

        assertTrue(required.isMissing("  "))
        assertFalse(required.isMissing("An answer"))
        assertFalse(optional.isMissing(null))
        assertTrue(requiredFile.isMissing(null, hasAttachment = false))
        assertFalse(requiredFile.isMissing(null, hasAttachment = true))
    }

    @Test
    fun teamNameLengthValidationMatchesTheParticipantFlow() {
        assertEquals("Use at least $TEAM_NAME_MIN_LENGTH characters.", teamNameLengthError("xy"))
        assertNull(teamNameLengthError("Team"))
        assertEquals(
            "Use no more than $TEAM_NAME_MAX_LENGTH characters.",
            teamNameLengthError("x".repeat(TEAM_NAME_MAX_LENGTH + 1)),
        )
    }
}
