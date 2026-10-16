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
import com.example.campusconnect.feature.map.mapengine.model.MarkerType
import com.example.campusconnect.feature.map.model.EventFilter
import com.example.campusconnect.feature.map.model.MapFilterContext

@Composable
internal fun FilterPanel(
    filterContext: MapFilterContext,
    eventCategories: List<EventFilter>,
    modifier: Modifier = Modifier,
    onFilterSelected: (MarkerType?) -> Unit,
    onEventCategorySelected: (EventFilter) -> Unit = {}
) {
    Surface(
        modifier = modifier
            .width(210.dp)
            .height(340.dp),
        shape = RoundedCornerShape(18.dp),
        color = Color.White.copy(alpha = 0.92f),
        tonalElevation = 8.dp,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Filters",
                color = TextDark,
                fontWeight = FontWeight.SemiBold
            )

            when (filterContext) {
                MapFilterContext.HOME_MARKERS -> {
                    FilterButton("All") { onFilterSelected(null) }
                    FilterButton("Users") { onFilterSelected(MarkerType.USER) }
                    FilterButton("POI") { onFilterSelected(MarkerType.POI) }
                }

                MapFilterContext.EVENT_CATEGORIES -> {
                    if (eventCategories.isEmpty()) {
                        Text(
                            text = "Event categories will appear here.",
                            color = TextDark
                        )
                    } else {
                        eventCategories.forEach { category ->
                            FilterButton(category.name) {
                                onEventCategorySelected(category)
                            }
                        }
                    }
                }

                MapFilterContext.NONE -> Unit
            }
        }
    }
}

@Composable
internal fun FilterButton(
    text: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = OrangePrimary
        )
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

