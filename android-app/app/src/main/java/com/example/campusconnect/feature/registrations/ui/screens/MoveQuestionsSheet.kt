package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Bottom sheet for moving existing individual questions into the team questions:
 * search, multi-select with checkboxes, and a "Move N Questions" action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoveQuestionsSheet(
    candidates: List<FormField>,
    onDismiss: () -> Unit,
    onConfirm: (ids: List<Int>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf("") }
    var selectedIds by remember { mutableStateOf(emptySet<Int>()) }

    val visible = remember(candidates, query) {
        candidates.filter { query.isBlank() || it.label.contains(query.trim(), ignoreCase = true) }
    }

    // Slide the sheet out first, then report the result.
    fun closeThen(action: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) action()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardBg,
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .navigationBarsPadding(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Move to Team Questions",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                )
                IconButton(onClick = { closeThen(onDismiss) }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Close", tint = TextPrimary)
                }
            }
            Text(
                "Select questions from your individual questions and move them to the team.",
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = TextSecondary,
            )
            Spacer(Modifier.height(14.dp))

            TextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search questions...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF1F1F4),
                    unfocusedContainerColor = Color(0xFFF1F1F4),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = OrangePrimary,
                ),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(6.dp))

            when {
                candidates.isEmpty() -> SheetMessage("No individual questions to move.")
                visible.isEmpty() -> SheetMessage("No matching questions.")
                else -> LazyColumn(Modifier.weight(1f, fill = false)) {
                    items(visible, key = { it.id }) { field ->
                        MoveQuestionRow(
                            field = field,
                            checked = field.id in selectedIds,
                            onToggle = {
                                selectedIds = if (field.id in selectedIds) selectedIds - field.id else selectedIds + field.id
                            },
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Button(
                enabled = selectedIds.isNotEmpty(),
                onClick = {
                    val ids = candidates.filter { it.id in selectedIds }.map { it.id }
                    closeThen { onConfirm(ids) }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = Color.White,
                ),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) {
                Text(
                    when (selectedIds.size) {
                        0 -> "Move Questions"
                        1 -> "Move 1 Question"
                        else -> "Move ${selectedIds.size} Questions"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun MoveQuestionRow(field: FormField, checked: Boolean, onToggle: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null, // the whole row toggles
            colors = CheckboxDefaults.colors(
                checkedColor = OrangePrimary,
                uncheckedColor = Color(0xFF616161),
            ),
        )
        Column(Modifier.weight(1f)) {
            Text(
                field.label.ifBlank { "Untitled question" },
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (field.label.isBlank()) TextSecondary else TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (field.placeholder.isNotBlank()) {
                Text(
                    field.placeholder,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Surface(shape = RoundedCornerShape(8.dp), color = OrangeLight) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Icon(field.fieldType.icon, null, modifier = Modifier.size(13.dp), tint = OrangePrimary)
                Text(field.fieldType.label, fontSize = 11.sp, color = OrangePrimary, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun SheetMessage(text: String) {
    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Text(text, fontSize = 14.sp, color = TextSecondary)
    }
}
