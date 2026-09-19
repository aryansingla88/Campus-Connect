package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RequestPasswordResetDialog(

    email: String,

    warning: String,

    isLoading: Boolean,

    onEmailChange: (String) -> Unit,

    onSendOtp: () -> Unit,

    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color.White,

        shape = RoundedCornerShape(28.dp),

        title = {
            Text(
                text = "Forgot Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },

        text = {

            Column {

                Text(
                    text = "Enter your registered email address. " +
                            "We will send you a verification OTP.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier = Modifier.height(20.dp)
                )

                OutlinedTextField(

                    value = email,

                    onValueChange = onEmailChange,

                    placeholder = {
                        Text("Email", color = Color.Gray)
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Email
                        ),

                    shape = RoundedCornerShape(16.dp),

                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F6FA),
                        unfocusedContainerColor = Color(0xFFF5F6FA),
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                )

                if (warning.isNotEmpty()) {

                    Spacer(
                        modifier =
                            Modifier.height(10.dp)
                    )

                    Text(
                        text = warning,
                        color = Color(0xFFD32F2F),
                        fontSize = 13.sp
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = onSendOtp,

                enabled = !isLoading,

                shape = RoundedCornerShape(20.dp),

                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF6B35)
                )
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )

                } else {

                    Text(
                        text = "Send OTP",
                        color = Color.White
                    )
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text(
                    text = "Cancel",
                    color = Color.Gray
                )
            }
        }
    )
}