package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TeamQuestionsScreen(
    eventName: String,
    isPublished: Boolean,
    onBack: () -> Unit,
) {
    var questions by remember {
        mutableStateOf(
            listOf(
                TeamQuestion(1, "Team Description", "Long Answer", true),
                TeamQuestion(2, "Team Size (Confirm)", "Number", true),
                TeamQuestion(3, "Project Domain", "Single Choice", true),
                TeamQuestion(4, "Any Previous Experience?", "Single Choice", false),
            )
        )
    }
    var nextId by remember { mutableIntStateOf(5) }
    var editingQuestion by remember { mutableStateOf<TeamQuestion?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    if (showAddDialog) {
        AddTeamQuestionDialog(
            onDismiss = { showAddDialog = false },
            onSave = { label, type, required ->
                questions = questions + TeamQuestion(nextId++, label, type, required)
                showAddDialog = false
            },
        )
    }

    editingQuestion?.let { question ->
        AddTeamQuestionDialog(
            initialLabel = question.label,
            initialType = question.type,
            initialRequired = question.required,
            title = "Edit team question",
            confirmLabel = "Save",
            onDismiss = { editingQuestion = null },
            onSave = { label, type, required ->
                questions = questions.map {
                    if (it.id == question.id) it.copy(label = label, type = type, required = required) else it
                }
                editingQuestion = null
            },
        )
    }

    Scaffold(
        containerColor = PageBg,
        topBar = { RegistrationHeader(eventName, isPublished, onBack) },
        bottomBar = {
            RegistrationBottomBar {
                OutlinedButton(
                    onClick = { showAddDialog = true },
                    border = BorderStroke(1.5.dp, OrangePrimary),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Add Question", fontWeight = FontWeight.Medium)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text("Team registration questions", fontSize = 23.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(
                    "These questions will be answered once per team during registration.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
            }

            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0EEFF),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = Color(0xFF4F46E5), modifier = Modifier.size(22.dp))
                        Column {
                            Text("Team Name is added by default.", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF312E81))
                            Text("It is required for every team and cannot be edited or removed.", fontSize = 12.sp, color = Color(0xFF4C4A7A))
                        }
                    }
                }
            }

            item {
                LockedTeamNameCard()
            }

            itemsIndexed(questions, key = { _, question -> question.id }) { index, question ->
                TeamQuestionCard(
                    question = question,
                    index = index + 2,
                    onDelete = { questions = questions.filter { it.id != question.id } },
                    onEdit = { editingQuestion = question },
                )
            }
        }
    }
}

private data class TeamQuestion(
    val id: Int,
    val label: String,
    val type: String,
    val required: Boolean,
)

@Composable
private fun LockedTeamNameCard() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FB)),
        border = BorderStroke(1.dp, Color(0xFFDDE2EA)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(Icons.Outlined.DragIndicator, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text("Team Name", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Icon(Icons.Outlined.Lock, null, tint = TextSecondary, modifier = Modifier.size(15.dp))
                }
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuestionTag("Short Answer")
                    QuestionTag("Required")
                    QuestionTag("Default")
                }
            }
        }
    }
}

@Composable
private fun TeamQuestionCard(
    question: TeamQuestion,
    index: Int,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Icon(Icons.Outlined.DragIndicator, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(question.label, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(5.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    QuestionTag(question.type)
                    QuestionTag(if (question.required) "Required" else "Optional")
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Outlined.Edit, "Edit question", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.DeleteOutline, "Delete question", tint = TextPrimary, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun QuestionTag(text: String) {
    Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFFF0F2F5)) {
        Text(text, fontSize = 11.sp, color = Color(0xFF445064), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
private fun AddTeamQuestionDialog(
    initialLabel: String = "",
    initialType: String = "Short Answer",
    initialRequired: Boolean = false,
    title: String = "Add team question",
    confirmLabel: String = "Add",
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean) -> Unit,
) {
    var label by remember { mutableStateOf(initialLabel) }
    var type by remember { mutableStateOf(initialType) }
    var required by remember { mutableStateOf(initialRequired) }
    val types = listOf("Short Answer", "Long Answer", "Number", "Single Choice", "Multiple Choice", "Date")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Question") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("Question type", fontSize = 12.sp, color = TextSecondary)
                types.forEach { option ->
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable { type = option }.padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = type == option, onClick = { type = option })
                        Text(option, fontSize = 14.sp, color = TextPrimary)
                    }
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Required", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("Team must answer this question", fontSize = 12.sp, color = TextSecondary)
                    }
                    Switch(
                        checked = required,
                        onCheckedChange = { required = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = OrangePrimary,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color(0xFFBDBDBD),
                        ),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = label.isNotBlank(), onClick = { onSave(label.trim(), type, required) }) { Text(confirmLabel, color = OrangePrimary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        containerColor = CardBg,
    )
}

