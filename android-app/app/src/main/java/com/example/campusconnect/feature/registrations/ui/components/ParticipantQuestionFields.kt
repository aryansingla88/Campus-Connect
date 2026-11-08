package com.example.campusconnect.feature.registrations.ui.components

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material.icons.outlined.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.AttachedFile
import com.example.campusconnect.feature.registrations.model.isMissing
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionKind
import com.example.campusconnect.feature.registrations.ui.model.ParticipantQuestionUiModel
import com.example.campusconnect.feature.registrations.ui.model.isMissing
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
// ── Question inputs ───────────────────────────────────────────────────────────
//
// Every participant question, individual or team, is rendered through ParticipantQuestionField so the
// label, required/optional marker, input style, error text and spacing are identical on all steps.

private val FieldShape = RoundedCornerShape(14.dp)
private val DisplayDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

/** File state for FILE questions. Screens that cannot collect files leave it out. */
internal class QuestionAttachment(
    val file: AttachedFile?,
    val error: String?,
    val onPicked: (AttachedFile) -> Unit,
    val onRemoved: () -> Unit,
)

@Composable
internal fun ParticipantQuestionField(
    question: ParticipantQuestionUiModel,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    modifier: Modifier = Modifier,
    attachment: QuestionAttachment? = null,
    leadingIcon: ImageVector? = null,
) {
    val enabled = !question.readOnly
    val missing = showError && question.isMissing(value, attachment?.file)
    val placeholder = question.placeholder

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FieldLabel(question.label, required = question.required, optional = !question.required && enabled)

        when (question.kind) {
            ParticipantQuestionKind.TEXT -> FlowTextInput(
                value, { onValueChange(it.take(question.maxLength)) }, placeholder,
                KeyboardType.Text, enabled, missing,
                leadingIcon = leadingIcon ?: Icons.Outlined.Edit,
            )

            ParticipantQuestionKind.NUMBER -> FlowTextInput(
                value, { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == '-' }.take(question.maxLength)) },
                placeholder, KeyboardType.Number, enabled, missing,
                leadingIcon = leadingIcon ?: Icons.Outlined.Tag,
            )

            ParticipantQuestionKind.LONG_TEXT -> LongTextInput(
                value, { onValueChange(it.take(question.maxLength)) }, placeholder, question.maxLength, enabled, missing,
                leadingIcon ?: Icons.Outlined.Description,
            )

            ParticipantQuestionKind.PHONE -> PhoneInput(value, onValueChange, enabled, missing)

            ParticipantQuestionKind.DATE -> DateInput(value, placeholder.ifBlank { "Select date" }, enabled, missing, onValueChange)

            ParticipantQuestionKind.DROPDOWN -> DropdownInput(
                value, question.options, placeholder.ifBlank { "Select" }, enabled, missing, onValueChange,
                leadingIcon = leadingIcon ?: Icons.Outlined.Layers,
            )

            ParticipantQuestionKind.MULTISELECT -> MultiSelectInput(value, question.options, enabled, missing, onValueChange)

            ParticipantQuestionKind.UNSUPPORTED -> Text(
                "This question type isn't supported by this version.",
                fontSize = 12.sp,
                color = TextSecondary,
            )

            ParticipantQuestionKind.FILE -> if (attachment != null) {
                FileInput(question.maxFileBytes, attachment)
            } else {
                Text("File upload isn't available on this step.", fontSize = 12.sp, color = TextSecondary)
            }
        }

        val message = when {
            question.kind == ParticipantQuestionKind.FILE && attachment?.error != null -> attachment.error
            missing -> "This field is required"
            else -> null
        }
        if (message != null) Text(message, fontSize = 12.sp, color = FlowDanger)
    }
}

@Composable
internal fun FieldLabel(text: String, required: Boolean, optional: Boolean = false) {
    Text(
        buildAnnotatedString {
            append(text)
            if (required) withStyle(SpanStyle(color = FlowDanger)) { append(" *") }
            if (optional) withStyle(SpanStyle(color = TextSecondary, fontWeight = FontWeight.Normal)) { append("  (Optional)") }
        },
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
    )
}

// ── Text-like inputs ──────────────────────────────────────────────────────────

@Composable
private fun flowFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = OrangePrimary,
    unfocusedBorderColor = FlowFieldBorder,
    errorBorderColor = FlowDanger,
    focusedContainerColor = CardBg,
    unfocusedContainerColor = CardBg,
    errorContainerColor = CardBg,
    cursorColor = OrangePrimary,
    disabledContainerColor = FlowDisabledBg,
    disabledBorderColor = FlowFieldBorder,
    disabledTextColor = TextSecondary,
)

