package com.example.campusconnect.feature.map.components

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
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.roundToInt


private val OrangePrimary = Color(0xFFFE5200)
private val TextDark = Color(0xFF000000)
private val TextMuted = Color(0xFF666B71)
private val BorderOrange = Color(0xFFFE5200)

private val StatCardBg = Color(0xFFFDF4EF)
private val PillBg = Color(0xFFFDE5D9)
private val PillText = Color(0xFFD53A0C)


// ============================================================================
// MAIN BOTTOM SHEET
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventMarkerDialog(
    event: MapEventInfo,
    onDismiss: () -> Unit,
    onNavigateClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onNotifyClick: () -> Unit = {},
    endTime: String? = null,
    description: String? = null,
    registrationsCount: Int? = null,
    eventType: String? = null,
    onViewAllHostsClick: () -> Unit = {},
    nearbyEvents: List<MapEventInfo> = emptyList(),
    onNearbyEventClick: (MapEventInfo) -> Unit = {}
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    /*
     * IMPORTANT
     *
     * Use currentValue and NOT targetValue.
     *
     * targetValue changes while the user is dragging.
     * If we use targetValue, the content starts changing before
     * the sheet has actually reached the expanded/collapsed anchor.
     *
     * currentValue changes only after the sheet reaches its anchor.
     */
    val isExpanded =
        sheetState.currentValue == SheetValue.Expanded

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(
            topStart = 28.dp,
            topEnd = 28.dp
        ),
        dragHandle = {
            SheetDragHandle()
        }
    ) {

        EventQuickPreviewContent(
            event = event,
            endTime = endTime,
            description = description,
            registrationsCount = registrationsCount,
            eventType = eventType,
            onViewAllHostsClick = onViewAllHostsClick,
            nearbyEvents = nearbyEvents,
            isExpanded = isExpanded,
            onDismiss = onDismiss,
            onNavigateClick = onNavigateClick,
            onRegisterClick = onRegisterClick,
            onNotifyClick = onNotifyClick,
            onNearbyEventClick = onNearbyEventClick
        )
    }
}


// ============================================================================
// DRAG HANDLE
// ============================================================================

@Composable
private fun SheetDragHandle() {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 8.dp,
                bottom = 8.dp
            ),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(
                    RoundedCornerShape(4.dp)
                )
                .background(
                    Color(0xFFC7C7C7)
                )
        )
    }
}


// ============================================================================
// MAIN CONTENT
// ============================================================================

/*
 * IMPORTANT — WHY THIS LOOKS DIFFERENT FROM THE ORIGINAL:
 *
 * The original composable swapped between wrap-content and fillMaxHeight()
 * based on isExpanded (an instant, un-animated jump) AND wrapped the
 * "details" section in a separate AnimatedVisibility animation. Two height
 * animations fought over the same space — the sheet's own native
 * drag/offset animation, and our manual one — causing visible flicker.
 *
 * Removing fillMaxHeight() entirely fixed the flicker but introduced a new
 * bug: the white background only extended as far as the actual content, so
 * once the sheet dragged past that point, the dark scrim behind the sheet
 * showed through as a black gap at the bottom.
 *
 * THE FIX: apply fillMaxHeight() UNCONDITIONALLY — not gated on isExpanded.
 * Because it's always on, the content's measured height never changes when
 * isExpanded flips, so there is nothing left to animate at the content
 * level. The white background always reaches the bottom of the sheet (no
 * black gap), and the ModalBottomSheet's own anchor system (PartiallyExpanded
 * vs Expanded) is the only thing moving — it reveals more or less of this
 * already-full-height column via its native offset animation, which is
 * already smooth.
 *
 * isExpanded is now only used for the tiny "QUICK PREVIEW" vs "EVENT" text
 * label swap, which is a text-only change and doesn't affect layout height,
 * so it can't cause jank.
 */
