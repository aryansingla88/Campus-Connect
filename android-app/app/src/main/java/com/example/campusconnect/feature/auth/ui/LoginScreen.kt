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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
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


    // navigation trigger
    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            onLoginSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFF4DA3FF),  // match your gradient
                        Color(0xFF0A3A7A)
                    )
                )
            )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(80.dp))

            // Logo
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Username
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

                singleLine = true,

                shape = RoundedCornerShape(12.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,

                    focusedBorderColor = Color.Blue,
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

                singleLine = true,

                visualTransformation = PasswordVisualTransformation(),

                shape = RoundedCornerShape(12.dp),

                colors = OutlinedTextFieldDefaults.colors(

                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,

                    focusedBorderColor = Color.Blue,
                    unfocusedBorderColor = Color.Transparent,

                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),

                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Forgot Password?",
                color = Color.White,
                modifier = Modifier.clickable {
                    passwordResetViewModel.startPasswordReset()
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Button
            Button(
                onClick = { viewModel.login() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                //Passing a value to the colors parameter of the Button composable.
                // A "Buttoncolors" object is returned to the colors variable
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00BCD4)
                )
            ) {
                Text("Submit", color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Register text
            Text(
                text = "New user? Register",
                color = Color(0xFFE3F2FD),
                modifier = Modifier.clickable {
                    onNavigateToRegister()
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Warning
            if (warning.isNotEmpty()) {
                Text(
                    text = warning,
                    color = Color(0xFFFFE6E6),
                    fontSize = 14.sp
                )
            }
        }
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
//NOTE: funloginscreen() -> box -> column