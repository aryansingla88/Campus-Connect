package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.visibility.VisibilityApiConfiguration
import com.example.campusconnect.core.visibility.VisibilityScope
import com.example.campusconnect.core.visibility.VisibilitySelection
import com.example.campusconnect.core.visibility.VisibilitySelectionValue
import com.example.campusconnect.core.visibility.VisibilityDefinitions
import com.example.campusconnect.core.visibility.VisibilityValueKind
import com.example.campusconnect.feature.metadata.courses.CourseRepositoryProvider
import com.example.campusconnect.feature.metadata.hostels.HostelRepositoryProvider
import com.example.campusconnect.feature.profile.data.repo.ApiProfileRepository
import com.example.campusconnect.feature.profile.model.ClubStatus

private data class VisibilityOption(val id: Int, val label: String)

@Composable
fun CustomPostVisibilityDialog(
    onDismiss: () -> Unit,
    onConfirm: (VisibilityApiConfiguration) -> Unit
) {
    val context = LocalContext.current
    var selectedScope by remember { mutableStateOf<VisibilityScope?>(null) }
    var selectedValue by remember { mutableStateOf<VisibilitySelectionValue?>(null) }
    var retryKey by remember { mutableIntStateOf(0) }

    var clubs by remember { mutableStateOf<List<VisibilityOption>>(emptyList()) }
    var hostels by remember { mutableStateOf<List<VisibilityOption>>(emptyList()) }
    var courses by remember { mutableStateOf<List<VisibilityOption>>(emptyList()) }
    var clubsLoading by remember { mutableStateOf(true) }
    var hostelsLoading by remember { mutableStateOf(true) }
    var coursesLoading by remember { mutableStateOf(true) }
    var clubsError by remember { mutableStateOf<String?>(null) }
    var hostelsError by remember { mutableStateOf<String?>(null) }
    var coursesError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(retryKey) {
        clubsLoading = true
        clubsError = null
        val result = ApiProfileRepository(
            courseRepository = CourseRepositoryProvider.getRepository(context)
        ).getMyClubs()
        result.onSuccess { joined ->
            clubs = joined.filter { it.status == ClubStatus.JOINED || it.status == ClubStatus.LEAD }
                .map { VisibilityOption(it.clubId, it.name) }
        }.onFailure { clubsError = it.message ?: "Couldn't load your clubs." }
        clubsLoading = false
    }

    LaunchedEffect(retryKey) {
        hostelsLoading = true
        hostelsError = null
        val repository = HostelRepositoryProvider.getRepository(context)
        val cached = repository.getAllHostels()
        hostels = cached.map { VisibilityOption(it.id, it.displayLabel()) }
        val refreshed = repository.ensureHostelsCached()
        if (refreshed.isSuccess) {
            hostels = repository.getAllHostels().map { VisibilityOption(it.id, it.displayLabel()) }
        } else if (hostels.isEmpty()) {
            hostelsError = refreshed.exceptionOrNull()?.message ?: "Couldn't load hostels."
        }
        hostelsLoading = false
    }

    LaunchedEffect(retryKey) {
        coursesLoading = true
        coursesError = null
        val repository = CourseRepositoryProvider.getRepository(context)
        courses = repository.getAllCourses().map {
            VisibilityOption(it.courseId, listOfNotNull(it.degree, it.programName).filter(String::isNotBlank).joinToString(" — "))
        }
        val refreshed = repository.ensureCoursesCached()
        if (refreshed.isSuccess) {
            courses = repository.getAllCourses().map {
                VisibilityOption(it.courseId, listOfNotNull(it.degree, it.programName).filter(String::isNotBlank).joinToString(" — "))
            }
        } else if (courses.isEmpty()) {
            coursesError = refreshed.exceptionOrNull()?.message ?: "Couldn't load courses."
        }
        coursesLoading = false
    }

    val requiresValue = selectedScope?.valueKind?.let { it != VisibilityValueKind.NONE } ?: false
    val batchConfigured = selectedScope?.valueKind != VisibilityValueKind.COURSE_AND_ACADEMIC_YEAR
    val valueIsValid = when (selectedScope?.valueKind) {
        null -> false
        VisibilityValueKind.NONE -> selectedValue == null
        VisibilityValueKind.JOINED_CLUB -> selectedValue is VisibilitySelectionValue.ClubId
        VisibilityValueKind.HOSTEL_METADATA -> selectedValue is VisibilitySelectionValue.HostelId
        VisibilityValueKind.COURSE_METADATA -> selectedValue is VisibilitySelectionValue.CourseId
        VisibilityValueKind.ACADEMIC_YEAR -> selectedValue is VisibilitySelectionValue.AcademicYear
        VisibilityValueKind.COURSE_AND_ACADEMIC_YEAR -> false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose who can see your post") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(440.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                VisibilityScope.entries.filter { it != VisibilityScope.PUBLIC }.forEach { scope ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedScope = scope
                                selectedValue = null
                            }
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedScope == scope, onClick = {
                            selectedScope = scope
                            selectedValue = null
                        })
                        Column(modifier = Modifier.padding(start = 4.dp)) {
                            Text(scope.label, style = MaterialTheme.typography.titleSmall)
                            Text(scope.description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (requiresValue) {
                    Spacer(Modifier.height(4.dp))
                    when (selectedScope?.valueKind) {
                        null -> Unit
                        VisibilityValueKind.JOINED_CLUB -> OptionsSection(
                            label = "Club",
                            options = clubs,
                            selectedId = (selectedValue as? VisibilitySelectionValue.ClubId)?.id,
                            loading = clubsLoading,
                            error = clubsError,
                            emptyMessage = "You haven't joined any clubs yet.",
                            onRetry = { retryKey++ },
                            onSelect = { selectedValue = VisibilitySelectionValue.ClubId(it) }
                        )
                        VisibilityValueKind.HOSTEL_METADATA -> OptionsSection(
                            label = "Hostel",
                            options = hostels,
                            selectedId = (selectedValue as? VisibilitySelectionValue.HostelId)?.id,
                            loading = hostelsLoading,
                            error = hostelsError,
                            emptyMessage = "No hostels are available.",
                            onRetry = { retryKey++ },
                            onSelect = { selectedValue = VisibilitySelectionValue.HostelId(it) }
                        )
                        VisibilityValueKind.COURSE_METADATA -> OptionsSection(
                            label = "Course",
                            options = courses,
                            selectedId = (selectedValue as? VisibilitySelectionValue.CourseId)?.id,
                            loading = coursesLoading,
                            error = coursesError,
                            emptyMessage = "No courses are available.",
                            onRetry = { retryKey++ },
                            onSelect = { selectedValue = VisibilitySelectionValue.CourseId(it) }
                        )
                        VisibilityValueKind.ACADEMIC_YEAR -> {
                            val choices = VisibilityDefinitions.academicYearChoices
                            OptionDropdown(
                                label = "Academic year",
                                options = choices.mapIndexed { index, choice -> VisibilityOption(index + 1, choice.label) },
                                selectedId = (selectedValue as? VisibilitySelectionValue.AcademicYear)?.let { value ->
                                    choices.indexOf(value.choice).takeIf { it >= 0 }?.plus(1)
                                },
                                onSelect = { id -> selectedValue = VisibilitySelectionValue.AcademicYear(choices[id - 1]) }
                            )
                        }
                        VisibilityValueKind.COURSE_AND_ACADEMIC_YEAR -> Text(
                            "Batch selection is unavailable because the app has no defined course/year matching rule yet.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        VisibilityValueKind.NONE -> Unit
                    }
                }
                if (!batchConfigured) Text(
                    "Choose another audience to continue.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(
                enabled = valueIsValid && batchConfigured,
                onClick = {
                    val scope = selectedScope ?: return@Button
                    val selection = VisibilitySelection(scope, selectedValue)
                    runCatching { selection.toApiConfiguration() }
                        .onSuccess(onConfirm)
                }
            ) { Text("Continue") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun OptionsSection(
    label: String,
    options: List<VisibilityOption>,
    selectedId: Int?,
    loading: Boolean,
    error: String?,
    emptyMessage: String,
    onRetry: () -> Unit,
    onSelect: (Int) -> Unit
) {
    Text(label, style = MaterialTheme.typography.labelLarge)
    when {
        loading -> CircularProgressIndicator(modifier = Modifier.padding(12.dp))
        error != null -> Row(verticalAlignment = Alignment.CenterVertically) {
            Text(error, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
            TextButton(onClick = onRetry) { Text("Retry") }
        }
        options.isEmpty() -> Text(emptyMessage, style = MaterialTheme.typography.bodySmall)
        else -> OptionDropdown(
            label = "Select $label",
            options = options,
            selectedId = selectedId,
            onSelect = onSelect
        )
    }
}

@Composable
private fun OptionDropdown(
    label: String,
    options: List<VisibilityOption>,
    selectedId: Int?,
    onSelect: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.id == selectedId }?.label ?: label
    androidx.compose.foundation.layout.Box {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedLabel, modifier = Modifier.weight(1f), maxLines = 1)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    onClick = {
                        expanded = false
                        onSelect(option.id)
                    }
                )
            }
        }
    }
}
