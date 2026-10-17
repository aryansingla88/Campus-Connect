package com.example.campusconnect.feature.settings.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.profile.ui.components.PageBg
import com.example.campusconnect.feature.settings.model.LocationVisibilityId
import com.example.campusconnect.feature.settings.model.SettingsDestinations
import com.example.campusconnect.feature.settings.model.SettingsItem
import com.example.campusconnect.feature.settings.model.SettingsItemId
import com.example.campusconnect.feature.settings.ui.components.SettingsDangerCard
import com.example.campusconnect.feature.settings.ui.components.SettingsSectionCard
import com.example.campusconnect.feature.settings.ui.components.SettingsTopBar
import com.example.campusconnect.feature.settings.ui.sheets.AppAppearanceSheet
import com.example.campusconnect.feature.settings.ui.sheets.DeleteAccountSheet
import com.example.campusconnect.feature.settings.ui.sheets.LanguageSheet
import com.example.campusconnect.feature.settings.ui.sheets.LocationVisibilityFlowSheet
import com.example.campusconnect.feature.settings.viewmodel.SettingsSheet
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel

//-------------------------------
// Settings Screen
//-------------------------------
// Rows fall into three buckets on tap:
//  1. Opens a bottom sheet locally (Location Visibility, App Appearance, Language,
//     Delete Account) — handled entirely by `vm.activeSheet` below, no navigation.
//  2. Pushes a full screen (Profile Visibility, Manage Social Links, Notifications) —
//     via `onNavigateFullScreen`, wired up in SettingsNav.
//  3. Not built yet (Change Password, Manage Data, Privacy & Security, Help & FAQ,
//     Contact Us, Terms & Policies, About, Edit Profile) — falls through to the
//     generic `onNavigate(route)` passthrough so it can be wired up later.

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onNavigateFullScreen: (SettingsItemId) -> Unit = {},
    onNavigate: (String) -> Unit = {},
    onConfirmDeleteAccount: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
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
            item.id == SettingsItemId.APP_APPEARANCE -> vm.openSheet(SettingsSheet.APP_APPEARANCE)
            item.id == SettingsItemId.LANGUAGE -> vm.openSheet(SettingsSheet.LANGUAGE)
            item.id == SettingsItemId.DELETE_ACCOUNT -> vm.openSheet(SettingsSheet.DELETE_ACCOUNT)

            item.id in fullScreenIds -> onNavigateFullScreen(item.id)

            else -> onNavigate(SettingsDestinations.routeFor(item.id))
        }
    }

    Scaffold(
        containerColor = PageBg,
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
