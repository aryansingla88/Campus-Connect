package com.example.campusconnect.feature.events.ui.dialog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.campusconnect.feature.events.model.Event

private val OrangePrimary = Color(0xFFFF6F00)
private val DangerRed     = Color(0xFFD32F2F)

/**
 * Delete confirmation, styled like the rest of the app:
 * white 28dp card, red icon badge, centred copy, orange-outlined Cancel + red Delete pills.
 */
@Composable
fun DeleteEventDialog(
    event: Event,
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Surface(
            shape           = RoundedCornerShape(28.dp),
            color           = Color.White,
            tonalElevation  = 0.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(Color(0xFFFFF0F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = Icons.Default.Delete,
                        contentDescription = null,
                        tint               = DangerRed,
                        modifier           = Modifier.size(28.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text       = "Delete Event?",
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF1A1A1A),
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text       = buildAnnotatedString {
                        append("Are you sure you want to delete\n")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF1A1A1A))) {
                            append("“${event.title}”")
                        }
                        append("\nThis cannot be undone.")
                    },
                    fontSize   = 14.sp,
                    lineHeight = 20.sp,
                    color      = Color.Gray,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick  = onCancel,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(24.dp),
                        border   = BorderStroke(1.5.dp, OrangePrimary),
                        colors   = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                    ) {
                        Text("Cancel", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick  = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape    = RoundedCornerShape(24.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Delete", color = Color.White, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
