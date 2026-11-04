package com.example.campusconnect.feature.settings.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.core.network.RetrofitClient
import com.example.campusconnect.feature.auth.data.remote.request.ChangePasswordRequest
import com.example.campusconnect.feature.auth.viewmodel.PasswordResetViewModel
import com.example.campusconnect.feature.auth.ui.RequestPasswordResetDialog
import com.example.campusconnect.feature.auth.ui.VerifyOtpDialog
import com.example.campusconnect.feature.auth.ui.ResetPasswordDialog

import com.example.campusconnect.feature.profile.ui.components.PageBg
import com.example.campusconnect.feature.settings.model.LocationVisibilityId
import com.example.campusconnect.feature.settings.model.SettingsDestinations
import com.example.campusconnect.feature.settings.model.SettingsItem
import com.example.campusconnect.feature.settings.model.SettingsItemId
import com.example.campusconnect.feature.settings.ui.components.SettingsDangerCard
import com.example.campusconnect.feature.settings.ui.components.SettingsSectionCard
import com.example.campusconnect.feature.settings.ui.components.SettingsTopBar
import com.example.campusconnect.feature.settings.ui.components.SettingsPrimaryButton
import com.example.campusconnect.feature.settings.ui.components.ChangePasswordDialog
import com.example.campusconnect.feature.settings.ui.sheets.AppAppearanceSheet
import com.example.campusconnect.feature.settings.ui.sheets.DeleteAccountSheet
import com.example.campusconnect.feature.settings.ui.sheets.LanguageSheet
import com.example.campusconnect.feature.settings.ui.sheets.LocationVisibilityFlowSheet
import com.example.campusconnect.feature.settings.viewmodel.SettingsSheet
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel
import com.example.campusconnect.feature.settings.ui.sheets.LocationTrackingModeSheet
import kotlinx.coroutines.launch

