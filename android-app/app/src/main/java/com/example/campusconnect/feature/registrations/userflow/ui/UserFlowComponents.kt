package com.example.campusconnect.feature.registrations.userflow.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.KeyboardArrowDown
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeBorder
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.RegistrationBottomBar
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.AttachedFile
import com.example.campusconnect.feature.registrations.userflow.model.USER_REGISTRATION_TOTAL_STEPS
import com.example.campusconnect.feature.registrations.userflow.model.UserFieldKind
import com.example.campusconnect.feature.registrations.userflow.model.UserQuestion
import com.example.campusconnect.feature.registrations.userflow.model.isMissing
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

internal val FlowDanger = Color(0xFFD32F2F)
internal val FlowFieldBorder = Color(0xFFDADCE0)
internal val FlowDisabledBg = Color(0xFFF1F1F3)

// ── Top bar with step progress ────────────────────────────────────────────────

@Composable
internal fun FlowTopBar(
    title: String,
    onBack: () -> Unit,
    step: Int? = null,
    totalSteps: Int = USER_REGISTRATION_TOTAL_STEPS,
) {
    Surface(color = CardBg) {
        Column(Modifier.statusBarsPadding().fillMaxWidth().padding(bottom = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 4.dp),
                )
            }
            if (step != null) {
                val progress by animateFloatAsState(step / totalSteps.toFloat(), label = "stepProgress")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.weight(1f).height(5.dp).clip(RoundedCornerShape(50)),
                        color = OrangePrimary,
                        trackColor = OrangeBorder.copy(alpha = 0.5f),
                    )
                    Text("$step/$totalSteps", fontSize = 12.sp, color = TextSecondary)
                }
            }
        }
    }
}

/** Icon tile + title + subtitle used at the top of each step. */
@Composable
internal fun StepHeader(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    iconBackground: Color = OrangeLight,
    iconTint: Color = OrangePrimary,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(iconBackground),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
        Column {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(subtitle, fontSize = 13.sp, color = TextSecondary)
        }
    }
}

@Composable
internal fun NextBottomBar(onClick: () -> Unit, enabled: Boolean = true, label: String = "Next") {
    RegistrationBottomBar {
        Button(
            onClick = onClick,
            enabled = enabled,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = Color.White,
            ),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Outlined.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

// ── Question inputs ───────────────────────────────────────────────────────────
//
// Every participant question, individual or team, is rendered through UserQuestionField so the
// label, required/optional marker, input style, error text and spacing are identical on all steps.

private val FieldShape = RoundedCornerShape(10.dp)
private val DisplayDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

/** File state for FILE questions. Screens that cannot collect files leave it out. */
internal class QuestionAttachment(
    val file: AttachedFile?,
    val error: String?,
    val onPicked: (AttachedFile) -> Unit,
    val onRemoved: () -> Unit,
)

@Composable
internal fun UserQuestionField(
    question: UserQuestion,
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean,
    modifier: Modifier = Modifier,
    attachment: QuestionAttachment? = null,
) {
    val enabled = !question.readOnly
    val missing = showError && question.isMissing(value, attachment?.file)
    val placeholder = question.placeholder

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(question.label, required = question.required, optional = !question.required && enabled)

        when (question.kind) {
            UserFieldKind.TEXT -> FlowTextInput(
                value, { onValueChange(it.take(question.maxLength)) }, placeholder,
                KeyboardType.Text, enabled, missing,
            )

            UserFieldKind.NUMBER -> FlowTextInput(
                value, { onValueChange(it.filter { c -> c.isDigit() || c == '.' || c == '-' }.take(question.maxLength)) },
                placeholder, KeyboardType.Number, enabled, missing,
            )

            UserFieldKind.LONG_TEXT -> LongTextInput(
                value, { onValueChange(it.take(question.maxLength)) }, placeholder, question.maxLength, enabled, missing,
            )

            UserFieldKind.PHONE -> PhoneInput(value, onValueChange, enabled, missing)

            UserFieldKind.DATE -> DateInput(value, placeholder.ifBlank { "Select date" }, enabled, missing, onValueChange)

            UserFieldKind.DROPDOWN -> DropdownInput(
                value, question.options, placeholder.ifBlank { "Select" }, enabled, missing, onValueChange,
            )

            UserFieldKind.MULTISELECT -> MultiSelectInput(value, question.options, enabled, missing, onValueChange)

            UserFieldKind.FILE -> if (attachment != null) {
                FileInput(question.maxFileBytes, attachment)
            } else {
                Text("File upload isn't available on this step.", fontSize = 12.sp, color = TextSecondary)
            }
        }

        val message = when {
            question.kind == UserFieldKind.FILE && attachment?.error != null -> attachment.error
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
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
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

private val InputTextStyle = TextStyle(fontSize = 14.sp, lineHeight = 20.sp)

@Composable
private fun FlowTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    enabled: Boolean,
    isError: Boolean,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        enabled = enabled,
        isError = isError,
        singleLine = true,
        placeholder = if (placeholder.isBlank()) null else ({ Text(placeholder, fontSize = 14.sp) }),
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
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            isError = isError,
            minLines = 4,
            maxLines = 8,
            placeholder = if (placeholder.isBlank()) null else ({ Text(placeholder, fontSize = 14.sp) }),
            textStyle = InputTextStyle,
            shape = FieldShape,
            colors = flowFieldColors(),
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            "${value.length}/$maxLength",
            fontSize = 12.sp,
            color = if (value.length >= maxLength) FlowDanger else TextSecondary,
            textAlign = TextAlign.End,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
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
            modifier = Modifier.width(100.dp),
        )
        FlowTextInput(
            value = number,
            onValueChange = { raw -> onValueChange("$code ${raw.filter { it.isDigit() || it == ' ' }}".trim()) },
            placeholder = "",
            keyboardType = KeyboardType.Phone,
            enabled = enabled,
            isError = isError,
            modifier = Modifier.weight(1f),
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
    ) {
        Text(
            value.ifBlank { placeholder },
            fontSize = 14.sp,
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
) {
    var expanded by remember { mutableStateOf(false) }
    var fieldWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(modifier) {
        FieldShell(
            value, placeholder, Icons.Outlined.KeyboardArrowDown, enabled, isError,
            onClick = { expanded = true },
            modifier = Modifier.onGloballyPositioned { fieldWidth = with(density) { it.size.width.toDp() } },
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