@Composable
private fun EventQuickPreviewContent(
    event: MapEventInfo,
    endTime: String?,
    description: String?,
    registrationsCount: Int?,
    eventType: String?,
    onViewAllHostsClick: () -> Unit,
    nearbyEvents: List<MapEventInfo>,
    isExpanded: Boolean,
    onDismiss: () -> Unit,
    onNavigateClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onNotifyClick: () -> Unit,
    onNearbyEventClick: (MapEventInfo) -> Unit
) {

    val scrollState = rememberScrollState()
    val aboutScrollState = rememberScrollState()

    /*
     * IMPORTANT — fillMaxHeight(sheetHeightFraction) here is UNCONDITIONAL
     * (not gated on isExpanded) and capped at a FRACTION, not the full
     * screen.
     *
     * Why unconditional: if this only applied when isExpanded was true, the
     * content's measured height would change the moment the sheet settles
     * into the Expanded anchor — competing with the sheet's own native
     * drag/offset animation and causing a visible jump (flicker).
     *
     * Why capped at a fraction (not a bare fillMaxHeight()): filling 100% of
     * the available height lets the sheet cover the entire screen with no
     * visible gap above it. Capping it at 90% keeps a small barrier/gap at
     * the top at all times, showing the map behind it, while still being a
     * CONSTANT value across both peek and expanded states — nothing about
     * layout changes when isExpanded flips, so the sheet's own native
     * offset animation is the only thing moving, and it's already smooth.
     *
     * isExpanded is only used for the tiny "QUICK PREVIEW" vs "EVENT" text
     * label swap, which is a text-only change and doesn't affect layout
     * height, so it can't cause jank.
     */
    val sheetHeightFraction = 0.80f

    // Fallback values so the Registrations / Event Type stat cards always
    // render even when the caller hasn't wired up real data — mirrors the
    // same fallback pattern already used by formatEventDate/
    // formatEventTimeRange further down (placeholder defaults when input
    // is blank).
    val displayRegistrationsCount =
        registrationsCount ?: 320

    val displayEventType =
        eventType
            ?.takeIf { it.isNotBlank() }
            ?: "Technical"

    // Fallback so the Hosts section always has something to render even
    // when the caller hasn't wired up real host data yet — same pattern as
    // the stats fallbacks above. 10 names to verify horizontal scrolling.
    val displayHosts =
        event.hosts
            .map { it.name }
            .ifEmpty {
                listOf(
                    "Alex",
                    "Maria",
                    "Chen",
                    "Fatima",
                    "Ravi",
                    "Sofia",
                    "Liam",
                    "Aisha",
                    "Diego",
                    "Priya"
                )
            }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(sheetHeightFraction)
            .background(Color.White)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .verticalScroll(scrollState)
                .padding(
                    start = 20.dp,
                    end = 20.dp,
                    top = 2.dp
                )
        ) {

            // ================================================================
            // HEADER
            // ================================================================

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {

                EventPoster(
                    event = event,
                    modifier = Modifier
                        .width(118.dp)
                        .height(185.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 26.dp)
                ) {

                    EventTag(
                        expanded = isExpanded
                    )

                    Spacer(
                        modifier = Modifier.height(4.dp)
                    )

                    Text(
                        text = event.title,
                        color = TextDark,
                        fontSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(2.dp)
                    )

                    Text(
                        text = "By ${
                            event.hosts.firstOrNull()?.name
                                ?: "CS Club"
                        }",
                        color = TextMuted,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )

                    MetaRow(
                        icon = Icons.Default.CalendarToday,
                        text = formatEventDate(event.date)
                    )

                    MetaRow(
                        icon = Icons.Default.AccessTime,
                        text = formatEventTimeRange(
                            event.time,
                            endTime
                        )
                    )

                    MetaRow(
                        icon = Icons.Default.LocationOn,
                        text = event.venue
                            ?.ifBlank {
                                "Main Auditorium"
                            }
                            ?: "Main Auditorium"
                    )
                }
            }


            // ================================================================
            // ACTION BUTTONS
            // ================================================================

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                NotifyMeButton(
                    onClick = onNotifyClick,
                    modifier = Modifier.weight(1f)
                )

                RegisterActionButton(
                    isJoined = event.isJoined,
                    onClick = onRegisterClick,
                    modifier = Modifier.weight(1f)
                )
            }


            // ================================================================
            // DETAILS (always composed — revealed by the sheet's own drag
            // animation, not by AnimatedVisibility)
            // ================================================================

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = 16.dp
                    )
            ) {

                // ----------------------------------------------------
                // STATS
                // ----------------------------------------------------

                StatsRow(
                    registrationsCount =
                        displayRegistrationsCount,
                    eventType =
                        displayEventType
                )


                // ----------------------------------------------------
                // ABOUT
                // ----------------------------------------------------

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "About",
                    color = TextDark,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                // Fixed height (not just a max) so About always reserves
                // the same amount of space whether the description is one
                // line or ten — short text just leaves blank space below
                // it, long text scrolls within this same fixed area.
                Text(
                    text = description
                        ?.takeIf {
                            it.isNotBlank()
                        }
                        ?: "An open mic or open mike (shortened from \"open microphone\") is a live show at a venue such as a coffeehouse, nightclub, comedy club, strip club, or pub, often taking place at night (an open mic night), in which audience members may perform on stage whether they are amateurs or professionals, often for the first time or to promote an upcoming performance.[1] As the name suggests, performers are usually provided with a microphone plugged into a PA system so that they can be better heard by the audience",
                    color = TextMuted,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .verticalScroll(aboutScrollState)
                )


                // ----------------------------------------------------
                // HOSTS
                // ----------------------------------------------------

                Spacer(
                    modifier = Modifier.height(22.dp)
                )

                HostsSection(
                    hosts = displayHosts,
                    onViewAllClick =
                        onViewAllHostsClick
                )

                Spacer(
                    modifier = Modifier.height(28.dp)
                )
            }
        }


        // ================================================================
        // FLOATING NAVIGATE BUTTON
        // ================================================================

        /*
         * No statusBarsPadding().
         *
         * This button belongs to the bottom sheet, not the screen.
         * statusBarsPadding() here can make the button jump when the
         * sheet changes position.
         */
        NavigateIconButton(
            onClick = onNavigateClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 10.dp)
        )
    }
}


