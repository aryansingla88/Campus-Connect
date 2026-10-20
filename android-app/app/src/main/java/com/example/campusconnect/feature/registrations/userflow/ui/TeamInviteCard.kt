package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.TeamInvite

/** Invitation card: team, leader, member count, skill tags, and full-width Accept / Reject. */
@Composable
internal fun TeamInviteCard(
    invite: TeamInvite,
    pending: Boolean,
    onResponse: (accept: Boolean) -> Unit,
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = BorderStroke(1.dp, Color(0xFFEDEEF0)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppAvatar(entityId = invite.id, displayName = invite.teamName, size = 52.dp)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        invite.teamName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text("Leader: ${invite.invitedBy}", fontSize = 13.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${invite.memberCount}/${invite.maxMembers} members", fontSize = 13.sp, color = TextSecondary)
                }
            }

            if (invite.tags.isNotEmpty()) TagChips(invite.tags)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { onResponse(true) },
                    enabled = !pending,
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
                    modifier = Modifier.weight(1f).height(44.dp),
                ) {
                    if (pending) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    else Text("Accept", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                OutlinedButton(
                    onClick = { onResponse(false) },
                    enabled = !pending,
                    shape = RoundedCornerShape(24.dp),
                    border = BorderStroke(1.dp, OrangePrimary),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                    modifier = Modifier.weight(1f).height(44.dp),
                ) { Text("Reject", fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}