private val InputTextStyle = TextStyle(fontSize = 15.sp, lineHeight = 21.sp)

@Composable
internal fun FlowTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        placeholder = if (placeholder.isBlank()) null else ({ Text(placeholder, fontSize = 15.sp) }),
        leadingIcon = leadingIcon?.let { icon -> { Icon(icon, contentDescription = null, tint = FlowIconTint, modifier = Modifier.size(22.dp)) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        textStyle = InputTextStyle,
        shape = FieldShape,
        colors = flowFieldColors(),
        modifier = modifier.fillMaxWidth(),
    )
}

@Composable
private fun LongTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    maxLength: Int,
    enabled: Boolean,
    isError: Boolean,
    icon: ImageVector,
) {
    var focused by remember { mutableStateOf(false) }
    val borderColor = when {
        isError -> FlowDanger
        focused -> OrangePrimary
        else -> FlowFieldBorder
    }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp)
                .clip(FieldShape)
                .background(if (enabled) CardBg else FlowDisabledBg)
                .border(if (focused) 1.5.dp else 1.dp, borderColor, FieldShape)
                .padding(horizontal = 14.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(icon, contentDescription = null, tint = FlowIconTint, modifier = Modifier.padding(top = 1.dp).size(22.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                textStyle = InputTextStyle.copy(color = if (enabled) TextPrimary else TextSecondary),
                cursorBrush = SolidColor(OrangePrimary),
                modifier = Modifier.weight(1f).onFocusChanged { focused = it.isFocused },
                decorationBox = { inner ->
                    Box {
                        if (value.isEmpty() && placeholder.isNotBlank()) {
                            Text(placeholder, style = InputTextStyle, color = TextSecondary)
                        }
                        inner()
                    }
                },
            )
        }
        Text(
            "${value.length} / $maxLength",
            fontSize = 13.sp,
            color = if (value.length >= maxLength) FlowDanger else TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp, end = 4.dp),
        )
    }
}

// Phone: "+91 98765 43210" is stored as one string; the code is everything before the first space.
private const val DEFAULT_COUNTRY_CODE = "+91"
private val CountryCodes = listOf("+91", "+1", "+44", "+61", "+65", "+971")

private fun splitPhone(value: String): Pair<String, String> {
    val trimmed = value.trim()
    return if (trimmed.startsWith("+")) {
        trimmed.substringBefore(' ') to trimmed.substringAfter(' ', "")
    } else {
        DEFAULT_COUNTRY_CODE to trimmed
    }
}

@Composable
private fun PhoneInput(value: String, onValueChange: (String) -> Unit, enabled: Boolean, isError: Boolean) {
    val (code, number) = splitPhone(value)

    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        DropdownInput(
            value = code,
            options = CountryCodes,
            placeholder = DEFAULT_COUNTRY_CODE,
            enabled = enabled,
            isError = false,
            onSelect = { onValueChange("$it $number".trim()) },
            modifier = Modifier.width(104.dp),
        )
        FlowTextInput(
            value = number,
            onValueChange = { raw -> onValueChange("$code ${raw.filter { it.isDigit() || it == ' ' }}".trim()) },
            placeholder = "",
            keyboardType = KeyboardType.Phone,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.weight(1f),
            leadingIcon = Icons.Outlined.Phone,
        )
    }
}

// ── Picker-style inputs (dropdown, date) share one field shell ────────────────

@Composable
private fun FieldShell(
    value: String,
    placeholder: String,
    trailingIcon: ImageVector,
    enabled: Boolean,
    isError: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(FieldShape)
            .background(if (enabled) CardBg else FlowDisabledBg)
            .border(1.dp, if (isError) FlowDanger else FlowFieldBorder, FieldShape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, contentDescription = null, tint = FlowIconTint, modifier = Modifier.size(22.dp))
        }
        Text(
            value.ifBlank { placeholder },
            fontSize = 15.sp,
            color = if (value.isBlank() || !enabled) TextSecondary else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Icon(trailingIcon, contentDescription = null, tint = TextSecondary)
    }
}

