package com.example.campusconnect.feature.registrations.userflow.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ConfettiColors = listOf(
    Color(0xFFF2570F), Color(0xFFFFB300), Color(0xFFE53935), Color(0xFFFF8A65), Color(0xFFFFD54F),
)
private val ReceiptBg = Color(0xFFFBF3EE)
private val CompletedBg = Color(0xFFDDF6EA)
private val CompletedFg = Color(0xFF087443)

/** "Registration Submitted!" with the receipt and the two follow-up actions. */
@Composable
fun RegistrationSuccessScreen(
    eventName: String,
    receipt: SubmissionReceipt,
    onViewRegistration: () -> Unit,
    onBackToEvent: () -> Unit,
) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val checkScale by animateFloatAsState(
        targetValue = if (shown) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "checkScale",
    )

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
        Box(Modifier.size(200.dp).scale(checkScale), contentAlignment = Alignment.Center) {
            Confetti(Modifier.fillMaxSize())
            Box(Modifier.size(112.dp).background(OrangeLight, CircleShape))
            Box(
                Modifier.size(84.dp).background(OrangePrimary, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Outlined.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
        }

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
    val completed = receipt.status.equals("Completed", ignoreCase = true)

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
                    Icon(Icons.Outlined.CheckCircle, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Text("Status", fontSize = 12.sp, color = TextSecondary)
                }
                Surface(shape = RoundedCornerShape(50), color = if (completed) CompletedBg else OrangeLight) {
                    Text(
                        receipt.status,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (completed) CompletedFg else OrangePrimary,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    )
                }
            }
        }
    }
}

/** Short bursts and dots radiating around the check; deterministic so it doesn't reshuffle on recomposition. */
@Composable
private fun Confetti(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        val base = size.minDimension * 0.36f
        val count = 18
        for (i in 0 until count) {
            val angle = 2 * PI * i / count + (if (i % 2 == 0) 0.0 else 0.12)
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            val color = ConfettiColors[i % ConfettiColors.size]
            val start = base + (i % 3) * 6.dp.toPx()
            if (i % 3 == 0) {
                drawCircle(color, radius = 3.5.dp.toPx(), center = center + Offset(dx, dy) * (start + 12.dp.toPx()))
            } else {
                val length = (10 + (i % 4) * 4).dp.toPx()
                drawLine(
                    color = color,
                    start = center + Offset(dx, dy) * start,
                    end = center + Offset(dx, dy) * (start + length),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round,
                )
            }
        }
    }
}
