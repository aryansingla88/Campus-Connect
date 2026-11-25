package com.example.campusconnect.core.visibility

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class VisibilityDefinitionsTest {
    @Test
    fun catalogContainsEveryScopeAndValueRequirement() {
        assertEquals(VisibilityScope.entries.toSet(), VisibilityDefinitions.all.map { it.scope }.toSet())
        assertFalse(VisibilityDefinitions.definition(VisibilityScope.PUBLIC)!!.requiresValue)
        assertFalse(VisibilityDefinitions.definition(VisibilityScope.CONNECTIONS)!!.requiresValue)
        assertTrue(VisibilityDefinitions.definition(VisibilityScope.CLUB)!!.requiresValue)
        assertTrue(VisibilityDefinitions.definition(VisibilityScope.BATCH)!!.requiresValue)
    }

    @Test
    fun academicYearsKeepAlumniDistinctFromNumericYears() {
        assertEquals(listOf("Year 1", "Year 2", "Year 3", "Year 4", "Alumni"),
            VisibilityDefinitions.academicYearChoices.map { it.label })
        assertEquals("ALUMNI", AcademicYearChoice.Alumni.apiValue)
        assertEquals("3", AcademicYearChoice.Year(3).apiValue)
    }

    @Test
    fun selectionsRequireScopeAppropriateValues() {
        assertEquals(VisibilityApiConfiguration(VisibilityScope.PUBLIC, null),
            VisibilitySelection(VisibilityScope.PUBLIC).toApiConfiguration())
        assertEquals(VisibilityApiConfiguration(VisibilityScope.CLUB, "14"),
            VisibilitySelection(VisibilityScope.CLUB, VisibilitySelectionValue.ClubId(14)).toApiConfiguration())
        assertThrows(IllegalArgumentException::class.java) {
            VisibilitySelection(VisibilityScope.HOSTEL).toApiConfiguration()
        }
        assertThrows(IllegalStateException::class.java) {
            VisibilitySelection(VisibilityScope.BATCH).toApiConfiguration()
        }
    }
}
