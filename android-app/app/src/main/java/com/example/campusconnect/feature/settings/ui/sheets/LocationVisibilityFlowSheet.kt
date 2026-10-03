package com.example.campusconnect.feature.settings.ui.sheets

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.ui.components.CardBg
import com.example.campusconnect.feature.profile.ui.components.TextMuted
import com.example.campusconnect.feature.profile.ui.components.TextPrimary
import com.example.campusconnect.feature.settings.model.LocationVisibilityId
import com.example.campusconnect.feature.settings.model.LocationVisibilityOption
import com.example.campusconnect.feature.settings.model.SelectableClub
import com.example.campusconnect.feature.settings.ui.components.PrimaryOrange
import com.example.campusconnect.feature.settings.ui.components.SettingsCheckboxRow
import com.example.campusconnect.feature.settings.ui.components.SettingsIconChip
import com.example.campusconnect.feature.settings.ui.components.SettingsRadioRow
import com.example.campusconnect.feature.settings.viewmodel.SettingsSheet

// --- Location Visibility <-> Select Clubs, as ONE modal bottom sheet --------------------
// One ModalBottomSheet, one outer layout: header block, a scrollable body, and a single
// Button that are all mounted exactly once and never rebuilt as a different composable
// tree. Switching steps only crossfades the fields living inside that same layout —
// the title/subtitle text and the row list — rather than swapping in two differently
// shaped screens. The Done button itself is one stable instance the whole time; only
// its onClick target changes depending on which step is active.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationVisibilityFlowSheet(
    step: SettingsSheet, // LOCATION_VISIBILITY or SELECT_CLUBS
    options: List<LocationVisibilityOption>,
    selectedOption: LocationVisibilityId,
    onSelectOption: (LocationVisibilityId) -> Unit,
    clubs: List<SelectableClub>,
    onToggleClub: (Int) -> Unit,
    onDismissAll: () -> Unit,
    onDoneLocation: () -> Unit,
    onDoneClubs: () -> Unit
) {
    val isClubsStep = step == SettingsSheet.SELECT_CLUBS

    ModalBottomSheet(
        onDismissRequest = onDismissAll,
        containerColor = CardBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
        ) {

            // --- Title + subtitle: just a field crossfade, same text slots every time ---
            Crossfade(
                targetState = isClubsStep,
                animationSpec = tween(220),
                label = "sheet_title"
            ) { clubsStep ->
                Column {
                    Text(
                        text = if (clubsStep) "Select Clubs" else "Location Visibility",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (clubsStep) {
                            "Choose the clubs that can see your location"
                        } else {
                            "Choose who can see your location on campus"
                        },
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            // --- Body: same scrollable list container, only its rows change -------------
            Crossfade(
                targetState = isClubsStep,
                animationSpec = tween(220),
                label = "sheet_body"
            ) { clubsStep ->
                Column(
                    modifier = Modifier
                        .heightIn(max = 420.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(if (clubsStep) 8.dp else 4.dp)
                ) {
                    if (clubsStep) {
                        clubs.forEach { club ->
                            SettingsCheckboxRow(
                                label = club.name,
                                checked = club.isSelected,
                                onToggle = { onToggleClub(club.id) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    } else {
                        options.forEach { option ->
                            SettingsRadioRow(
                                title = option.label,
                                subtitle = option.subtitle,
                                selected = selectedOption == option.id,
                                onClick = { onSelectOption(option.id) },
                                leading = {
                                    SettingsIconChip(
                                        icon = option.icon,
                                        tint = option.iconTint,
                                        background = option.iconBackground,
                                        size = 34.dp
                                    )
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // --- One stable button for the whole flow — only its target changes ---------
            Button(
                onClick = if (isClubsStep) onDoneClubs else onDoneLocation,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange)
            ) {
                Text("Done", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
