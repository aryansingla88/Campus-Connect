package com.example.campusconnect.core.academic

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class AcademicStatusTest {
    @Test fun fourYearCourseTransitionsAfterYearFour() {
        assertEquals("YEAR_1", AcademicStatus.forCalculatedYear(1, 4).apiValue)
        assertEquals("YEAR_4", AcademicStatus.forCalculatedYear(4, 4).apiValue)
        assertEquals("ALUMNI", AcademicStatus.forCalculatedYear(5, 4).apiValue)
    }

    @Test fun threeAndTwoYearCoursesUseTheirMetadataDuration() {
        assertEquals("YEAR_3", AcademicStatus.forCalculatedYear(3, 3).apiValue)
        assertEquals("ALUMNI", AcademicStatus.forCalculatedYear(4, 3).apiValue)
        assertEquals("YEAR_2", AcademicStatus.forCalculatedYear(2, 2).apiValue)
        assertEquals("ALUMNI", AcademicStatus.forCalculatedYear(3, 2).apiValue)
    }

    @Test fun longerCoursesAreNotAlumniAtYearFour() {
        assertEquals("YEAR_4", AcademicStatus.forCalculatedYear(4, 5).apiValue)
        assertEquals("YEAR_5", AcademicStatus.forCalculatedYear(5, 5).apiValue)
        assertEquals("ALUMNI", AcademicStatus.forCalculatedYear(6, 5).apiValue)
    }

    @Test fun julySessionBoundaryMatchesBackendConvention() {
        assertEquals("YEAR_3", AcademicStatus.current(2023, 4, LocalDate.of(2026, 6, 30))?.apiValue)
        assertEquals("YEAR_4", AcademicStatus.current(2023, 4, LocalDate.of(2026, 7, 1))?.apiValue)
        assertNull(AcademicStatus.current(null, 4, LocalDate.of(2026, 7, 1)))
        assertNull(AcademicStatus.current(2023, null, LocalDate.of(2026, 7, 1)))
    }
}
