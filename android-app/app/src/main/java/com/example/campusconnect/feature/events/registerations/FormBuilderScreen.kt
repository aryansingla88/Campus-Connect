package com.example.campusconnect.feature.events.registerations

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

// ── Theme Colors ──────────────────────────────────────────────────────────────

val OrangePrimary = Color(0xFFE65100)
val OrangeLight   = Color(0xFFFFF3E0)
val OrangeSurface = Color(0xFFFFF8F5)
val OrangeBorder  = Color(0xFFFFCCBC)
val TextPrimary   = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF757575)
val CardBg        = Color(0xFFFFFFFF)
val PageBg        = Color(0xFFF5F5F5)

// OrangeAccent removed — was never used

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
    onPublish: (title: String, description: String, fields: List<FormField>) -> Unit,
    onBack: () -> Unit,
    initialTitle: String           = "",
    initialDescription: String     = "",
    initialFields: List<FormField> = emptyList(),
) {
    var formTitle       by remember { mutableStateOf(initialTitle) }
    var formDescription by remember { mutableStateOf(initialDescription) }
    var fields          by remember { mutableStateOf(initialFields) }
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
            FormTopBar(onBack = onBack, onPublish = { onPublish(formTitle, formDescription, fields) })
        },
        bottomBar = {
            AddFieldBar(
                onAdd = {
                    val newField = FormField(id = nextId++)
                    fields = fields + newField
                    expandedFieldId = newField.id
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier            = Modifier.fillMaxSize().padding(padding),
            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                FormHeaderCard(
                    title         = formTitle,
                    description   = formDescription,
                    onTitleChange = { formTitle = it },
                    onDescChange  = { formDescription = it },
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

// ── Top Bar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTopBar(onBack: () -> Unit, onPublish: () -> Unit) {
    TopAppBar(
        title = { Text("Form builder", fontWeight = FontWeight.SemiBold, fontSize = 17.sp, color = TextPrimary) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
        },
        actions = {
            Button(
                onClick  = onPublish,
                colors   = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                shape    = RoundedCornerShape(10.dp),
                modifier = Modifier.padding(end = 12.dp),
            ) {
                Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Publish", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg),
    )
}

// ── Add Field Bar ─────────────────────────────────────────────────────────────

@Composable
fun AddFieldBar(onAdd: () -> Unit) {
    Surface(color = CardBg, tonalElevation = 0.dp, modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedButton(
                onClick  = onAdd,
                border   = BorderStroke(1.5.dp, OrangePrimary),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth().height(48.dp),
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add question", fontWeight = FontWeight.Medium, fontSize = 15.sp)
            }
        }
    }
}

// ── Form Header Card ──────────────────────────────────────────────────────────

@Composable
fun FormHeaderCard(
    title: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Box(Modifier.fillMaxWidth().height(6.dp).background(OrangePrimary))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Form details", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OrangePrimary, letterSpacing = 0.8.sp)
            OutlinedTextField(
                value         = title,
                onValueChange = onTitleChange,
                label         = { Text("Form title *") },
                placeholder   = { Text("e.g. Workshop registration") },
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = orangeTextFieldColors(),
                shape         = RoundedCornerShape(10.dp),
            )
            OutlinedTextField(
                value         = description,
                onValueChange = onDescChange,
                label         = { Text("Description") },
                placeholder   = { Text("What is this form for?") },
                minLines      = 2,
                maxLines      = 4,
                modifier      = Modifier.fillMaxWidth(),
                colors        = orangeTextFieldColors(),
                shape         = RoundedCornerShape(10.dp),
            )
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
        FormBuilderScreen(eventId = 1, onPublish = { _, _, _ -> }, onBack = {})
    }
}