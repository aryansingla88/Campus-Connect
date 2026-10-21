package com.example.campusconnect.feature.registrations.model

/** Registration question control type, shared by host form editing and participant answer forms. */
enum class FieldType(val label: String) {
    TEXT("Short text"),
    NUMBER("Number"),
    SELECT("Dropdown"),
    MULTISELECT("Checkboxes"),
    DATE("Date"),
}

/** Participant presentation grouping; it is not serialized in host registration-field requests. */
enum class RegistrationQuestionContext { INDIVIDUAL, TEAM_COMMON, TEAM_CREATION }

data class FormField(
    val id: Int,
    /** Null for a new field; existing backend IDs are retained when editing. */
    val backendId: Int? = null,
    val label: String = "",
    val fieldType: FieldType = FieldType.TEXT,
    val rawFieldType: String? = null,
    val placeholder: String = "",
    val isRequired: Boolean = false,
    val options: List<String> = emptyList(),
    val individual: Boolean = true,
    val fieldOrder: Int = 0,
    /** Participant-only metadata; host form editing and serialization ignore it. */
    val readOnly: Boolean = false,
    val registrationContext: RegistrationQuestionContext? = null,
)