/** Dropdown built from a clickable field + [DropdownMenu] (avoids the version-sensitive exposed-dropdown API). */
@Composable
internal fun DropdownInput(
    value: String,
    options: List<String>,
    placeholder: String,
    enabled: Boolean,
    isError: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier) {
        FieldShell(
            value, placeholder, Icons.Outlined.KeyboardArrowDown, enabled, isError,
            onClick = { expanded = true },
            modifier = Modifier.onGloballyPositioned { fieldWidth = with(density) { it.size.width.toDp() } },
            leadingIcon = leadingIcon,
        )
        // The menu matches the field's width so long options (e.g. branch names) stay readable.
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.width(fieldWidth)) {
            options.forEach { option ->
                val selected = option == value
                DropdownMenuItem(
                    text = {
                        Text(
                            option,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) OrangePrimary else TextPrimary,
                        )
                    },
                    trailingIcon = if (selected) ({ Icon(Icons.Outlined.Check, null, tint = OrangePrimary) }) else null,
                    onClick = {
                        onSelect(option)
                        expanded = false
                    },
                )
            }
        }
    }
}

/** Date answers are stored as ISO `yyyy-MM-dd` and shown as `dd MMM yyyy`. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateInput(
    value: String,
    placeholder: String,
    enabled: Boolean,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val parsed = remember(value) { runCatching { LocalDate.parse(value) }.getOrNull() }

    FieldShell(parsed?.format(DisplayDate) ?: value, placeholder, Icons.Outlined.CalendarMonth, enabled, isError, { open = true })

    if (open) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = parsed?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        onValueChange(java.time.Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate().toString())
                    }
                    open = false
                }) { Text("OK", color = OrangePrimary) }
            },
            dismissButton = { TextButton(onClick = { open = false }) { Text("Cancel", color = TextSecondary) } },
        ) { DatePicker(state = state) }
    }
}

/** Selected options are stored newline-separated, in option order. */
@Composable
private fun MultiSelectInput(
    value: String,
    options: List<String>,
    enabled: Boolean,
    isError: Boolean,
    onValueChange: (String) -> Unit,
) {
    val selected = value.split('\n').filter { it.isNotBlank() }.toSet()

    Column(
        Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(if (enabled) CardBg else FlowDisabledBg)
            .border(1.dp, if (isError) FlowDanger else FlowFieldBorder, FieldShape),
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) HorizontalDivider(color = FlowFieldBorder.copy(alpha = 0.6f))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = enabled) {
                        val next = if (option in selected) selected - option else selected + option
                        onValueChange(options.filter { it in next }.joinToString("\n"))
                    }
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Checkbox(
                    checked = option in selected,
                    onCheckedChange = null,
                    enabled = enabled,
                    modifier = Modifier.padding(10.dp),
                    colors = CheckboxDefaults.colors(checkedColor = OrangePrimary),
                )
                Text(option, fontSize = 14.sp, color = TextPrimary)
            }
        }
    }
}

// ── File input ────────────────────────────────────────────────────────────────

@Composable
private fun FileInput(maxFileBytes: Long, attachment: QuestionAttachment) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) attachment.onPicked(readFileInfo(context, uri))
    }
    val file = attachment.file

    if (file == null) {
        val dash = OrangePrimary.copy(alpha = 0.6f)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FieldShape)
                .clickable { launcher.launch(arrayOf("*/*")) }
                .drawBehind {
                    drawRoundRect(
                        color = dash,
                        cornerRadius = CornerRadius(10.dp.toPx()),
                        style = Stroke(width = 1.5.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f))),
                    )
                }
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Icon(Icons.Outlined.UploadFile, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(24.dp))
            Text("Choose file", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = OrangePrimary)
            Text("Max ${maxFileBytes / (1024 * 1024)} MB", fontSize = 12.sp, color = TextSecondary)
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FieldShape)
                .background(CardBg)
                .border(1.dp, FlowFieldBorder, FieldShape)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                Modifier.size(36.dp).clip(RoundedCornerShape(8.dp)).background(OrangeLight),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Description, contentDescription = null, tint = OrangePrimary, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(file.name, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (file.readableSize.isNotEmpty()) Text(file.readableSize, fontSize = 12.sp, color = TextSecondary)
            }
            IconButton(onClick = attachment.onRemoved) {
                Icon(Icons.Outlined.Close, contentDescription = "Remove file", tint = TextSecondary)
            }
        }
    }
}

private fun readFileInfo(context: Context, uri: Uri): AttachedFile {
    var name = "attachment"
    var size = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: name
            val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) size = cursor.getLong(sizeIndex)
        }
    }
    return AttachedFile(uri = uri.toString(), name = name, sizeBytes = size)
}


