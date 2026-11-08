package com.example.campusconnect.feature.registrations.ui.components.responses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.ui.components.AccentPurple
import com.example.campusconnect.feature.registrations.ui.components.AccentPurpleLight
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.SubtleDivider
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationDetailState
@Composable
internal fun RegistrationResponseCard(
    response: RegistrationResponseItem,
    expanded: Boolean,
    detailState: RegistrationDetailState?,
    selectedUserId: Int?,
    onToggle: () -> Unit,
    onSelectMember: (Int) -> Unit,
    onRetry: () -> Unit,
    onFullResponse: () -> Unit,
    onReview: (Boolean) -> Unit,
    isReviewing: Boolean,
) {
    val arrowRotation by animateFloatAsState(if (expanded) -90f else 0f, label = "arrowRotation")

    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(if (expanded) 1.5.dp else 1.dp, if (expanded) AccentPurple else ResponseCardBorder),
        modifier = Modifier.fillMaxWidth().animateContentSize(),
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            RegistrationAvatar(response)

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    response.name?.takeIf(String::isNotBlank) ?: "Registrant",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(registrationTypeLabel(response), fontSize = 12.sp, color = TextSecondary)
                if (!response.email.isNullOrBlank()) Text(
                    response.email,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                response.status?.let { ResponseStatusChip(it) }
                response.submittedAt?.let { Text(it, fontSize = 10.sp, color = TextSecondary, maxLines = 1) }
            }

            IconButton(onClick = onToggle) {
                Icon(
                    Icons.Outlined.ChevronRight,
                    contentDescription = if (expanded) "Collapse response" else "Expand response",
                    tint = TextPrimary,
                    modifier = Modifier.size(20.dp).rotate(arrowRotation),
                )
            }
        }

        if (response.status == RegistrationResponseStatus.WAITLISTED) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp).padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End) {
                TextButton(enabled = !isReviewing, onClick = { onReview(false) }) { Text("Reject") }
                Button(enabled = !isReviewing, onClick = { onReview(true) }) {
                    if (isReviewing) CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                    else Text("Approve")
                }
            }
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut(),
        ) {
            Column(Modifier.padding(horizontal = 14.dp).padding(bottom = 12.dp)) {
                SubtleDivider()
                when (detailState) {
                    null, RegistrationDetailState.Loading -> Box(
                        Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = OrangePrimary,
                        )
                    }

                    is RegistrationDetailState.Failed -> Column(
                        Modifier.fillMaxWidth().padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(detailState.message, fontSize = 13.sp, color = TextSecondary, textAlign = TextAlign.Center)
                        TextButton(onClick = onRetry) { Text("Retry", color = AccentPurple) }
                    }

                    is RegistrationDetailState.Loaded -> {
                        RegistrationDetailBody(
                            registrationId = response.id,
                            detail = detailState.detail,
                            selectedUserId = selectedUserId,
                            onSelectMember = onSelectMember,
                            limits = DetailLimits.Inline,
                        )
                        if (!detailState.detail.isEmpty) {
                            Spacer(Modifier.height(4.dp))
                            FullResponseButton(onClick = onFullResponse)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullResponseButton(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = AccentPurpleLight,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.OpenInNew, contentDescription = null, tint = AccentPurple, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Full Response", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = AccentPurple)
        }
    }
}

internal fun registrationTypeLabel(response: RegistrationResponseItem): String =
    when (response.type) {
        RegistrationResponseType.TEAM -> "Team${response.memberCount?.let { " ($it members)" } ?: ""}"
        RegistrationResponseType.INDIVIDUAL -> "Individual"
        RegistrationResponseType.OTHER -> "Registration"
    }

@Composable
internal fun RegistrationAvatar(response: RegistrationResponseItem, size: Dp = 48.dp) {
    if (response.type == RegistrationResponseType.TEAM) {
        Box(
            modifier = Modifier.size(size).background(TeamsBg, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Outlined.Groups,
                contentDescription = null,
                tint = TeamsFg,
                modifier = Modifier.size(size * 0.54f),
            )
        }
    } else {
        AppAvatar(entityId = response.id, displayName = response.name ?: "Registrant", size = size)
    }
}

@Composable
internal fun ResponseStatusChip(status: RegistrationResponseStatus) {
    val background = when (status) {
        RegistrationResponseStatus.CONFIRMED -> Color(0xFFDDF6EA)
        RegistrationResponseStatus.PENDING -> Color(0xFFFFEFCF)
        RegistrationResponseStatus.REJECTED -> Color(0xFFFDE1E3)
        RegistrationResponseStatus.WAITLISTED -> Color(0xFFEDEBFC)
        RegistrationResponseStatus.UNKNOWN -> Color(0xFFF0F1F3)
    }
    val foreground = when (status) {
        RegistrationResponseStatus.CONFIRMED -> Color(0xFF087443)
        RegistrationResponseStatus.PENDING -> Color(0xFF9A5B00)
        RegistrationResponseStatus.REJECTED -> Color(0xFFC62828)
        RegistrationResponseStatus.WAITLISTED -> Color(0xFF6C4CF1)
        RegistrationResponseStatus.UNKNOWN -> TextSecondary
    }

    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = foreground,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
internal fun EmptyResponsesState(genuinelyEmpty: Boolean) {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Outlined.Inbox, null, tint = TextSecondary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text(if (genuinelyEmpty) "No registrations yet" else "No responses found", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text(
                if (genuinelyEmpty) "Submitted registrations will appear here." else "Try another search or filter.",
                fontSize = 12.sp,
                color = TextSecondary,
            )
        }
    }
}
