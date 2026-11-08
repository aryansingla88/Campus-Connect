package com.example.campusconnect.feature.registrations.ui.components.builder

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDownCircle
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Pin
import androidx.compose.material.icons.outlined.ShortText
import androidx.compose.material.icons.outlined.Subject
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.campusconnect.feature.registrations.model.QuestionType

internal val builderQuestionTypes = listOf(
    QuestionType.Known.TEXT,
    QuestionType.Known.NUMBER,
    QuestionType.Known.SELECT,
    QuestionType.Known.MULTISELECT,
    QuestionType.Known.DATE,
)

internal val QuestionType.presentationLabel: String
    get() = when (this) {
        is QuestionType.Unknown -> "Unsupported (${apiValue})"
        QuestionType.Known.TEXT -> "Short text"
        QuestionType.Known.TEXTAREA, QuestionType.Known.LONG_TEXT, QuestionType.Known.PARAGRAPH -> "Long text"
        QuestionType.Known.NUMBER -> "Number"
        QuestionType.Known.PHONE -> "Phone"
        QuestionType.Known.SELECT, QuestionType.Known.DROPDOWN -> "Dropdown"
        QuestionType.Known.MULTISELECT -> "Checkboxes"
        QuestionType.Known.DATE -> "Date"
        QuestionType.Known.FILE, QuestionType.Known.FILE_UPLOAD -> "File upload"
    }

internal val QuestionType.presentationIcon: ImageVector
    get() = when (this) {
        is QuestionType.Unknown -> Icons.Outlined.HelpOutline
        QuestionType.Known.TEXT -> Icons.Outlined.ShortText
        QuestionType.Known.TEXTAREA, QuestionType.Known.LONG_TEXT, QuestionType.Known.PARAGRAPH -> Icons.Outlined.Subject
        QuestionType.Known.NUMBER -> Icons.Outlined.Pin
        QuestionType.Known.PHONE -> Icons.Outlined.Phone
        QuestionType.Known.SELECT, QuestionType.Known.DROPDOWN -> Icons.Outlined.ArrowDropDownCircle
        QuestionType.Known.MULTISELECT -> Icons.Outlined.CheckBox
        QuestionType.Known.DATE -> Icons.Outlined.CalendarMonth
        QuestionType.Known.FILE, QuestionType.Known.FILE_UPLOAD -> Icons.Outlined.AttachFile
    }

internal fun QuestionType.supportsPlaceholder(): Boolean =
    this == QuestionType.Known.TEXT || this == QuestionType.Known.NUMBER

internal fun QuestionType.supportsOptions(): Boolean =
    this == QuestionType.Known.SELECT || this == QuestionType.Known.DROPDOWN || this == QuestionType.Known.MULTISELECT
