package com.example.campusconnect.feature.map.ui.components.markers

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusconnect.feature.map.mapengine.MapMotion
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.MapMarker
import com.example.campusconnect.feature.map.model.MapPoiInfo
import com.example.campusconnect.feature.map.model.MapUserProfile
import com.example.campusconnect.feature.map.ui.HintColor
import com.example.campusconnect.feature.map.ui.OrangePrimary
import com.example.campusconnect.feature.map.ui.TextDark
@Composable
internal fun SelectedMarkerContent(
    selectedMarker: MapMarker?,
    lastSelectedMarker: MapMarker?,
    selectedProfile: MapUserProfile?,
    selectedPoi: MapPoiInfo?,
    onDismiss: () -> Unit,
    onAddFriendClick: (Int) -> Unit
) {
    AnimatedVisibility(
        visible = selectedMarker != null && selectedMarker.type != MarkerType.EVENT,
        enter = markerCardEnter(),
        exit = markerCardExit()
    ) {
        lastSelectedMarker?.let { marker ->
                when (marker.type) {

                    MarkerType.USER -> {
                        selectedProfile?.let { profile ->
                            UserMarkerDialog(
                                profile = profile,
                                onDismiss = {
                                    onDismiss()
                                },
                                onAddFriendClick = {
                                    onAddFriendClick(profile.id)
                                }
                            )
                        }
                    }

                    MarkerType.POI -> {
                        selectedPoi?.let { poiInfo ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                PoiMarkerDialog(
                                    poi = poiInfo,
                                    onNavigateClick = {
                                    },
                                    onCloseClick = {
                                        onDismiss()
                                    }
                                )
                            }
                        }
                    }

                    MarkerType.EVENT -> {
                        // Handled below via EventMarkerDialog, outside this
                        // AnimatedVisibility — ModalBottomSheet is a Popup with
                        // its own enter/exit animation, so it should not be
                        // wrapped in another slide/scale AnimatedVisibility.
                    }

                    MarkerType.SHOP -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            MarkerPreviewCard(
                                modifier = Modifier.padding(16.dp),
                                title = marker.label,
                                type = marker.type.name,
                                onClose = {
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
    }
}

private fun markerCardEnter(): EnterTransition {
    return fadeIn(MapMotion.tweenMedium()) +
            slideInVertically(
                animationSpec = MapMotion.tweenMedium(),
                initialOffsetY = { it / 3 }
            ) +
            scaleIn(
                initialScale = 0.98f,
                animationSpec = MapMotion.springSoft()
            )
}

private fun markerCardExit(): ExitTransition {
    return fadeOut(MapMotion.tweenFast()) +
            slideOutVertically(
                animationSpec = MapMotion.tweenFast(),
                targetOffsetY = { it / 3 }
            ) +
            scaleOut(
                targetScale = 0.98f,
                animationSpec = MapMotion.tweenFast()
            )
}

@Composable
private fun MarkerPreviewCard(
    modifier: Modifier = Modifier,
    title: String,
    type: String,
    onClose: () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "Type: $type",
                color = HintColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary
                    )
                ) {
                    Text("Details")
                }

                OutlinedButton(
                    onClick = {},
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    )
                ) {
                    Text("Navigate")
                }

                OutlinedButton(
                    onClick = onClose,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = OrangePrimary
                    )
                ) {
                    Text("Close")
                }
            }
        }
    }
}