// ============================================================================
// NAVIGATE BUTTON
// ============================================================================

@Composable
private fun NavigateIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(
                OrangePrimary
            )
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {

        Icon(
            imageVector = Icons.Default.NearMe,
            contentDescription = "Navigate",
            tint = Color.White,
            modifier = Modifier.size(19.dp)
        )
    }
}


// ============================================================================
// EVENT POSTER
// ============================================================================

@Composable
private fun EventPoster(
    event: MapEventInfo,
    modifier: Modifier = Modifier
) {

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Box(
            modifier = Modifier.fillMaxWidth()
        ) {

            when {

                event.posterResId != null -> {

                    Image(
                        painter = painterResource(
                            id = event.posterResId
                        ),
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                !event.posterUrl.isNullOrBlank() -> {

                    AsyncImage(
                        model = event.posterUrl,
                        contentDescription = event.title,
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Crop
                    )
                }

                else -> {

                    FallbackPoster(
                        title = event.title
                    )
                }
            }
        }
    }
}


// ============================================================================
// FALLBACK POSTER
// ============================================================================

@Composable
private fun FallbackPoster(
    title: String
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(185.dp)
    ) {

        Image(
            painter = painterResource(
                id = R.drawable.default_event_poster_bg
            ),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(
                                alpha = 0.15f
                            ),
                            Color.Black.copy(
                                alpha = 0.55f
                            )
                        )
                    )
                )
        )

        Text(
            text = title,
            color = Color.White,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontFamily = FontFamily.SansSerif,
            fontWeight = FontWeight.Black,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(
                    horizontal = 8.dp
                )
        )
    }
}


// ============================================================================
// EVENT TAG
// ============================================================================

@Composable
private fun EventTag(
    expanded: Boolean
) {

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(
                    OrangePrimary
                )
        )

        Spacer(
            modifier = Modifier.width(6.dp)
        )

        Text(
            text = if (expanded) {
                "EVENT"
            } else {
                "QUICK PREVIEW"
            },
            color = OrangePrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}


// ============================================================================
// META ROW
// ============================================================================

