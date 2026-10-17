package com.example.campusconnect.feature.map.ui.components.events

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.R
import com.example.campusconnect.feature.map.model.MapEventInfo
import com.example.campusconnect.feature.map.ui.util.formatEventDate
import com.example.campusconnect.feature.map.ui.util.formatEventTimeRange
import kotlin.math.roundToInt


// ============================================================================
// NOTIFY BUTTON
// ============================================================================

@Composable
internal fun NotifyMeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var isNotified by remember {
        mutableStateOf(false)
    }

    val bellRotation = remember {
        Animatable(0f)
    }

    LaunchedEffect(isNotified) {

        if (isNotified) {

            bellRotation.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {

                    durationMillis = 450

                    0f at 0
                    -25f at 90
                    20f at 180
                    -15f at 270
                    8f at 360
                    0f at 450
                }
            )
        }
    }

    val backgroundColor =
        if (isNotified) {
            OrangePrimary
        } else {
            Color.White
        }

    val contentColor =
        if (isNotified) {
            Color.White
        } else {
            OrangePrimary
        }

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                backgroundColor
            )
            .then(
                if (!isNotified) {

                    Modifier.border(
                        width = 1.dp,
                        color = BorderOrange,
                        shape = RoundedCornerShape(
                            14.dp
                        )
                    )

                } else {
                    Modifier
                }
            )
            .clickable {

                isNotified = !isNotified

                onClick()
            },
        horizontalArrangement =
            Arrangement.Center,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                Icons.Default.Notifications,
            contentDescription =
                if (isNotified) {
                    "Notified"
                } else {
                    "Notify me"
                },
            tint = contentColor,
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer {
                    rotationZ =
                        bellRotation.value
                }
        )

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        Text(
            text =
                if (isNotified) {
                    "Notified"
                } else {
                    "Notify Me"
                },
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


// ============================================================================
// REGISTER BUTTON
// ============================================================================

@Composable
internal fun RegisterActionButton(
    isJoined: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    var registered by remember(isJoined) {
        mutableStateOf(isJoined)
    }

    val backgroundColor =
        if (registered) {
            Color.White
        } else {
            OrangePrimary
        }

    val contentColor =
        if (registered) {
            OrangePrimary
        } else {
            Color.White
        }

    Row(
        modifier = modifier
            .height(48.dp)
            .clip(
                RoundedCornerShape(14.dp)
            )
            .background(
                backgroundColor
            )
            .then(
                if (registered) {

                    Modifier.border(
                        width = 1.dp,
                        color = OrangePrimary,
                        shape = RoundedCornerShape(
                            14.dp
                        )
                    )

                } else {
                    Modifier
                }
            )
            .clickable {

                registered = !registered

                onClick()
            },
        horizontalArrangement =
            Arrangement.Center,
        verticalAlignment =
            Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                if (registered) {
                    Icons.Default.Check
                } else {
                    Icons.Default.PersonAdd
                },
            contentDescription =
                if (registered) {
                    "Registered"
                } else {
                    "Register"
                },
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        Text(
            text =
                if (registered) {
                    "Registered"
                } else {
                    "Register"
                },
            color = contentColor,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold
        )
    }
}


