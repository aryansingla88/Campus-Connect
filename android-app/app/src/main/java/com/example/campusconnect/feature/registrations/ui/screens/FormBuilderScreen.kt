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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Data Models ───────────────────────────────────────────────────────────────

enum class FieldType(val label: String, val icon: ImageVector) {
    TEXT("Short text", Icons.Outlined.ShortText),
    NUMBER("Number", Icons.Outlined.Pin),
    SELECT("Dropdown", Icons.Outlined.ArrowDropDownCircle),
    MULTISELECT("Checkboxes", Icons.Outlined.CheckBox),
    DATE("Date", Icons.Outlined.CalendarMonth),
}

// var → val, MutableList → List (immutable, safe for Compose state)
data class FormField(
    val id: Int,
    val label: String        = "",
    val fieldType: FieldType = FieldType.TEXT,
    val placeholder: String  = "",
    val isRequired: Boolean  = false,
    val options: List<String> = listOf("Option 1"),
)

// ── Screen ────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormBuilderScreen(
    eventId: Int,
    eventName: String,
    isPublished: Boolean,
    onPublish: (fields: List<FormField>) -> Unit,
    onBack: () -> Unit,
    initialQuickFieldIds: Set<String> = emptySet(),
    onQuickFieldsChanged: (Set<String>) -> Unit = {},
    initialFields: List<FormField> = emptyList(),
) {
    var fields by remember { mutableStateOf(initialFields) }
    var selectedQuickFields by remember { mutableStateOf(initialQuickFieldIds) }
    var nextId          by remember { mutableIntStateOf((initialFields.maxOfOrNull { it.id } ?: 0) + 1) }
    var expandedFieldId by remember { mutableStateOf<Int?>(null) }
    var showTypeSheet   by remember { mutableStateOf<Int?>(null) }

    if (showTypeSheet != null) {
        val targetId = showTypeSheet!!
        ModalBottomSheet(
            onDismissRequest = { showTypeSheet = null },
            containerColor   = CardBg,
            tonalElevation   = 0.dp,
        ) {
            FieldTypePicker(
                current = fields.find { it.id == targetId }?.fieldType ?: FieldType.TEXT,
                onPick  = { picked ->
                    fields = fields.map { if (it.id == targetId) it.copy(fieldType = picked) else it }
                    showTypeSheet = null
                },
            )
            Spacer(Modifier.height(32.dp))
        }
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
                        text = "Publish",
                        icon = Icons.Outlined.Send,
                        onClick = { onPublish(fields) }
                    )
                }
            )
        },

        bottomBar = {
            RegistrationBottomBar {

                OutlinedButton(
                    onClick = {
                        val newField = FormField(id = nextId++)
                        fields = fields + newField
                        expandedFieldId = newField.id
                    },
                    border = BorderStroke(
                        1.5.dp,
                        OrangePrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Icon(
                        Icons.Outlined.Add,
                        contentDescription = null
                    )

                    Spacer(Modifier.width(8.dp))

                    Text(
                        "Add Question",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                QuickFieldsCard(
                    selectedIds = selectedQuickFields,
                    onToggle = { id ->
                        val updated = if (id in selectedQuickFields) {
                            selectedQuickFields - id
                        } else {
                            selectedQuickFields + id
                        }

                        selectedQuickFields = updated
                        onQuickFieldsChanged(updated)
                    }
                )
            }

            itemsIndexed(fields, key = { _, f -> f.id }) { index, field ->
                FieldCard(
                    field               = field,
                    index               = index + 1,
                    expanded            = expandedFieldId == field.id,
                    onExpand            = { expandedFieldId = if (expandedFieldId == field.id) null else field.id },
                    onDelete            = { fields = fields.filter { it.id != field.id } },
                    onDuplicate         = {
                        val copy = field.copy(id = nextId++, options = field.options.toMutableList())
                        val idx  = fields.indexOfFirst { it.id == field.id }
                        fields   = fields.toMutableList().apply { add(idx + 1, copy) }
                    },
                    onLabelChange       = { v -> fields = fields.map { if (it.id == field.id) it.copy(label = v) else it } },
                    onPlaceholderChange = { v -> fields = fields.map { if (it.id == field.id) it.copy(placeholder = v) else it } },
                    onRequiredToggle    = { fields = fields.map { if (it.id == field.id) it.copy(isRequired = !it.isRequired) else it } },
                    onTypeClick         = { showTypeSheet = field.id },
                    onAddOption         = { fields = fields.map { if (it.id == field.id) it.copy(options = it.options + "Option ${it.options.size + 1}") else it } },
                    onOptionChange      = { optIdx, v ->
                        fields = fields.map { f ->
                            if (f.id == field.id) f.copy(options = f.options.toMutableList().also { it[optIdx] = v }) else f
                        }
                    },
                    onOptionDelete = { optIdx ->
                        fields = fields.map { f ->
                            if (f.id == field.id) f.copy(options = f.options.toMutableList().also { it.removeAt(optIdx) }) else f
                        }
                    },
                )
            }

            if (fields.isEmpty()) item { EmptyState() }
        }
    }
}

// ── Field Card ────────────────────────────────────────────────────────────────

@Composable
fun FieldCard(
    field: FormField,
    index: Int,
    expanded: Boolean,
    onExpand: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onLabelChange: (String) -> Unit,
    onPlaceholderChange: (String) -> Unit,
    onRequiredToggle: () -> Unit,
    onTypeClick: () -> Unit,
    onAddOption: () -> Unit,
    onOptionChange: (Int, String) -> Unit,
    onOptionDelete: (Int) -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier              = Modifier.fillMaxWidth().clickable(onClick = onExpand).padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier         = Modifier.size(28.dp).clip(CircleShape).background(if (expanded) OrangePrimary else OrangeLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("$index", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = if (expanded) Color.White else OrangePrimary)
                }

                Text(
                    text       = field.label.ifBlank { "Untitled question" },
                    modifier   = Modifier.weight(1f),
                    fontSize   = 14.sp,
                    fontWeight = if (field.label.isNotBlank()) FontWeight.Medium else FontWeight.Normal,
                    color      = if (field.label.isNotBlank()) TextPrimary else TextSecondary,
                    maxLines   = 1,
                )

                Surface(shape = RoundedCornerShape(8.dp), color = OrangeLight) {
                    Row(
                        modifier              = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Icon(field.fieldType.icon, null, modifier = Modifier.size(13.dp), tint = OrangePrimary)
                        Text(field.fieldType.label, fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Medium)
                    }
                }

                Icon(
                    if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = null,
                    tint               = TextSecondary,
                    modifier           = Modifier.size(20.dp),
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    HorizontalDivider(color = OrangeBorder, thickness = 0.5.dp)

                    OutlinedTextField(
                        value         = field.label,
                        onValueChange = onLabelChange,
                        label         = { Text("Question label *") },
                        placeholder   = { Text("e.g. Full name") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        colors        = orangeTextFieldColors(),
                        shape         = RoundedCornerShape(10.dp),
                    )

                    Text("Field type", fontSize = 12.sp, color = TextSecondary)
                    Surface(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).border(1.dp, OrangeBorder, RoundedCornerShape(10.dp)).clickable(onClick = onTypeClick),
                        color    = OrangeSurface,
                    ) {
                        Row(
                            modifier              = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Icon(field.fieldType.icon, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                            Text(field.fieldType.label, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                            Icon(Icons.Outlined.SwapVert, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                        }
                    }

                    if (field.fieldType == FieldType.TEXT || field.fieldType == FieldType.NUMBER) {
                        OutlinedTextField(
                            value         = field.placeholder,
                            onValueChange = onPlaceholderChange,
                            label         = { Text("Placeholder (optional)") },
                            singleLine    = true,
                            modifier      = Modifier.fillMaxWidth(),
                            colors        = orangeTextFieldColors(),
                            shape         = RoundedCornerShape(10.dp),
                        )
                    }

                    if (field.fieldType == FieldType.SELECT || field.fieldType == FieldType.MULTISELECT) {
                        OptionsEditor(
                            options        = field.options,
                            onOptionChange = onOptionChange,
                            onOptionDelete = onOptionDelete,
                            onAddOption    = onAddOption,
                        )
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Column {
                            Text("Required field", fontSize = 14.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text("Respondent must answer", fontSize = 12.sp, color = TextSecondary)
                        }
                        Switch(
                            checked         = field.isRequired,
                            onCheckedChange = { onRequiredToggle() },
                            colors          = SwitchDefaults.colors(
                                checkedThumbColor   = Color.White,
                                checkedTrackColor   = OrangePrimary,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color(0xFFBDBDBD),
                            ),
                        )
                    }

                    Row(
                        modifier              = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedButton(
                            onClick  = onDuplicate,
                            border   = BorderStroke(0.5.dp, OrangeBorder),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.ContentCopy, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Duplicate", fontSize = 13.sp)
                        }
                        OutlinedButton(
                            onClick  = onDelete,
                            border   = BorderStroke(0.5.dp, Color(0xFFFFCDD2)),
                            shape    = RoundedCornerShape(10.dp),
                            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935)),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Outlined.Delete, null, modifier = Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Delete", fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}
// ── Quick Fields Card ─────────────────────────────────────────────────────────

private data class QuickFieldOption(
    val id: String,
    val label: String,
    val icon: ImageVector,
)

private val quickFieldOptions = listOf(
    QuickFieldOption("full_name", "Full Name", Icons.Outlined.Person),
    QuickFieldOption("email", "Email Address", Icons.Outlined.Email),
    QuickFieldOption("phone", "Phone Number", Icons.Outlined.Phone),
    QuickFieldOption("course", "Course", Icons.Outlined.School),
    QuickFieldOption("year_batch", "Year / Batch", Icons.Outlined.CalendarMonth),
    QuickFieldOption("roll_number", "Roll Number", Icons.Outlined.Badge),
    QuickFieldOption("hostel", "Hostel", Icons.Outlined.Home),
    QuickFieldOption("hometown", "Hometown", Icons.Outlined.LocationOn),
    QuickFieldOption("gender", "Gender", Icons.Outlined.PersonOutline),
    QuickFieldOption("date_of_birth", "Date of Birth", Icons.Outlined.Cake),
    QuickFieldOption("github", "GitHub Profile", Icons.Outlined.Code),
    QuickFieldOption("linkedin", "LinkedIn Profile", Icons.Outlined.Link),
)

@Composable
private fun QuickFieldsCard(
    selectedIds: Set<String>,
    onToggle: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(horizontal = 16.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(OrangeLight),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Badge,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(30.dp),
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Quick Fields",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                    )

                    Text(
                        "Select profile information collected automatically",
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = TextSecondary,
                    )

                    if (!expanded) {
                        val selectedLabels = quickFieldOptions
                            .filter { it.id in selectedIds }
                            .map { it.label }

                        val previewLabels = selectedLabels.take(3)
                        val remainingCount =
                            selectedLabels.size - previewLabels.size

                        Text(
                            text = when {
                                selectedLabels.isEmpty() ->
                                    "No fields selected"

                                remainingCount > 0 ->
                                    "${previewLabels.joinToString(" · ")} · +$remainingCount more"

                                else ->
                                    previewLabels.joinToString(" · ")
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            color = OrangePrimary,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                    }
                }

                Icon(
                    imageVector =
                        if (expanded)
                            Icons.Outlined.KeyboardArrowUp
                        else
                            Icons.Outlined.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(26.dp),
                )
            }

            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    )
                ) {
                    HorizontalDivider(
                        color = Color(0xFFEEEEEE)
                    )

                    quickFieldOptions.forEachIndexed { index, option ->

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onToggle(option.id)
                                }
                                .padding(vertical = 9.dp),
                            verticalAlignment =
                                Alignment.CenterVertically,
                            horizontalArrangement =
                                Arrangement.spacedBy(10.dp),
                        ) {
                            Checkbox(
                                checked = option.id in selectedIds,
                                onCheckedChange = {
                                    onToggle(option.id)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = OrangePrimary,
                                    uncheckedColor =
                                        Color(0xFF9E9E9E),
                                ),
                                modifier = Modifier.size(24.dp),
                            )

                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(18.dp),
                            )

                            Text(
                                text = option.label,
                                fontSize = 13.sp,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f),
                            )
                        }

                        if (index != quickFieldOptions.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFF1F1F1),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
// ── Options Editor ────────────────────────────────────────────────────────────

@Composable
fun OptionsEditor(
    options: List<String>,
    onOptionChange: (Int, String) -> Unit,
    onOptionDelete: (Int) -> Unit,
    onAddOption: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Options", fontSize = 12.sp, color = TextSecondary)
        options.forEachIndexed { idx, opt ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Outlined.DragIndicator, null, tint = TextSecondary, modifier = Modifier.size(18.dp))
                OutlinedTextField(
                    value         = opt,
                    onValueChange = { onOptionChange(idx, it) },
                    singleLine    = true,
                    modifier      = Modifier.weight(1f),
                    colors        = orangeTextFieldColors(),
                    shape         = RoundedCornerShape(10.dp),
                )
                if (options.size > 1) {
                    IconButton(onClick = { onOptionDelete(idx) }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Close, null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
        TextButton(onClick = onAddOption, colors = ButtonDefaults.textButtonColors(contentColor = OrangePrimary)) {
            Icon(Icons.Outlined.Add, null, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Add option", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

// ── Field Type Picker ─────────────────────────────────────────────────────────

@Composable
fun FieldTypePicker(current: FieldType, onPick: (FieldType) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text("Choose field type", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary, modifier = Modifier.padding(bottom = 16.dp))
        FieldType.entries.forEach { type ->
            val isSelected = type == current
            Surface(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onPick(type) }.padding(vertical = 2.dp),
                color    = if (isSelected) OrangeLight else Color.Transparent,
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Box(
                        modifier         = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(if (isSelected) OrangePrimary else OrangeLight),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(type.icon, null, tint = if (isSelected) Color.White else OrangePrimary, modifier = Modifier.size(18.dp))
                    }
                    Text(
                        type.label,
                        fontSize   = 15.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        color      = if (isSelected) OrangePrimary else TextPrimary,
                        modifier   = Modifier.weight(1f),
                    )
                    if (isSelected) Icon(Icons.Outlined.CheckCircle, null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
fun EmptyState() {
    Column(
        modifier              = Modifier.fillMaxWidth().padding(vertical = 48.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier         = Modifier.size(72.dp).clip(CircleShape).background(OrangeLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Outlined.ListAlt, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(32.dp))
        }
        Text("No questions yet", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
        Text("Tap \"Add question\" to start\nbuilding your form", fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center, lineHeight = 20.sp)
    }
}

// ── Shared TextField Colors ───────────────────────────────────────────────────

@Composable
fun orangeTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = OrangePrimary,
    unfocusedBorderColor = OrangeBorder,
    focusedLabelColor    = OrangePrimary,
    cursorColor          = OrangePrimary,
)

// ── Preview ───────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun FormBuilderPreview() {
    MaterialTheme {
        FormBuilderScreen(
            eventId = 1,
            eventName = "Demo Event",
            isPublished = false,
            onPublish = { _ -> },
            onBack = {}
        )
    }
}
