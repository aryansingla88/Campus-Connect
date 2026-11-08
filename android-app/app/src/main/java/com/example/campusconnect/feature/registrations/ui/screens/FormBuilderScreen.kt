package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Screen ────────────────────────────────────────────────────────────────────

/**
 * Individual registration form: the shared [QuestionBuilderScreen] with the
 * Quick Fields card and a single "Add Question" bottom button.
 */
@Composable
fun FormBuilderScreen(
    eventId: Int,
    eventName: String,
    isPublished: Boolean,
    fields: List<RegistrationQuestion>,
    onFieldsChange: (List<RegistrationQuestion>) -> Unit,
    onPublish: (fields: List<RegistrationQuestion>) -> Unit,
    onBack: () -> Unit,
    initialQuickFieldIds: Set<String> = emptySet(),
    onQuickFieldsChanged: (Set<String>) -> Unit = {},
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    isLoaded: Boolean = true,
    saveError: String? = null,
    saveSucceeded: Boolean = false,
    onRetry: () -> Unit = {},
) {
    QuestionBuilderScreen(
        eventName = eventName,
        isPublished = isPublished,
        fields = fields,
        onFieldsChange = onFieldsChange,
        onSave = { onPublish(fields) },
        onBack = onBack,
        showQuickFields = true,
        initialQuickFieldIds = initialQuickFieldIds,
        onQuickFieldsChanged = onQuickFieldsChanged,
        isLoading = isLoading,
        isSaving = isSaving,
        isLoaded = isLoaded,
        saveError = saveError,
        saveSucceeded = saveSucceeded,
        onRetry = onRetry,
    )
}

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun FormBuilderPreview() {
    MaterialTheme {
        var fields by remember { mutableStateOf(emptyList<RegistrationQuestion>()) }
        FormBuilderScreen(
            eventId = 1,
            eventName = "Demo Event",
            isPublished = false,
            fields = fields,
            onFieldsChange = { fields = it },
            onPublish = { _ -> },
            onBack = {},
        )
    }
}
