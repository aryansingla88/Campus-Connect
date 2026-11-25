package com.example.campusconnect.core.visibility

enum class VisibilityValueKind {
    NONE,
    JOINED_CLUB,
    HOSTEL_METADATA,
    COURSE_METADATA,
    ACADEMIC_YEAR,
    COURSE_AND_ACADEMIC_YEAR
}

enum class VisibilityScope(
    val label: String,
    val description: String,
    val valueKind: VisibilityValueKind
) {
    PUBLIC("Public", "Everyone can see this post", VisibilityValueKind.NONE),
    CONNECTIONS("Connections", "People connected with you", VisibilityValueKind.NONE),
    CLUB("Club", "Members of a club you have joined", VisibilityValueKind.JOINED_CLUB),
    HOSTEL("Hostel", "Students assigned to a hostel", VisibilityValueKind.HOSTEL_METADATA),
    COURSE("Course", "Students in a course", VisibilityValueKind.COURSE_METADATA),
    ACADEMIC_YEAR("Academic year", "Students in a selected year or alumni", VisibilityValueKind.ACADEMIC_YEAR),
    BATCH("Batch", "A course and academic-year group", VisibilityValueKind.COURSE_AND_ACADEMIC_YEAR)
}

data class VisibilityDefinition(
    val scope: VisibilityScope,
    val valueKind: VisibilityValueKind = scope.valueKind
) {
    val requiresValue: Boolean get() = valueKind != VisibilityValueKind.NONE
}

sealed interface AcademicYearChoice {
    val label: String
    val apiValue: String

    data class Year(val year: Int) : AcademicYearChoice {
        init { require(year in 1..4) }
        override val label: String = "Year $year"
        override val apiValue: String = year.toString()
    }

    data object Alumni : AcademicYearChoice {
        override val label: String = "Alumni"
        override val apiValue: String = "ALUMNI"
    }
}

sealed interface VisibilitySelectionValue {
    data class ClubId(val id: Int) : VisibilitySelectionValue {
        init { require(id > 0) }
    }
    data class HostelId(val id: Int) : VisibilitySelectionValue {
        init { require(id > 0) }
    }
    data class CourseId(val id: Int) : VisibilitySelectionValue {
        init { require(id > 0) }
    }
    data class AcademicYear(val choice: AcademicYearChoice) : VisibilitySelectionValue
}

data class VisibilityApiConfiguration(
    val scope: VisibilityScope?,
    val value: String? = null
)

data class VisibilitySelection(
    val scope: VisibilityScope,
    val value: VisibilitySelectionValue? = null
) {
    fun toApiConfiguration(): VisibilityApiConfiguration {
        val apiValue = when (val selected = value) {
            null -> null
            is VisibilitySelectionValue.ClubId -> selected.id.toString()
            is VisibilitySelectionValue.HostelId -> selected.id.toString()
            is VisibilitySelectionValue.CourseId -> selected.id.toString()
            is VisibilitySelectionValue.AcademicYear -> selected.choice.apiValue
        }

        when (scope.valueKind) {
            VisibilityValueKind.NONE -> require(value == null) { "$scope does not accept a value" }
            VisibilityValueKind.JOINED_CLUB -> require(value is VisibilitySelectionValue.ClubId)
            VisibilityValueKind.HOSTEL_METADATA -> require(value is VisibilitySelectionValue.HostelId)
            VisibilityValueKind.COURSE_METADATA -> require(value is VisibilitySelectionValue.CourseId)
            VisibilityValueKind.ACADEMIC_YEAR -> require(value is VisibilitySelectionValue.AcademicYear)
            // No committed project contract defines how a BATCH value is encoded.
            VisibilityValueKind.COURSE_AND_ACADEMIC_YEAR ->
                throw IllegalStateException("Batch visibility configuration is not defined")
        }
        return VisibilityApiConfiguration(scope, apiValue)
    }
}

object VisibilityDefinitions {
    val all: List<VisibilityDefinition> = VisibilityScope.entries.map(::VisibilityDefinition)
    val academicYearChoices: List<AcademicYearChoice> =
        (1..4).map(AcademicYearChoice::Year) + AcademicYearChoice.Alumni

    fun definition(scope: VisibilityScope): VisibilityDefinition? = all.firstOrNull { it.scope == scope }
}
