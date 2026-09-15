package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun ResetPasswordDialog(

    newPassword: String,

    confirmPassword: String,

    warning: String,

    isLoading: Boolean,

    onNewPasswordChange: (String) -> Unit,

    onConfirmPasswordChange: (String) -> Unit,

    onResetPassword: () -> Unit,

    onDismiss: () -> Unit
) {

    AlertDialog(

        onDismissRequest = onDismiss,

        title = {
            Text("Create New Password")
        },

        text = {

            Column {

                Text(
                    "Enter your new password."
                )

                Spacer(
                    modifier =
                        Modifier.height(16.dp)
                )

                OutlinedTextField(

                    value = newPassword,

                    onValueChange =
                        onNewPasswordChange,

                    label = {
                        Text("New Password")
                    },

                    singleLine = true,

                    visualTransformation =
                        PasswordVisualTransformation(),

                    modifier =
                        Modifier.fillMaxWidth()
                )

                Spacer(
                    modifier =
                        Modifier.height(12.dp)
                )

                OutlinedTextField(

                    value = confirmPassword,

                    onValueChange =
                        onConfirmPasswordChange,

                    label = {
                        Text("Confirm Password")
                    },

                    singleLine = true,

                    visualTransformation =
                        PasswordVisualTransformation(),

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

                onClick = onResetPassword,

                enabled = !isLoading
            ) {

                if (isLoading) {

                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp)
                    )

                } else {

                    Text("Reset Password")
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