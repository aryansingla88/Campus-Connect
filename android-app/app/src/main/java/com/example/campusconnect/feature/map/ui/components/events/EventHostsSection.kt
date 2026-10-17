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
// HOSTS
// ============================================================================

@Composable
internal fun HostsSection(
    hosts: List<String>,
    onViewAllClick: () -> Unit
) {

    val hostsListState = rememberLazyListState()

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        Text(
            text = "Hosts",
            color = TextDark,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )

        Spacer(
            modifier = Modifier.height(10.dp)
        )

        LazyRow(
            state = hostsListState,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement =
                Arrangement.spacedBy(20.dp)
        ) {

            items(
                items = hosts,
                key = { it }
            ) { name ->

                HostAvatar(
                    name = name
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        // Horizontal sliding scroll indicator reflecting how far through
        // the hosts row the user has scrolled, in place of "View All".
        HostsScrollIndicator(
            listState = hostsListState,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 2.dp)
        )
    }
}


// ============================================================================
// HOSTS SCROLL INDICATOR
// ============================================================================

@Composable
private fun HostsScrollIndicator(
    listState: LazyListState,
    modifier: Modifier = Modifier
) {

    val layoutInfo = listState.layoutInfo
    val totalItemsCount = layoutInfo.totalItemsCount
    val visibleItemsInfo = layoutInfo.visibleItemsInfo

    // thumbWidthFraction: how much of the track the thumb covers.
    // thumbStartFraction: how far along the track the thumb starts.
    val (thumbWidthFraction, thumbStartFraction) = remember(
        totalItemsCount,
        visibleItemsInfo,
        listState.firstVisibleItemIndex,
        listState.firstVisibleItemScrollOffset
    ) {

        if (
            totalItemsCount == 0 ||
            visibleItemsInfo.isEmpty()
        ) {

            1f to 0f

        } else {

            val avgItemSize =
                visibleItemsInfo
                    .sumOf { it.size }
                    .toFloat() /
                        visibleItemsInfo.size

            val viewportSize =
                layoutInfo.viewportSize.width.toFloat()

            val totalContentSize =
                avgItemSize * totalItemsCount

            val widthFraction =
                (viewportSize / totalContentSize)
                    .coerceIn(0.15f, 1f)

            val maxScrollPx =
                (totalContentSize - viewportSize)
                    .coerceAtLeast(1f)

            val scrolledPx =
                (listState.firstVisibleItemIndex * avgItemSize) +
                        listState.firstVisibleItemScrollOffset

            val scrolledFraction =
                (scrolledPx / maxScrollPx)
                    .coerceIn(0f, 1f)

            widthFraction to
                    scrolledFraction * (1f - widthFraction)
        }
    }

    // Track width in raw pixels, measured directly off this Box via
    // onGloballyPositioned. This avoids BoxWithConstraints entirely (and
    // the "scope is unused" lint that comes with it), since all we ever
    // needed was the container's width — a plain Box gives us that just
    // as well.
    var trackWidthPx by remember {
        mutableStateOf(0)
    }

    Box(
        modifier = modifier
            .height(4.dp)
            .onGloballyPositioned { coordinates ->
                trackWidthPx = coordinates.size.width
            }
    ) {

        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(
                    RoundedCornerShape(2.dp)
                )
                .background(
                    Color(0xFFEDEDED)
                )
        )

        // Thumb — offset is applied in raw pixels via the lambda form of
        // Modifier.offset, so no dp/px conversion is needed at all.
        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (thumbStartFraction * trackWidthPx)
                            .roundToInt(),
                        y = 0
                    )
                }
                .fillMaxWidth(thumbWidthFraction)
                .height(4.dp)
                .clip(
                    RoundedCornerShape(2.dp)
                )
                .background(
                    OrangePrimary
                )
        )
    }
}


// ============================================================================
// HOST AVATAR
// ============================================================================

@Composable
private fun HostAvatar(
    name: String
) {

    Column(
        modifier = Modifier.width(64.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    avatarColorFor(name)
                ),
            contentAlignment =
                Alignment.Center
        ) {

            Text(
                text =
                    name.firstOrNull()
                        ?.uppercase()
                        ?: "?",
                color = TextDark,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = name,
            color = TextDark,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


private val hostAvatarPalette = listOf(
    Color(0xFFD2E9FC),
    Color(0xFFD3F2D3),
    Color(0xFFFCF1D3),
    Color(0xFFFDDEE4)
)


private fun avatarColorFor(
    name: String
): Color {

    val hash = name.hashCode().let {
        if (it < 0) -it else it
    }

    return hostAvatarPalette[
        hash % hostAvatarPalette.size
    ]
}


