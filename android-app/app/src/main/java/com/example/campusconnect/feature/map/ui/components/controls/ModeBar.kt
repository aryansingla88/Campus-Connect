package com.example.campusconnect.feature.map.ui.components.controls

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.PanelSearchBar
import com.example.campusconnect.feature.map.ui.OrangeLight
import com.example.campusconnect.feature.map.ui.OrangePrimary
import com.example.campusconnect.feature.map.ui.TextDark
import com.example.campusconnect.feature.map.model.MapMode

@Composable
internal fun ModeBar(
    selectedMode: MapMode,
    onModeSelected: (MapMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 10.dp,
                    vertical = 6.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeBarItem(
                    icon = Icons.Default.Image,
                    label = "Poster",
                    selected = selectedMode == MapMode.POSTER,
                    onClick = {
                        onModeSelected(MapMode.POSTER)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Home,
                    label = "Home",
                    selected = selectedMode == MapMode.HOME,
                    onClick = {
                        onModeSelected(MapMode.HOME)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Event,
                    label = "Events",
                    selected = selectedMode == MapMode.EVENT,
                    onClick = {
                        onModeSelected(MapMode.EVENT)
                    },
                    modifier = Modifier.weight(1f)
                )

                ModeBarItem(
                    icon = Icons.Default.Storefront,
                    label = "Shop",
                    selected = selectedMode == MapMode.SHOP,
                    onClick = {
                        onModeSelected(MapMode.SHOP)
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
internal fun ModeBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(
                if (selected) OrangeLight else Color.Transparent
            )
            .clickable {
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(horizontal = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) OrangePrimary else TextDark,
                modifier = Modifier.size(20.dp)
            )

            Text(
                text = label,
                color = if (selected) OrangePrimary else TextDark,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
