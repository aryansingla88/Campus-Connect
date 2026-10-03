package com.example.campusconnect.feature.settings.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.campusconnect.feature.profile.ui.components.CardBg
import com.example.campusconnect.feature.profile.ui.components.DividerColor
import com.example.campusconnect.feature.profile.ui.components.PageBg
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.profile.ui.components.TextPrimary
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrange
import com.example.campusconnect.feature.settings.ui.components.SettingsGlyphChip
import com.example.campusconnect.feature.settings.ui.components.SettingsPrimaryButton
import com.example.campusconnect.feature.settings.ui.components.SettingsTopBar
import com.example.campusconnect.feature.settings.viewmodel.SettingsViewModel

@Composable
fun ManageSocialLinksScreen(
    onBack: () -> Unit = {},
    onSaveChanges: () -> Unit = {},
    vm: SettingsViewModel = viewModel()
) {
    Scaffold(
        containerColor = PageBg,
        topBar = {
            SettingsTopBar(
                title = "Manage Social Links",
                subtitle = "Add or remove your social profiles",
                onBack = onBack
            )
        },
        bottomBar = {
            SettingsPrimaryButton(text = "Save Changes", onClick = onSaveChanges)
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            vm.socialLinks.forEach { link ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    elevation = CardDefaults.cardElevation(1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            SettingsGlyphChip(
                                glyph = link.glyph,
                                tint = link.iconTint,
                                background = link.iconBackground,
                                size = 34.dp,
                                circular = true
                            )

                            Text(
                                text = link.platformLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )

                            IconButton(
                                onClick = { vm.removeSocialLink(link.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    Icons.Outlined.Close,
                                    contentDescription = "Remove ${link.platformLabel}",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = link.url,
                            onValueChange = { vm.updateSocialLinkUrl(link.id, it) },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(link.placeholder, fontSize = 12.sp, color = TextMuted)
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = PageBg,
                                unfocusedContainerColor = PageBg,
                                focusedBorderColor = PrimaryOrange,
                                unfocusedBorderColor = DividerColor,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                cursorColor = PrimaryOrange
                            )
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { vm.addSocialLink() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryOrange)
            ) {
                Icon(Icons.Outlined.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Add More", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(4.dp))
        }
    }
}
