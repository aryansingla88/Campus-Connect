package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

@Composable
fun VerifyOtpDialog(

    email: String,

    otp: String,

    warning: String,

    isLoading: Boolean,

    onOtpChange: (String) -> Unit,

    onVerifyOtp: () -> Unit,

    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Verify OTP")
        },

        text = {

            Column {

                Text(
                    "Enter the 6-digit OTP sent to:"
                )

                Spacer(
                    modifier =
                        Modifier.height(4.dp)
                )

                Text(
                    text = email,
                    style =
                        MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                OutlinedTextField(

                    value = otp,

                    onValueChange = onOtpChange,

                    label = {
                        Text("6-digit OTP")
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Number
                        ),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                if (warning.isNotEmpty()) {

                    Spacer(
                        modifier =
                            Modifier.height(8.dp)
                    )

                    Text(
                        text = warning,
                        color =
                            MaterialTheme.colorScheme.error
                    )
                }
            }
        },

        confirmButton = {

            Button(

                onClick = onVerifyOtp,

                enabled = !isLoading
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp)
                    )

                } else {

                    Text("Verify")
                }
            }
        },

        dismissButton = {

            TextButton(
                onClick = onDismiss
            ) {

                Text("Cancel")
            }
        }
    )
}