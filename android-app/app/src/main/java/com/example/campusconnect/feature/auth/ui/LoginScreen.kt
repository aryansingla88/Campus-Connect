/*
StateFlow is basically:
“A variable whose changes can be watched by other parts of the program.”

StateFlow is different
val x = MutableStateFlow(5)

Now when:

x.value = 10

anything watching x
immediately gets notified.
 */
package com.example.campusconnect.feature.auth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.R
import com.example.campusconnect.feature.auth.viewmodel.LoginViewModel
import com.example.campusconnect.feature.auth.viewmodel.PasswordResetViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    viewModel: LoginViewModel = viewModel(),
    passwordResetViewModel: PasswordResetViewModel = viewModel()
) {
//collectAsState means that donot collect a static value, collect as state, i.e. update as state changes
    val username by viewModel.username.collectAsState()
    val password by viewModel.password.collectAsState()
    val warning by viewModel.warning.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val resetStep by
    passwordResetViewModel
        .currentStep
        .collectAsState()

    val resetEmail by
    passwordResetViewModel
        .email
        .collectAsState()

    val resetOtp by
    passwordResetViewModel
        .otp
        .collectAsState()

    val newPassword by
    passwordResetViewModel
        .newPassword
        .collectAsState()

    val confirmPassword by
    passwordResetViewModel
        .confirmPassword
        .collectAsState()

    val resetWarning by
    passwordResetViewModel
        .warning
        .collectAsState()

    val resetLoading by
    passwordResetViewModel
        .isLoading
        .collectAsState()

    // UI-only state (no logic change): toggles password visibility in the field
    var passwordVisible by remember { mutableStateOf(false) }

    // navigation trigger
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier

        // If you have a real background photo (like the campus image in the mock),
        // swap the gradient above for:
        // Image(
        //     painter = painterResource(id = R.drawable.login_background),
        //     contentDescription = null,
        //     contentScale = ContentScale.Crop,
        //     modifier = Modifier.fillMaxSize()
        // )
    ) {

        // ───────── BACKGROUND IMAGE ─────────
        Image(
            painter = painterResource(R.drawable.login_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .offset(y = 37.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---- White rounded card ----
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    // Title
                    Text(
                        text = "Welcome Back",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A1A1A),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Sign in to continue to Campus Connect",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(28.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = viewModel::onUsernameChange,

                        placeholder = {
                            Text(
                                text = "Username",
                                color = Color.Gray
                            )
                        },

                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Gray
                            )
                        },

                        singleLine = true,

                        shape = RoundedCornerShape(16.dp),

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedContainerColor = Color(0xFFF5F6FA),
                            unfocusedContainerColor = Color(0xFFF5F6FA),

                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,

                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = viewModel::onPasswordChange,

                        placeholder = {
                            Text(
                                text = "Password",
                                color = Color.Gray
                            )
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
                                if (passwordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility

                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },

                        singleLine = true,

                        visualTransformation =
                            if (passwordVisible) VisualTransformation.None
                            else PasswordVisualTransformation(),

                        shape = RoundedCornerShape(16.dp),

                        colors = OutlinedTextFieldDefaults.colors(

                            focusedContainerColor = Color(0xFFF5F6FA),
                            unfocusedContainerColor = Color(0xFFF5F6FA),

                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,

                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),

                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Forgot password, right-aligned
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Color(0xFFFF7A3D),
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                passwordResetViewModel.startPasswordReset()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Submit button
                    Button(
                        onClick = { viewModel.login() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35)
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Submit",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // "OR" divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0)
                        )
                        Text(
                            text = "  OR  ",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFFE0E0E0)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Register text
                    Row {
                        Text(
                            text = "New user? ",
                            color = Color(0xFF444444),
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Register",
                            color = Color(0xFFFF6B35),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                onNavigateToRegister()
                            }
                        )
                    }

                    // Warning
                    if (warning.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = warning,
                            color = Color(0xFFD32F2F),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // ---- Password reset flow (logic unchanged) ----
        when (resetStep) {

            PasswordResetViewModel.ResetStep.EMAIL -> {

                RequestPasswordResetDialog(

                    email = resetEmail,

                    warning = resetWarning,

                    isLoading = resetLoading,

                    onEmailChange =
                        passwordResetViewModel::onEmailChange,

                    onSendOtp =
                        passwordResetViewModel::requestOtp,

                    onDismiss =
                        passwordResetViewModel::closePasswordReset
                )
            }


            PasswordResetViewModel.ResetStep.OTP -> {

                VerifyOtpDialog(

                    email = resetEmail,

                    otp = resetOtp,

                    warning = resetWarning,

                    isLoading = resetLoading,

                    onOtpChange =
                        passwordResetViewModel::onOtpChange,

                    onVerifyOtp =
                        passwordResetViewModel::verifyOtp,

                    onDismiss =
                        passwordResetViewModel::closePasswordReset
                )
            }


            PasswordResetViewModel.ResetStep.NEW_PASSWORD -> {

                ResetPasswordDialog(

                    newPassword = newPassword,

                    confirmPassword = confirmPassword,

                    warning = resetWarning,

                    isLoading = resetLoading,

                    onNewPasswordChange =
                        passwordResetViewModel::onNewPasswordChange,

                    onConfirmPasswordChange =
                        passwordResetViewModel::onConfirmPasswordChange,

                    onResetPassword =
                        passwordResetViewModel::resetPassword,

                    onDismiss =
                        passwordResetViewModel::closePasswordReset
                )
            }


            PasswordResetViewModel.ResetStep.SUCCESS -> {

                AlertDialog(

                    onDismissRequest = {},

                    title = {
                        Text("Password Reset Successful")
                    },

                    text = {
                        Text(
                            "Your password has been reset successfully. Please login with your new password."
                        )
                    },

                    confirmButton = {

                        Button(

                            onClick = {

                                passwordResetViewModel
                                    .closePasswordReset()
                            }

                        ) {

                            Text("Back to Login")
                        }
                    }
                )
            }


            PasswordResetViewModel.ResetStep.CLOSED -> {

                // Nothing displayed
            }
        }
    }
}
//NOTE: funloginscreen() -> box -> column -> card(Welcome Back, fields, submit, OR, register)