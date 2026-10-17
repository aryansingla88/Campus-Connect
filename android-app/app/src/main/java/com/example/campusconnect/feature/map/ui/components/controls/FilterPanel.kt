package com.example.campusconnect.feature.map.ui.components.controls

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.map.model.MapFilterContext
import com.example.campusconnect.feature.map.model.MapFilterOption

private val FilterOrange = Color(0xFFF97316)
private val FilterOrangeLight = Color(0xFFFFF3EB)
private val FilterText = Color(0xFF1A1A1A)
private val FilterBorder = Color(0xFFF0E6DE)

private data class FilterGroup(
    val title: String,
    val options: List<MapFilterOption>
)

/** Responsive right-side filter drawer. Only one group can be expanded at a time. */
@Composable
internal fun FilterPanel(
    filterContext: MapFilterContext,
    userOptions: List<MapFilterOption>,
    poiCategories: List<MapFilterOption>,
    eventCategories: List<MapFilterOption>,
    selectedFilterKey: String?,
    expandedSection: String?,
    onExpandedSectionChange: (String?, Int) -> Unit,
    onOptionSelected: (String, MapFilterOption) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groups = when (filterContext) {
        MapFilterContext.HOME_MARKERS -> listOf(
            FilterGroup("Users", userOptions),
            FilterGroup("Places", poiCategories)
        )
        MapFilterContext.EVENT_CATEGORIES -> listOf(FilterGroup("Events", eventCategories))
        MapFilterContext.NONE -> emptyList()
    }

    BoxWithConstraints(modifier = modifier) {
        val drawerWidth = minOf(maxWidth * 0.42f, 180.dp)
        val drawerMaxHeight = minOf(maxHeight * 0.84f, 720.dp)

        Box {
            Surface(
                modifier = Modifier.width(drawerWidth).heightIn(max = drawerMaxHeight),
                shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
                color = Color.White.copy(alpha = 0.98f),
                tonalElevation = 3.dp,
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color(0xFFECEDEF))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = drawerMaxHeight)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 28.dp, end = 10.dp, top = 12.dp, bottom = 12.dp)
                ) {
                    groups.forEachIndexed { index, group ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .clickable {
                                    val nextSection = if (expandedSection == group.title) null else group.title
                                    onExpandedSectionChange(
                                        nextSection,
                                        if (nextSection == null) 0 else minOf(group.options.size, 3)
                                    )
                                }
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = group.title,
                                modifier = Modifier.weight(1f),
                                color = FilterText,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Icon(
                                imageVector = if (expandedSection == group.title) {
                                    Icons.Default.ExpandLess
                                } else {
                                    Icons.Default.ExpandMore
                                },
                                contentDescription = if (expandedSection == group.title) {
                                    "Collapse ${group.title} filters"
                                } else {
                                    "Expand ${group.title} filters"
                                },
                                tint = FilterOrange,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (expandedSection == group.title) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp * minOf(group.options.size, 3).toFloat()),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                items(group.options, key = { "${group.title}-${it.id}" }) { option ->
                                    val selected = selectedFilterKey == "${group.title}:${option.id}"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(13.dp))
                                            .background(if (selected) FilterOrangeLight else Color.White)
                                            .border(
                                                width = 1.dp,
                                                color = if (selected) Color(0xFFFFB98F) else FilterBorder,
                                                shape = RoundedCornerShape(13.dp)
                                            )
                                            .clickable { onOptionSelected(group.title, option) }
                                            .padding(horizontal = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = option.name,
                                            color = if (selected) FilterOrange else FilterText,
                                            fontSize = 14.sp,
                                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        if (index < groups.lastIndex) {
                            HorizontalDivider(
                                color = Color(0xFFE8E9EB),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(28.dp)
                    .height(64.dp)
                    .pointerInput(onClose) {
                        var dragDistance = 0f
                        detectHorizontalDragGestures(
                            onDragStart = { dragDistance = 0f },
                            onHorizontalDrag = { _, amount -> dragDistance += amount },
                            onDragEnd = { if (dragDistance > 36.dp.toPx()) onClose() }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(Color(0xFFB9BDC3))
                )
            }
        }
    }
}
