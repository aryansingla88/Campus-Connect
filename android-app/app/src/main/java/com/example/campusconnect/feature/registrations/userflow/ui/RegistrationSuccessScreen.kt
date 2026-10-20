package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.OrangeLight
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.userflow.model.SubmissionReceipt
private val ReceiptBg = Color(0xFFFBF3EE)

/** "Registration Submitted!" with the receipt and the two follow-up actions. */
@Composable
fun RegistrationSuccessScreen(
    eventName: String,
    receipt: SubmissionReceipt,
    onViewRegistration: () -> Unit,
    onBackToEvent: () -> Unit,
    teamPortalActionLabel: String? = null,
    onTeamPortal: (() -> Unit)? = null,
) {
    var countdown by remember { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        while (countdown > 0) {
            kotlinx.coroutines.delay(1_000)
            countdown--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CardBg)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(Modifier.size(112.dp).background(ReceiptBg, CircleShape), contentAlignment = Alignment.Center) {
            Text(if (countdown > 0) countdown.toString() else "0", fontSize = 42.sp,
                fontWeight = FontWeight.Bold, color = OrangePrimary)
        }
        Spacer(Modifier.height(4.dp))
        Text(
            if (countdown > 0) "Registration received · $countdown" else "Registration received",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(12.dp))
        Text("Registration Submitted!", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Spacer(Modifier.height(8.dp))
        Text(
            "You have successfully registered for $eventName.",
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(24.dp))
        ReceiptCard(receipt)

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onViewRegistration,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary, contentColor = Color.White),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("View My Registration", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }

        if (teamPortalActionLabel != null && onTeamPortal != null) {
            Spacer(Modifier.height(12.dp))
            OutlinedButton(
                onClick = onTeamPortal,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, OrangePrimary),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text(teamPortalActionLabel, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedButton(
            onClick = onBackToEvent,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, OrangePrimary),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
            modifier = Modifier.fillMaxWidth().height(52.dp),
        ) { Text("Back to Event", fontSize = 16.sp, fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun ReceiptCard(receipt: SubmissionReceipt) {
    Surface(shape = RoundedCornerShape(14.dp), color = ReceiptBg, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Registration ID", fontSize = 12.sp, color = TextSecondary)
                Text(receipt.registrationCode, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column(horizontalAlignment = Alignment.Start, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Status", fontSize = 12.sp, color = TextSecondary)
                }
                Surface(shape = RoundedCornerShape(50), color = OrangeLight) {
                    Text(
                        "Pending",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OrangePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

