package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite
import kotlin.math.abs

/** Invite card: team, "invited you", time, member avatar stack, and Decline / Accept. */
@Composable
internal fun TeamInviteCard(
    invite: TeamInvite,
    pending: Boolean,
    onResponse: (accept: Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color(0xFFEDEEF0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppAvatar(entityId = invite.id, displayName = invite.teamName, size = 52.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        invite.teamName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("Invited you to join their team", fontSize = 13.sp, color = TextSecondary)
                }
                if (invite.sentAgo.isNotBlank()) {
                    Text(invite.sentAgo, fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                MemberAvatarStack(invite.memberNames)
                Spacer(Modifier.weight(1f))
                Text("${invite.memberCount}/${invite.maxMembers} members", fontSize = 13.sp, color = TextSecondary)
                Spacer(Modifier.weight(1f))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { onResponse(false) },
                    enabled = !pending,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFB8BCC4)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    modifier = Modifier.weight(1f).height(46.dp),
                ) { Text("Decline", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = { onResponse(true) },
                    enabled = !pending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(46.dp),
                ) {
                    if (pending) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    } else {
                        Text("Accept", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

/** Up to three overlapping circular avatars. Draws nothing when names are unknown. */
@Composable
private fun MemberAvatarStack(names: List<String>, size: Dp = 32.dp, overlap: Dp = 10.dp) {
    val shown = names.take(3)
    if (shown.isEmpty()) return

    val step = size - overlap
    Box(Modifier.width(size + step * (shown.size - 1)).height(size)) {
        shown.forEachIndexed { index, name ->
            Box(
                Modifier
                    .offset(x = step * index)
                    .size(size)
                    .border(2.dp, Color.White, CircleShape)
            ) {
                AppAvatar(entityId = abs(name.hashCode()), displayName = name, size = size)
            }
        }
    }
}
