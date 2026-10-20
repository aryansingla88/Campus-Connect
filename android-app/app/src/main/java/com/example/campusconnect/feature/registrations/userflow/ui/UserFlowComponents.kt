package com.example.campusconnect.feature.registrations.userflow.ui

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
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
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
import com.example.campusconnect.feature.registrations.userflow.model.USER_REGISTRATION_TOTAL_STEPS
import com.example.campusconnect.feature.registrations.ui.screens.FieldType
import com.example.campusconnect.feature.registrations.ui.screens.FormField
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
// Every participant question, individual or team, is rendered through RegistrationQuestionField so the
// label, required/optional marker, input style, error text and spacing are identical on all steps.

private val FieldShape = RoundedCornerShape(10.dp)
private val DisplayDate: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.getDefault())

@Composable
internal fun RegistrationQuestionField(
    question: FormField,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
) {
    val enabled = !question.readOnly
    val hasError = error != null

    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        FieldLabel(question.label, required = question.isRequired, optional = !question.isRequired && enabled)

        when (question.fieldType) {
            FieldType.TEXT -> FlowTextInput(value, onValueChange, question.placeholder, KeyboardType.Text, enabled, hasError)
            FieldType.NUMBER -> FlowTextInput(
                value,
                { raw -> onValueChange(raw.filter { it.isDigit() || it == '.' || it == '-' }) },
                question.placeholder,
                KeyboardType.Number,
                enabled,
                hasError,
            )
            FieldType.SELECT -> DropdownInput(
                value, question.options, question.placeholder.ifBlank { "Select" }, enabled, hasError, onValueChange,
            )
            FieldType.MULTISELECT -> MultiSelectInput(value, question.options, enabled, hasError, onValueChange)
            FieldType.DATE -> DateInput(value, question.placeholder.ifBlank { "Select date" }, enabled, hasError, onValueChange)
        }

        error?.let { Text(it, fontSize = 12.sp, color = FlowDanger) }
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
