package com.example.campusconnect.feature.registrations.viewmodel

import androidx.lifecycle.ViewModel
import com.example.campusconnect.feature.registrations.data.FakeFormDetails
import com.example.campusconnect.feature.registrations.ui.screens.FormField
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Holds the individual form and the team questions so both builder screens edit
 * one source of truth and "Move Existing Questions" can transfer between them.
 */
class RegistrationFormViewModel(
    initialIndividualFields: List<FormField> = FakeFormDetails.fields,
    initialTeamFields: List<FormField> = emptyList(),
) : ViewModel() {

    private val _individualFields = MutableStateFlow(initialIndividualFields)
    val individualFields: StateFlow<List<FormField>> = _individualFields.asStateFlow()

    private val _teamFields = MutableStateFlow(initialTeamFields)
    val teamFields: StateFlow<List<FormField>> = _teamFields.asStateFlow()

    fun setIndividualFields(fields: List<FormField>) {
        _individualFields.value = fields
    }

    fun setTeamFields(fields: List<FormField>) {
        _teamFields.value = fields
    }

    /** Removes the questions from the individual form and appends them to the team questions. */
    fun moveToTeam(ids: List<Int>) {
        val idSet = ids.toSet()
        val moving = _individualFields.value.filter { it.id in idSet }
        if (moving.isEmpty()) return

        _individualFields.update { list -> list.filterNot { it.id in idSet } }
        _teamFields.update { team ->
            var nextId = (team.maxOfOrNull { it.id } ?: 0) + 1
            team + moving.map { it.copy(id = nextId++) }
        }
    }
}
