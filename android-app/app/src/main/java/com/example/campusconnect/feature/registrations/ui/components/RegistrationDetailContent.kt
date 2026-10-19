package com.example.campusconnect.feature.registrations.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.QuickFieldValue
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationMemberDetail
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import com.example.campusconnect.feature.registrations.model.shortName

/** How much of a registration's detail to show. [Full] has no limits or truncation. */
data class DetailLimits(
    val maxSharedQuestions: Int? = null,
    val maxIndividualQuestions: Int? = null,
    val collapsedAnswerLines: Int? = null,
) {
    companion object {
        val Inline = DetailLimits(
            maxSharedQuestions = 3,
            maxIndividualQuestions = 2,
            collapsedAnswerLines = 3,
        )
        val Full = DetailLimits()
    }
}

/**
 * Shared answers, then the member selector, then the selected member's quick
 * fields and individual answers. Position alone separates shared from
 * member-specific content, so there are no section headings.
 */
@Composable
fun RegistrationDetailBody(
    registrationId: Int,
    detail: RegistrationDetailResponse,
    selectedUserId: Int?,
    onSelectMember: (Int) -> Unit,
    limits: DetailLimits,
    modifier: Modifier = Modifier,
) {
    if (detail.isEmpty) {
        Text(
            "No responses submitted.",
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = modifier.fillMaxWidth().padding(vertical = 8.dp),
        )
        return
    }

    val member = detail.members.firstOrNull { it.userId == selectedUserId } ?: detail.members.firstOrNull()
    val sharedAnswers = detail.teamAnswers.limited(limits.maxSharedQuestions)
    val individualAnswers = member?.individualAnswers.orEmpty().limited(limits.maxIndividualQuestions)

    Column(modifier = modifier.fillMaxWidth()) {
        var needsDivider = false

        if (sharedAnswers.isNotEmpty()) {
            AnswerList(
                answers = sharedAnswers,
                keyPrefix = "$registrationId-shared",
                collapsedLines = limits.collapsedAnswerLines,
            )
            needsDivider = true
        }

        if (member != null) {
            if (needsDivider) SubtleDivider()

            if (detail.members.size > 1) {
                MemberSelector(
                    members = detail.members,
                    selectedUserId = member.userId,
                    leaderUserId = detail.leaderUserId,
                    onSelect = onSelectMember,
                )
                SubtleDivider()
            }

            if (member.quickFields.isNotEmpty()) {
                QuickFieldsGrid(
                    fields = member.quickFields,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            }

            if (individualAnswers.isNotEmpty()) {
                if (member.quickFields.isNotEmpty()) SubtleDivider()
                AnswerList(
                    answers = individualAnswers,
                    keyPrefix = "$registrationId-${member.userId}",
                    collapsedLines = limits.collapsedAnswerLines,
                )
            }
        }
    }
}

private fun <T> List<T>.limited(max: Int?): List<T> = if (max == null) this else take(max)

// ── Questions & answers ───────────────────────────────────────────────────────

@Composable
private fun AnswerList(
    answers: List<ResponseAnswer>,
    keyPrefix: String,
    collapsedLines: Int?,
) {
    Column(Modifier.fillMaxWidth()) {
        answers.forEachIndexed { index, answer ->
            if (index > 0) SubtleDivider()
            Column(Modifier.padding(vertical = 10.dp)) {
                Text(
                    answer.fieldLabel,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(2.dp))
                AnswerText(
                    text = answer.answer.ifBlank { "—" },
                    stateKey = "$keyPrefix-${answer.fieldId}",
                    collapsedLines = collapsedLines,
                )
            }
        }
    }
}

/**
 * Plain-text answer. With [collapsedLines] set, long answers clamp to that many
 * lines with an ellipsis and a Read more / Show less toggle that expands in place.
 */
@Composable
private fun AnswerText(text: String, stateKey: String, collapsedLines: Int?) {
    if (collapsedLines == null) {
        Text(text, fontSize = 13.sp, lineHeight = 18.sp, color = TextSecondary)
        return
    }

    // Keyed on stateKey (registration + member + question) so switching member resets it.
    var expanded by rememberSaveable(stateKey) { mutableStateOf(false) }
    var overflows by remember(stateKey, text) { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth().animateContentSize()) {
        Text(
            text,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = TextSecondary,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedLines,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) overflows = it.hasVisualOverflow },
        )
        if (overflows) {
            Text(
                if (expanded) "Show less" else "Read more",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AccentPurple,
                modifier = Modifier
                    .clickable { expanded = !expanded }
                    .padding(top = 4.dp, bottom = 2.dp),
            )
        }
    }
}

// ── Members ───────────────────────────────────────────────────────────────────

@Composable
private fun MemberSelector(
    members: List<RegistrationMemberDetail>,
    selectedUserId: Int,
    leaderUserId: Int?,
    onSelect: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        members.forEachIndexed { index, member ->
            if (index > 0) {
                Box(Modifier.width(1.dp).height(16.dp).background(CardBorder))
            }
            MemberTab(
                name = member.shortName(index),
                isLeader = member.userId == leaderUserId,
                selected = member.userId == selectedUserId,
                onClick = { onSelect(member.userId) },
            )
        }
    }
}

@Composable
private fun MemberTab(name: String, isLeader: Boolean, selected: Boolean, onClick: () -> Unit) {
    val underline by animateColorAsState(if (selected) AccentPurple else Color.Transparent, label = "memberUnderline")

    Column(
        modifier = Modifier.clickable(onClick = onClick).padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLeader) {
                Icon(CrownIcon, contentDescription = "Team leader", tint = CrownGold, size = 14.dp)
                Spacer(Modifier.width(4.dp))
            }
            Text(
                name,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) AccentPurple else TextSecondary,
                maxLines = 1,
            )
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(underline))
    }
}

@Composable
private fun Icon(image: ImageVector, contentDescription: String?, tint: Color, size: Dp) {
    androidx.compose.material3.Icon(
        imageVector = image,
        contentDescription = contentDescription,
        tint = tint,
        modifier = Modifier.size(size),
    )
}

/** Small crown marking the team leader (Material has no crown glyph). */
private val CrownIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "Crown",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(5f, 16f)
            lineTo(3f, 5f)
            lineTo(8.5f, 10f)
            lineTo(12f, 4f)
            lineTo(15.5f, 10f)
            lineTo(21f, 5f)
            lineTo(19f, 16f)
            close()
            moveTo(5f, 18f)
            lineTo(19f, 18f)
            lineTo(19f, 20f)
            lineTo(5f, 20f)
            close()
        }
    }.build()
}

// ── Quick fields ──────────────────────────────────────────────────────────────

/** Two columns filled top-to-bottom (left column first), labels above values. */
@Composable
private fun QuickFieldsGrid(fields: List<QuickFieldValue>, modifier: Modifier = Modifier) {
    val rows = (fields.size + 1) / 2
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        repeat(rows) { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickFieldCell(fields[row], Modifier.weight(1f))
                val right = fields.getOrNull(row + rows)
                if (right != null) QuickFieldCell(right, Modifier.weight(1f)) else Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun QuickFieldCell(field: QuickFieldValue, modifier: Modifier) {
    Column(modifier) {
        Text(field.field, fontSize = 12.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(
            field.value.ifBlank { "—" },
            fontSize = 13.5.sp,
            color = TextPrimary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Bits ──────────────────────────────────────────────────────────────────────

@Composable
fun SubtleDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEDEEF0)))
}
