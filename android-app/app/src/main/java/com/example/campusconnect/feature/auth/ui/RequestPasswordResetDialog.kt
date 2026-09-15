package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp

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

        title = {
            Text("Forgot Password")
        },

        text = {

            Column {

                Text(
                    "Enter your registered email address. " +
                            "We will send you a verification OTP."
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                OutlinedTextField(

                    value = email,

                    onValueChange = onEmailChange,

                    label = {
                        Text("Email")
                    },

                    singleLine = true,

                    keyboardOptions =
                        KeyboardOptions(
                            keyboardType =
                                KeyboardType.Email
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

                onClick = onSendOtp,

                enabled = !isLoading
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp)
                    )

                } else {

                    Text("Send OTP")
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