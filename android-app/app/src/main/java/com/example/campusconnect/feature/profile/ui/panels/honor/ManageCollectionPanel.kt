package com.example.campusconnect.feature.profile.ui.panels.honor

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.model.ProfileHonor
import com.example.campusconnect.feature.profile.ui.components.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class CollectionTab { BADGES, MEDALS }

@Composable
fun ManageCollectionPanel(
    badges : List<ProfileHonor>,
    medals : List<ProfileHonor>,
    onBadgeMoveUp   : (Int) -> Unit,
    onBadgeMoveDown : (Int) -> Unit,
    onMedalMoveUp   : (Int) -> Unit,
    onMedalMoveDown : (Int) -> Unit,
    onBadgeMoveTo   : (Int, Int) -> Unit,
    onMedalMoveTo   : (Int, Int) -> Unit,
) {
    var selectedTab by remember { mutableStateOf(CollectionTab.BADGES) }

    Column(modifier = Modifier.fillMaxSize()) {

        TabRow(
            selectedTabIndex = if (selectedTab == CollectionTab.BADGES) 0 else 1,
            containerColor   = Color.White,
            contentColor     = Orange,
            indicator        = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(
                        positions[if (selectedTab == CollectionTab.BADGES) 0 else 1]
                    ),
                    color = Orange
                )
            }
        ) {
            Tab(
                selected = selectedTab == CollectionTab.BADGES,
                onClick  = { selectedTab = CollectionTab.BADGES },
                text     = { Text("Badges", color = if (selectedTab == CollectionTab.BADGES) Orange else TextMuted) }
            )
            Tab(
                selected = selectedTab == CollectionTab.MEDALS,
                onClick  = { selectedTab = CollectionTab.MEDALS },
                text     = { Text("Medals", color = if (selectedTab == CollectionTab.MEDALS) Orange else TextMuted) }
            )
        }

        val items    = if (selectedTab == CollectionTab.BADGES) badges else medals
        val moveUp   = if (selectedTab == CollectionTab.BADGES) onBadgeMoveUp   else onMedalMoveUp
        val moveDown = if (selectedTab == CollectionTab.BADGES) onBadgeMoveDown else onMedalMoveDown
        val moveTo   = if (selectedTab == CollectionTab.BADGES) onBadgeMoveTo   else onMedalMoveTo

        var expandedIndex by remember { mutableStateOf<Int?>(null) }

        Column(
            modifier            = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items.forEachIndexed { index, item ->
                HonorItemRow(
                    item         = item,
                    index        = index,
                    total        = items.size,
                    isBadge      = selectedTab == CollectionTab.BADGES,
                    isExpanded   = expandedIndex == index,
                    onMoveUp     = { moveUp(index) },
                    onMoveDown   = { moveDown(index) },
                    onChipTap    = {
                        expandedIndex = if (expandedIndex == index) null else index
                    },
                    onMoveTo     = { target ->
                        moveTo(index, target)
                        expandedIndex = null
                    }
                )
            }
        }
    }
}