@Composable
private fun MetaRow(
    icon: ImageVector,
    text: String
) {

    Row(
        modifier = Modifier.padding(
            vertical = 3.dp
        ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = OrangePrimary,
            modifier = Modifier.size(18.dp)
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = text,
            color = TextDark,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


// ============================================================================
// STATS
// ============================================================================

@Composable
private fun StatsRow(
    registrationsCount: Int?,
    eventType: String?
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        horizontalArrangement =
            Arrangement.spacedBy(12.dp)
    ) {

        if (registrationsCount != null) {

            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.Group,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )

                    Text(
                        text = "Registrations",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Column {

                    Text(
                        text = registrationsCount.toString(),
                        color = TextDark,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "students",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }


        if (!eventType.isNullOrBlank()) {

            StatCard(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement =
                    Arrangement.SpaceBetween
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.SpaceBetween,
                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector =
                            Icons.Default.ConfirmationNumber,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(22.dp)
                    )

                    Text(
                        text = "Event Type",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(10.dp)
                        )
                        .background(
                            PillBg
                        )
                        .padding(
                            horizontal = 10.dp,
                            vertical = 4.dp
                        )
                ) {

                    Text(
                        text = eventType,
                        color = PillText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ============================================================================
// STAT CARD
// ============================================================================

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable ColumnScope.() -> Unit
) {

    Column(
        modifier = modifier
            .clip(
                RoundedCornerShape(16.dp)
            )
            .background(
                StatCardBg
            )
            .padding(14.dp),
        verticalArrangement = verticalArrangement,
        content = content
    )
}


// ============================================================================
// HOSTS
// ============================================================================

@Composable
private fun HostsSection(
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


// ============================================================================
// NOTIFY BUTTON
// ============================================================================

@Composable
private fun NotifyMeButton(
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
private fun RegisterActionButton(
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


// ============================================================================
// DATE FORMAT
// ============================================================================

private val isoDateTimeRegex =
    Regex(
        """\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?"""
    )


private val isoTimeOfDayRegex =
    Regex(
        """T(\d{2}):(\d{2})"""
    )


private fun formatEventDate(
    raw: String
): String {

    if (raw.isBlank()) {
        return "20 May 2025"
    }

    val inputPatterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss",
        "yyyy-MM-dd",
        "yyyy/MM/dd",
        "dd-MM-yyyy",
        "dd/MM/yyyy",
        "MM-dd-yyyy",
        "MM/dd/yyyy"
    )

    for (pattern in inputPatterns) {

        try {

            val parser =
                SimpleDateFormat(
                    pattern,
                    Locale.getDefault()
                )

            parser.isLenient = false

            val parsed =
                parser.parse(
                    raw.trim()
                ) ?: continue

            val output =
                SimpleDateFormat(
                    "dd MMM yyyy",
                    Locale.getDefault()
                )

            return output.format(parsed)

        } catch (_: Exception) {
            // Try next pattern.
        }
    }

    return raw
}


// ============================================================================
// TIME FORMAT
// ============================================================================

private fun to12Hour(
    hour24: Int,
    minute: Int
): String {

    val period =
        if (hour24 < 12) {
            "AM"
        } else {
            "PM"
        }

    val hour12Raw =
        hour24 % 12

    val hour12 =
        if (hour12Raw == 0) {
            12
        } else {
            hour12Raw
        }

    return String.format(
        Locale.getDefault(),
        "%02d:%02d %s",
        hour12,
        minute,
        period
    )
}


private fun extractIsoTime(
    candidate: String
): String? {

    val match =
        isoTimeOfDayRegex.find(
            candidate
        ) ?: return null

    val hour =
        match.groupValues[1]
            .toIntOrNull()
            ?: return null

    val minute =
        match.groupValues[2]
            .toIntOrNull()
            ?: return null

    return to12Hour(
        hour,
        minute
    )
}


private fun formatSingleTime(
    raw: String
): String? {

    extractIsoTime(raw)?.let {
        return it
    }

    val inputPatterns = listOf(
        "HH:mm",
        "H:mm",
        "hh:mm a",
        "h:mm a"
    )

    for (pattern in inputPatterns) {

        try {

            val parser =
                SimpleDateFormat(
                    pattern,
                    Locale.getDefault()
                )

            parser.isLenient = false

            val parsed =
                parser.parse(
                    raw.trim()
                ) ?: continue

            val output =
                SimpleDateFormat(
                    "hh:mm a",
                    Locale.getDefault()
                )

            return output.format(parsed)

        } catch (_: Exception) {
            // Try next pattern.
        }
    }

    return null
}


private fun formatEventTimeRange(
    raw: String,
    endTimeRaw: String?
): String {

    if (raw.isBlank()) {
        return "10:00 AM – 04:00 PM"
    }

    if (!endTimeRaw.isNullOrBlank()) {

        val start =
            formatSingleTime(raw)

        val end =
            formatSingleTime(
                endTimeRaw
            )

        if (
            start != null &&
            end != null
        ) {
            return "$start – $end"
        }
    }

    val isoMatches =
        isoDateTimeRegex
            .findAll(raw)
            .map {
                it.value
            }
            .toList()

    if (isoMatches.size >= 2) {

        val start =
            extractIsoTime(
                isoMatches[0]
            )

        val end =
            extractIsoTime(
                isoMatches[1]
            )

        if (
            start != null &&
            end != null
        ) {
            return "$start – $end"
        }
    }

    if (isoMatches.isEmpty()) {

        val separators = listOf(
            "–",
            " to ",
            "-"
        )

        for (separator in separators) {

            val split =
                raw.split(separator)
                    .map {
                        it.trim()
                    }

            if (
                split.size == 2 &&
                split.all {
                    it.isNotBlank()
                }
            ) {

                val start =
                    formatSingleTime(
                        split[0]
                    )

                val end =
                    formatSingleTime(
                        split[1]
                    )

                if (
                    start != null &&
                    end != null
                ) {
                    return "$start – $end"
                }
            }
        }
    }

    if (isoMatches.size == 1) {

        extractIsoTime(
            isoMatches[0]
        )?.let {
            return it
        }
    }

    formatSingleTime(raw)?.let {
        return it
    }

    return raw
}