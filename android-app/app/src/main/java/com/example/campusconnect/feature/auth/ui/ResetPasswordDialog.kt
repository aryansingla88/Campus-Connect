package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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

    // UI-only state (no logic change): toggles visibility per field
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFF5F6FA),
        unfocusedContainerColor = Color(0xFFF5F6FA),
        focusedBorderColor = Color.Transparent,
        unfocusedBorderColor = Color.Transparent,
        focusedTextColor = Color.Black,
        unfocusedTextColor = Color.Black
    )

    AlertDialog(

        onDismissRequest = onDismiss,

        containerColor = Color.White,

        shape = RoundedCornerShape(28.dp),

        title = {
            Text(
                text = "Create New Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
        },

        text = {

            Column {

                Text(
                    text = "Enter your new password.",
                    color = Color.Gray,
                    fontSize = 13.sp
                )

                Spacer(
                    modifier =
                        Modifier.height(20.dp)
                )

                OutlinedTextField(

                    value = newPassword,

                    onValueChange =
                        onNewPasswordChange,

                    placeholder = {
                        Text("New Password", color = Color.Gray)
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },

                    trailingIcon = {
                        val icon =
                            if (newPasswordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility

                        IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },

                    singleLine = true,

                    visualTransformation =
                        if (newPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),

                    shape = RoundedCornerShape(16.dp),

                    colors = fieldColors,

                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                )

                Spacer(
                    modifier =
                        Modifier.height(14.dp)
                )

                OutlinedTextField(

                    value = confirmPassword,

                    onValueChange =
                        onConfirmPasswordChange,

                    placeholder = {
                        Text("Confirm Password", color = Color.Gray)
                    },

                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color.Gray
                        )
                    },

                    trailingIcon = {
                        val icon =
                            if (confirmPasswordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility

                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        }
                    },

                    singleLine = true,

                    visualTransformation =
                        if (confirmPasswordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),

                    shape = RoundedCornerShape(16.dp),

                    colors = fieldColors,

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

                onClick = onResetPassword,

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
                        text = "Reset Password",
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