// --- Single item row ---------------------------------------------------------------------------
//
@Composable
private fun HonorItemRow(
    item : ProfileHonor,
    index      : Int,
    total      : Int,
    isBadge    : Boolean,
    isExpanded : Boolean,
    onMoveUp   : () -> Unit,
    onMoveDown : () -> Unit,
    onChipTap  : () -> Unit,
    onMoveTo   : (Int) -> Unit,
) {
    ProfileListCard(
        title    = item.title,
        subtitle = item.subtitle ?: "",

        leadingContent = {
            Box(
                modifier         = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(OrangeDark.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (isBadge) {
                    Box(Modifier.size(18.dp).clip(CircleShape).background(Orange))
                } else {
                    Icon(Icons.Outlined.WorkspacePremium, null, tint = OrangeDark, modifier = Modifier.size(18.dp))
                }
            }
        },

        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    IconButton(
                        onClick  = onMoveUp,
                        enabled  = index > 0,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            Icons.Outlined.KeyboardArrowUp, null,
                            tint = if (index > 0) Orange else TextMuted.copy(alpha = 0.35f)
                        )
                    }
                    IconButton(
                        onClick  = onMoveDown,
                        enabled  = index < total - 1,
                        modifier = Modifier.size(22.dp)
                    ) {
                        Icon(
                            Icons.Outlined.KeyboardArrowDown, null,
                            tint = if (index < total - 1) Orange else TextMuted.copy(alpha = 0.35f)
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // Priority chip / inline wheel
                AnimatedContent(
                    targetState   = isExpanded,
                    transitionSpec = {
                        fadeIn(tween(150)) togetherWith fadeOut(tween(150))
                    },
                    label = "chip_expand_$index"
                ) { expanded ->
                    if (expanded) {
                        InlinePriorityWheel(
                            current  = index + 1,
                            total    = total,
                            onMoveTo = onMoveTo,
                            onCollapse = onChipTap
                        )
                    } else {
                        PriorityChip(number = index + 1, onClick = onChipTap)
                    }
                }
            }
        }
    )
}


// -- Collapsed chip ---------------------------------------------------------------------------
@Composable
private fun PriorityChip(number: Int, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        shape    = RoundedCornerShape(8.dp),
        color    = OrangeLight
    ) {
        Box(Modifier.size(28.dp), Alignment.Center) {
            Text("$number", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = OrangeDark)
        }
    }
}

// --  Inline scroll wheel ---------------------------------------------------------------------------

@Composable
private fun InlinePriorityWheel(
    current    : Int,
    total      : Int,
    onMoveTo   : (Int) -> Unit,
    onCollapse : () -> Unit,
) {
    val itemHeightDp : Dp    = 28.dp
    val visibleItems : Int   = 3        // 3 rows tall = compact, fits in the row
    val scope                = rememberCoroutineScope()

    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = (current - 1).coerceAtLeast(0)
    )

    val centredIndex by remember {
        derivedStateOf {
            val info   = listState.layoutInfo.visibleItemsInfo
            val offset = listState.firstVisibleItemScrollOffset
            val idx    = listState.firstVisibleItemIndex
            val size   = info.firstOrNull()?.size ?: 1
            if (offset > size / 2) idx + 1 else idx
        }
    }

    val isScrolling = listState.isScrollInProgress

    LaunchedEffect(isScrolling, centredIndex) {
        if (isScrolling) return@LaunchedEffect
        delay(1100)
        val target = centredIndex
        if (target != current - 1) onMoveTo(target)
        else onCollapse()
    }

    Box(
        modifier = Modifier
            .width(36.dp)
            .height(itemHeightDp * visibleItems)
            .clip(RoundedCornerShape(10.dp))
            .background(OrangeLight)
    ) {

        LazyColumn(
            state          = listState,
            modifier       = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = itemHeightDp), // centres first/last
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed((1..total).toList()) { _, number ->
                val isCentred = number == centredIndex + 1
                Box(
                    modifier         = Modifier
                        .width(36.dp)
                        .height(itemHeightDp)
                        .clickable {
                            scope.launch { listState.animateScrollToItem(number - 1) }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = "$number",
                        fontSize   = if (isCentred) 14.sp else 11.sp,
                        fontWeight = if (isCentred) FontWeight.Bold else FontWeight.Normal,
                        color      = if (isCentred) OrangeDark else TextMuted.copy(alpha = 0.4f),
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(OrangeLight, Color.Transparent)))
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp)
                .align(Alignment.BottomCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, OrangeLight)))
        )


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeightDp)
                .align(Alignment.Center)
                .drawBehind {
                    val stroke = 1.dp.toPx()
                    drawLine(Orange, Offset(4.dp.toPx(), 0f),          Offset(size.width - 4.dp.toPx(), 0f),          stroke)
                    drawLine(Orange, Offset(4.dp.toPx(), size.height), Offset(size.width - 4.dp.toPx(), size.height), stroke)
                }
        )
    }
}