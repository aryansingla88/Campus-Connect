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

/** Optional title block above the question list (used by the team builder). */
data class BuilderHeading(val title: String, val subtitle: String)

/**
 * Enables "Move Existing Questions": adds a second bottom-bar button and the
 * bottom sheet that lists [candidates]. [onMove] receives the chosen question ids.
 */
data class MoveExistingConfig(
    val candidates: List<FormField>,
    val onMove: (ids: List<Int>) -> Unit,
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
    fields: List<FormField>,
    onFieldsChange: (List<FormField>) -> Unit,
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
) {
    var selectedQuickFields by remember { mutableStateOf(initialQuickFieldIds) }
    var expandedFieldId by remember { mutableStateOf<Int?>(null) }
    var showTypeSheet by remember { mutableStateOf<Int?>(null) }
    var showMoveSheet by remember { mutableStateOf(false) }

    fun nextId() = (fields.maxOfOrNull { it.id } ?: 0) + 1
    fun updateField(id: Int, transform: (FormField) -> FormField) =
        onFieldsChange(fields.map { if (it.id == id) transform(it) else it })

    fun addQuestion() {
        val newField = FormField(id = nextId())
        onFieldsChange(fields + newField)
        expandedFieldId = newField.id
    }

    showTypeSheet?.let { targetId ->
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = null },
            containerColor = CardBg,
            tonalElevation = 0.dp,
        ) {
            FieldTypePicker(
                current = fields.find { it.id == targetId }?.fieldType ?: FieldType.TEXT,
                onPick = { picked ->
                    updateField(targetId) { it.copy(fieldType = picked) }
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
                        text = "Save",
                        icon = Icons.Outlined.Save,
                        onClick = onSave
                    )
                },
            )
        },
        bottomBar = {
            RegistrationBottomBar {
                Button(
                    onClick = ::addQuestion,
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

            itemsIndexed(fields, key = { _, f -> f.id }) { index, field ->
                FieldCard(
                    field = field,
                    index = index + 1,
                    expanded = expandedFieldId == field.id,
                    onExpand = { expandedFieldId = if (expandedFieldId == field.id) null else field.id },
                    onDelete = { onFieldsChange(fields.filter { it.id != field.id }) },
                    onDuplicate = {
                        val copy = field.copy(id = nextId())
                        val idx = fields.indexOfFirst { it.id == field.id }
                        onFieldsChange(fields.toMutableList().apply { add(idx + 1, copy) })
                    },
                    onLabelChange = { v -> updateField(field.id) { it.copy(label = v) } },
                    onPlaceholderChange = { v -> updateField(field.id) { it.copy(placeholder = v) } },
                    onRequiredToggle = { updateField(field.id) { it.copy(isRequired = !it.isRequired) } },
                    onTypeClick = { showTypeSheet = field.id },
                    onAddOption = { updateField(field.id) { it.copy(options = it.options + "Option ${it.options.size + 1}") } },
                    onOptionChange = { optIdx, v ->
                        updateField(field.id) { f -> f.copy(options = f.options.toMutableList().also { it[optIdx] = v }) }
                    },
                    onOptionDelete = { optIdx ->
                        updateField(field.id) { f -> f.copy(options = f.options.toMutableList().also { it.removeAt(optIdx) }) }
                    },
                )
            }

            if (fields.isEmpty()) item { EmptyState(title = emptyTitle, message = emptyMessage, icon = emptyIcon) }
        }
    }
}
