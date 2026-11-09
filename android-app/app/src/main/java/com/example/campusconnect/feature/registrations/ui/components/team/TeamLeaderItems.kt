package com.example.campusconnect.feature.registrations.ui.components.team

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary

@Composable
internal fun TeamMemberItem(name: String, status: String, isLeader: Boolean) {
    Surface(shape = RoundedCornerShape(12.dp), color = CardBg) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(name, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(status, fontSize = 12.sp, color = TextSecondary)
            }
            if (isLeader) Text("Leader", color = OrangePrimary, fontSize = 12.sp)
        }
    }
}

@Composable
internal fun LeaderJoinRequestItem(
    participantName: String,
    pending: Boolean,
    onRespond: (accept: Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(12.dp), color = CardBg) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(participantName, Modifier.weight(1f), color = TextPrimary)
            TextButton(enabled = !pending, onClick = { onRespond(false) }) { Text("Reject") }
            Button(enabled = !pending, onClick = { onRespond(true) }) { Text("Accept") }
        }
    }
}

@Composable
internal fun LeaderInviteCandidateItem(
    displayName: String,
    enabled: Boolean,
    onInvite: () -> Unit,
) {
    Surface(shape = RoundedCornerShape(12.dp), color = CardBg) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(displayName, Modifier.weight(1f), color = TextPrimary)
            Button(enabled = enabled, onClick = onInvite) { Text("Invite") }
        }
    }
}
