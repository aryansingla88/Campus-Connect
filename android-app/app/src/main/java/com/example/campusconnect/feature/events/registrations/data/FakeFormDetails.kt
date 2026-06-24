package com.example.campusconnect.feature.events.registrations.data

import com.example.campusconnect.feature.events.registrations.ui.FormField
import com.example.campusconnect.feature.events.registrations.ui.FieldType

object FakeFormDetails {

    val fields = listOf(
        FormField(
            id = 1,
            label = "Full Name",
            fieldType = FieldType.TEXT,
            isRequired = true
        ),
        FormField(
            id = 2,
            label = "Year",
            fieldType = FieldType.SELECT,
            options = mutableListOf(
                "1st Year",
                "2nd Year",
                "3rd Year",
                "4th Year"
            )
        )
    )

    const val title = "Android Workshop Registration"

    const val description =
        "Please fill out the form below."
}