//-------------------------------
// Settings Screen
//-------------------------------
// Rows fall into three buckets on tap:
//  1. Opens a bottom sheet locally (Location Visibility, App Appearance, Language,
//     Delete Account) — handled entirely by `vm.activeSheet` below, no navigation.
//  2. Pushes a full screen (Profile Visibility, Manage Social Links, Notifications) —
//     via `onNavigateFullScreen`, wired up in SettingsNav.
//  3. Not built yet (Manage Data, Privacy & Security, Help & FAQ,
//     Contact Us, Terms & Policies, About, Edit Profile) — falls through to the
//     generic `onNavigate(route)` passthrough so it can be wired up later.

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigateFullScreen: (SettingsItemId) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onConfirmDeleteAccount: () -> Unit = {},
    onLogout: () -> Unit = {},
    vm: SettingsViewModel = viewModel(),
    passwordResetVm: PasswordResetViewModel = viewModel()
) {
    val trackingMode by vm.trackingMode.collectAsState()
    var showLogoutConfirmation by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showPasswordChanged by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var passwordLoading by remember { mutableStateOf(false) }
    val resetStep by passwordResetVm.currentStep.collectAsState()
    val resetEmail by passwordResetVm.email.collectAsState()
    val resetOtp by passwordResetVm.otp.collectAsState()
    val resetNewPassword by passwordResetVm.newPassword.collectAsState()
    val resetConfirmPassword by passwordResetVm.confirmPassword.collectAsState()
    val resetWarning by passwordResetVm.warning.collectAsState()
    val resetLoading by passwordResetVm.isLoading.collectAsState()
    val scope = rememberCoroutineScope()
    val fullScreenIds = remember {
        setOf(
            SettingsItemId.PROFILE_VISIBILITY,
            SettingsItemId.MANAGE_SOCIAL_LINKS,
            SettingsItemId.NOTIFICATIONS,
            SettingsItemId.EDIT_PROFILE
        )
    }

    fun handleClick(item: SettingsItem) {
        when {
            item.id == SettingsItemId.LOCATION_VISIBILITY -> vm.openSheet(SettingsSheet.LOCATION_VISIBILITY)
            item.id == SettingsItemId.LOCATION_TRACKING_MODE -> vm.openSheet(SettingsSheet.LOCATION_TRACKING_MODE)
            item.id == SettingsItemId.APP_APPEARANCE -> vm.openSheet(SettingsSheet.APP_APPEARANCE)
            item.id == SettingsItemId.LANGUAGE -> vm.openSheet(SettingsSheet.LANGUAGE)
            item.id == SettingsItemId.DELETE_ACCOUNT -> vm.openSheet(SettingsSheet.DELETE_ACCOUNT)
            item.id == SettingsItemId.CHANGE_PASSWORD -> {
                currentPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = null
                showChangePassword = true
            }

            item.id in fullScreenIds -> onNavigateFullScreen(item.id)

            else -> onNavigate(SettingsDestinations.routeFor(item.id))
        }
    }

    Scaffold(
        containerColor = PageBg,
        bottomBar = {
            SettingsPrimaryButton(
                text = "Logout",
                onClick = { showLogoutConfirmation = true },
                modifier = Modifier.navigationBarsPadding()
            )
        },
        topBar = {
            SettingsTopBar(
                title = "Settings",
                subtitle = "Manage your account, privacy and app preferences",
                onBack = onBack
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {

            vm.sections.forEach { section ->
                SettingsSectionCard(
                    section = section,
                    onItemClick = ::handleClick
                )
            }

            SettingsDangerCard(
                item = vm.dangerItem,
                onClick = ::handleClick
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showLogoutConfirmation) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirmation = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            title = { Text("Log out of Campus Connect?", style = MaterialTheme.typography.titleLarge) },
            text = { Text("You can sign back in anytime with your account.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(onClick = {
                    showLogoutConfirmation = false
                    onLogout()
                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(12.dp)) { Text("Log out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirmation = false }) { Text("Stay signed in") }
            }
        )
    }

    if (showChangePassword && resetStep == PasswordResetViewModel.ResetStep.CLOSED) {
        ChangePasswordDialog(
            currentPassword = currentPassword,
            newPassword = newPassword,
            confirmPassword = confirmPassword,
            error = passwordError,
            isLoading = passwordLoading,
            onCurrentPasswordChange = { currentPassword = it; passwordError = null },
            onNewPasswordChange = { newPassword = it; passwordError = null },
            onConfirmPasswordChange = { confirmPassword = it; passwordError = null },
            onForgotPassword = {
                passwordResetVm.startPasswordReset()
            },
            onSubmit = {
                when {
                    currentPassword.isBlank() -> passwordError = "Enter your current password."
                    newPassword.length < 8 -> passwordError = "New password must be at least 8 characters."
                    newPassword != confirmPassword -> passwordError = "The new passwords do not match."
                    else -> scope.launch {
                        passwordLoading = true
                        passwordError = null
                        try {
                            val response = RetrofitClient.authApi.changePassword(
                                ChangePasswordRequest(currentPassword, newPassword)
                            )
                            if (response.isSuccessful && response.body()?.success == true) {
                                showChangePassword = false
                                showPasswordChanged = true
                            } else {
                                passwordError = response.body()?.message
                                    ?: "Unable to change password. Try again."
                            }
                        } catch (error: Exception) {
                            passwordError = error.message ?: "Unable to connect to the server."
                        } finally {
                            passwordLoading = false
                        }
                    }
                }
            },
            onDismiss = { showChangePassword = false }
        )
    }

    when (resetStep) {
        PasswordResetViewModel.ResetStep.EMAIL -> RequestPasswordResetDialog(
            email = resetEmail,
            warning = resetWarning,
            isLoading = resetLoading,
            onEmailChange = passwordResetVm::onEmailChange,
            onSendOtp = passwordResetVm::requestOtp,
            onDismiss = passwordResetVm::closePasswordReset
        )
        PasswordResetViewModel.ResetStep.OTP -> VerifyOtpDialog(
            email = resetEmail,
            otp = resetOtp,
            warning = resetWarning,
            isLoading = resetLoading,
            onOtpChange = passwordResetVm::onOtpChange,
            onVerifyOtp = passwordResetVm::verifyOtp,
            onDismiss = passwordResetVm::closePasswordReset
        )
        PasswordResetViewModel.ResetStep.NEW_PASSWORD -> ResetPasswordDialog(
            newPassword = resetNewPassword,
            confirmPassword = resetConfirmPassword,
            warning = resetWarning,
            isLoading = resetLoading,
            onNewPasswordChange = passwordResetVm::onNewPasswordChange,
            onConfirmPasswordChange = passwordResetVm::onConfirmPasswordChange,
            onResetPassword = passwordResetVm::resetPassword,
            onDismiss = passwordResetVm::closePasswordReset
        )
        PasswordResetViewModel.ResetStep.SUCCESS -> AlertDialog(
            onDismissRequest = {
                passwordResetVm.closePasswordReset()
                showChangePassword = false
            },
            title = { Text("Password reset") },
            text = { Text("Your password has been reset. Sign in with your new password next time.") },
            confirmButton = {
                TextButton(onClick = {
                    passwordResetVm.closePasswordReset()
                    showChangePassword = false
                }) { Text("Done") }
            }
        )
        PasswordResetViewModel.ResetStep.CLOSED -> Unit
    }

    if (showPasswordChanged) {
        AlertDialog(
            onDismissRequest = { showPasswordChanged = false },
            title = { Text("Password updated") },
            text = { Text("Your password was changed successfully.") },
            confirmButton = {
                TextButton(onClick = { showPasswordChanged = false }) { Text("Done") }
            }
        )
    }

    //-------------------------------
    // Bottom sheets
    //-------------------------------

    when (vm.activeSheet) {

        SettingsSheet.LOCATION_VISIBILITY, SettingsSheet.SELECT_CLUBS -> {
            // One sheet instance for both steps — see LocationVisibilityFlowSheet for
            // why this isn't two separate ModalBottomSheet calls.
            LocationVisibilityFlowSheet(
                step = vm.activeSheet ?: SettingsSheet.LOCATION_VISIBILITY,
                options = vm.locationOptions,
                selectedOption = vm.selectedLocationOption,
                onSelectOption = { id ->
                    vm.selectLocationOption(id)
                    if (id == LocationVisibilityId.CLUBS) {
                        vm.openSheet(SettingsSheet.SELECT_CLUBS)
                    }
                },
                clubs = vm.clubs,
                onToggleClub = vm::toggleClubSelection,
                onDismissAll = vm::closeSheet,
                onDoneLocation = vm::closeSheet,
                onDoneClubs = { vm.openSheet(SettingsSheet.LOCATION_VISIBILITY) }
            )
        }

        SettingsSheet.APP_APPEARANCE -> {
            AppAppearanceSheet(
                options = vm.themeOptions,
                selected = vm.selectedTheme,
                onSelect = vm::selectTheme,
                onDismiss = vm::closeSheet,
                onDone = vm::closeSheet
            )
        }

        SettingsSheet.LANGUAGE -> {
            LanguageSheet(
                options = vm.languageOptions,
                selected = vm.selectedLanguage,
                onSelect = vm::selectLanguage,
                onDismiss = vm::closeSheet,
                onDone = vm::closeSheet
            )
        }

        SettingsSheet.LOCATION_TRACKING_MODE -> {
            LocationTrackingModeSheet(
                selectedMode = trackingMode,
                onSelectMode = vm::selectTrackingMode,
                onDismiss = vm::closeSheet
            )
        }

        SettingsSheet.DELETE_ACCOUNT -> {
            DeleteAccountSheet(
                onDismiss = vm::closeSheet,
                onConfirmDelete = {
                    vm.closeSheet()
                    onConfirmDeleteAccount()
                }
            )
        }

        null -> Unit
    }
}
