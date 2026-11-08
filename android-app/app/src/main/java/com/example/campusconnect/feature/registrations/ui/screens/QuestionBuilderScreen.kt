package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.QuestionType
import com.example.campusconnect.feature.registrations.model.RegistrationQuestion
import java.util.UUID

/** Optional title block above the question list (used by the team builder). */
data class BuilderHeading(val title: String, val subtitle: String)

/**
 * Enables "Move Existing Questions": adds a second bottom-bar button and the
 * bottom sheet that lists [candidates]. [onMove] receives the chosen question ids.
 */
data class MoveExistingConfig(
    val candidates: List<RegistrationQuestion>,
    val onMove: (localIds: List<String>) -> Unit,
)

/**
 * The one question builder behind both the individual form and the team questions.
 * State is hoisted ([fields] / [onFieldsChange]). The two modes differ only by:
 *  - [showQuickFields]: the Quick Fields card (individual form only).
 *  - [moveExisting]: when non-null the bottom bar has two buttons (Add + Move
 *    Existing Questions) and the move sheet is available (team questions only).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionBuilderScreen(
    eventName: String,
    isPublished: Boolean,
    fields: List<RegistrationQuestion>,
    onFieldsChange: (List<RegistrationQuestion>) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    heading: BuilderHeading? = null,
    addQuestionLabel: String = "Add Question",
    emptyTitle: String = "No questions yet",
    emptyIcon: ImageVector = Icons.Outlined.ListAlt,
    emptyMessage: String = "Tap \"Add question\" to start\nbuilding your form",
    showQuickFields: Boolean = false,
    initialQuickFieldIds: Set<String> = emptySet(),
    onQuickFieldsChanged: (Set<String>) -> Unit = {},
    moveExisting: MoveExistingConfig? = null,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    isLoaded: Boolean = true,
    saveError: String? = null,
    saveSucceeded: Boolean = false,
    onRetry: () -> Unit = {},
) {
    var selectedQuickFields by remember(initialQuickFieldIds) { mutableStateOf(initialQuickFieldIds) }
    var expandedFieldId by remember { mutableStateOf<String?>(null) }
    var showTypeSheet by remember { mutableStateOf<String?>(null) }
    var showMoveSheet by remember { mutableStateOf(false) }

    fun updateField(localId: String, transform: (RegistrationQuestion) -> RegistrationQuestion) =
        onFieldsChange(fields.map { if (it.localId == localId) transform(it) else it })

    fun addQuestion() {
        val newField = RegistrationQuestion(localId = UUID.randomUUID().toString())
        onFieldsChange(fields + newField)
        expandedFieldId = newField.localId
    }

    showTypeSheet?.let { targetId ->
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = null },
            containerColor = CardBg,
            tonalElevation = 0.dp,
        ) {
            QuestionTypePicker(
                current = fields.find { it.localId == targetId }?.type as? QuestionType.Known ?: QuestionType.Known.TEXT,
                onPick = { picked ->
                    updateField(targetId) { it.copy(type = picked) }
                    showTypeSheet = null
                },
            )
            Spacer(Modifier.height(32.dp))
        }
    }

    if (moveExisting != null && showMoveSheet) {
        MoveQuestionsSheet(
            candidates = moveExisting.candidates,
            onDismiss = { showMoveSheet = false },
            onConfirm = { ids ->
                showMoveSheet = false
                moveExisting.onMove(ids)
            },
        )
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            RegistrationHeader(
                eventName = eventName,
                isPublished = isPublished,
                onBack = onBack,
                action = {
                    HeaderActionButton(
                        text = if (isSaving) "Saving…" else "Save",
                        icon = Icons.Outlined.Save,
                        onClick = onSave,
                        enabled = isLoaded && !isLoading && !isSaving,
                    )
                },
            )
        },
        bottomBar = {
            RegistrationBottomBar {
                Button(
                    onClick = ::addQuestion,
                    enabled = isLoaded && !isLoading && !isSaving,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        addQuestionLabel,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                if (moveExisting != null) {
                    Spacer(Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = { showMoveSheet = true },
                        enabled = isLoaded && !isLoading && !isSaving,
                        border = BorderStroke(1.5.dp, OrangePrimary),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = OrangePrimary,
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                    ) {
                        Icon(Icons.Outlined.SwapHoriz, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Move Existing Questions",
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (isLoading) item { LinearProgressIndicator(Modifier.fillMaxWidth()) }
            if (saveError != null) item {
                Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(saveError, modifier = Modifier.weight(1f), color = Color(0xFFE53935))
                        TextButton(onClick = onRetry) { Text("Retry") }
                    }
                }
            }
            if (saveSucceeded) item {
                Text("Changes saved.", color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 4.dp))
            }
            if (heading != null) {
                item {
                    Column {
                        Text(
                            if (fields.isEmpty()) heading.title else "${heading.title} (${fields.size})",
                            fontSize = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(heading.subtitle, fontSize = 13.sp, color = TextSecondary)
                    }
                }
            }

            if (showQuickFields) {
                item {
                    QuickFieldsCard(
                        selectedIds = selectedQuickFields,
                        onToggle = { id ->
                            val updated = if (id in selectedQuickFields) selectedQuickFields - id else selectedQuickFields + id
                            selectedQuickFields = updated
                            onQuickFieldsChanged(updated)
                        },
                    )
                }
            }

            itemsIndexed(fields, key = { _, f -> f.localId }) { index, field ->
                FieldCard(
                    field = field,
                    index = index + 1,
                    expanded = expandedFieldId == field.localId,
                    onExpand = { expandedFieldId = if (expandedFieldId == field.localId) null else field.localId },
                    onDelete = { onFieldsChange(fields.filter { it.localId != field.localId }) },
                    onDuplicate = {
                        val copy = field.copy(localId = UUID.randomUUID().toString(), backendId = null, fieldOrder = 0)
                        val idx = fields.indexOfFirst { it.localId == field.localId }
                        onFieldsChange(fields.toMutableList().apply { add(idx + 1, copy) })
                    },
                    onLabelChange = { v -> updateField(field.localId) { it.copy(label = v) } },
                    onPlaceholderChange = { v -> updateField(field.localId) { it.copy(placeholder = v) } },
                    onRequiredToggle = { updateField(field.localId) { it.copy(isRequired = !it.isRequired) } },
                    onTypeClick = { showTypeSheet = field.localId },
                    onAddOption = { updateField(field.localId) { it.copy(options = it.options + "Option ${it.options.size + 1}") } },
                    onOptionChange = { optIdx, v ->
                        updateField(field.localId) { f -> f.copy(options = f.options.toMutableList().also { it[optIdx] = v }) }
                    },
                    onOptionDelete = { optIdx ->
                        updateField(field.localId) { f -> f.copy(options = f.options.toMutableList().also { it.removeAt(optIdx) }) }
                    },
                )
            }

            if (fields.isEmpty() && isLoaded && !isLoading && saveError == null) {
                item { EmptyState(title = emptyTitle, message = emptyMessage, icon = emptyIcon) }
            }
        }
    }
}
