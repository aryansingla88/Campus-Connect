package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.data.FakeRegistrationResponses
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType

private enum class ResponseTab(val label: String) {
    TOTAL("Total\nRegistrations"),
    INDIVIDUAL("Individual"),
    TEAMS("Teams"),
    PENDING("Pending\nReview"),
}

private enum class ResponseSort(val label: String) {
    LATEST("Latest first"),
    OLDEST("Oldest first"),
    NAME("Name A–Z"),
}

// Stat card palette (matches the design mockup)
private val TotalBg = Color(0xFFFDEEE3)
private val TotalFg = Color(0xFFF2570F)
private val IndividualBg = Color(0xFFE3F8EE)
private val IndividualFg = Color(0xFF12A05C)
private val TeamsBg = Color(0xFFEDEBFC)
private val TeamsFg = Color(0xFF6C4CF1)
private val PendingBg = Color(0xFFFFF3E3)
private val PendingFg = Color(0xFFF2570F)
private val CardBorder = Color(0xFFE0E3E8)

@Composable
fun RegistrationResponsesScreen(
    eventName: String,
    isPublished: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit = {},
    onMore: () -> Unit = {},
    onOpenResponse: (RegistrationResponseItem) -> Unit = {},
) {
    var selectedTab by remember { mutableStateOf(ResponseTab.TOTAL) }
    var query by remember { mutableStateOf("") }
    var sort by remember { mutableStateOf(ResponseSort.LATEST) }
    var statusFilter by remember { mutableStateOf<RegistrationResponseStatus?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val allResponses = FakeRegistrationResponses.responses
    val filteredResponses = remember(selectedTab, query, sort, statusFilter) {
        allResponses
            .asSequence()
            .filter { response ->
                when (selectedTab) {
                    ResponseTab.TOTAL -> true
                    ResponseTab.INDIVIDUAL -> response.type == RegistrationResponseType.INDIVIDUAL
                    ResponseTab.TEAMS -> response.type == RegistrationResponseType.TEAM
                    ResponseTab.PENDING -> response.status == RegistrationResponseStatus.PENDING
                }
            }
            .filter { response -> statusFilter == null || response.status == statusFilter }
            .filter { response ->
                query.isBlank() ||
                        response.name.contains(query, ignoreCase = true) ||
                        response.email.contains(query, ignoreCase = true)
            }
            .let { sequence ->
                when (sort) {
                    ResponseSort.LATEST -> sequence.sortedByDescending { it.id }
                    ResponseSort.OLDEST -> sequence.sortedBy { it.id }
                    ResponseSort.NAME -> sequence.sortedBy { it.name.lowercase() }
                }
            }
            .toList()
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            RegistrationHeader(
                eventName = eventName,
                isPublished = isPublished,
                onBack = onBack,
                action = {
                    IconButton(onClick = onMore) {
                        Icon(
                            Icons.Outlined.MoreVert,
                            contentDescription = "More options",
                            tint = TextPrimary
                        )
                    }
                }
            )
        },
        bottomBar = {
            RegistrationBottomBar {
                Button(
                    onClick = onExport,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangeLight,
                        contentColor = OrangePrimary,
                    ),
                    border = BorderStroke(1.dp, OrangeBorder),
                    elevation = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                ) {
                    Icon(Icons.Outlined.FileDownload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Export Responses", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    "Registration Responses",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "View and manage all submitted registrations.",
                    fontSize = 13.sp,
                    color = TextSecondary,
                )
            }

            item {
                ResponseStatTabs(
                    selectedTab = selectedTab,
                    responses = allResponses,
                    onSelected = { selectedTab = it },
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SearchField(
                        query = query,
                        onQueryChange = { query = it },
                        modifier = Modifier.weight(1f),
                    )

                    Box {
                        DropdownButton(
                            label = "Sort",
                            icon = Icons.Outlined.SwapVert,
                            onClick = { showSortMenu = true },
                        )
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                        ) {
                            ResponseSort.values().forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = { sort = option; showSortMenu = false },
                                )
                            }
                        }
                    }

                    Box {
                        DropdownButton(
                            label = "Filter",
                            icon = Icons.Outlined.FilterList,
                            onClick = { showFilterMenu = true },
                        )
                        DropdownMenu(
                            expanded = showFilterMenu,
                            onDismissRequest = { showFilterMenu = false },
                        ) {
                            DropdownMenuItem(
                                text = { Text("All statuses") },
                                onClick = { statusFilter = null; showFilterMenu = false },
                            )
                            DropdownMenuItem(
                                text = { Text("Confirmed") },
                                onClick = {
                                    statusFilter = RegistrationResponseStatus.CONFIRMED
                                    showFilterMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Pending") },
                                onClick = {
                                    statusFilter = RegistrationResponseStatus.PENDING
                                    showFilterMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Cancelled") },
                                onClick = {
                                    statusFilter = RegistrationResponseStatus.CANCELLED
                                    showFilterMenu = false
                                },
                            )
                        }
                    }
                }
            }

            if (filteredResponses.isEmpty()) {
                item { EmptyResponsesState() }
            } else {
                items(filteredResponses, key = { it.id }) { response ->
                    RegistrationResponseCard(response, onClick = { onOpenResponse(response) })
                }
            }
        }
    }
}

/**
 * Compact 44dp search field. Built on BasicTextField because OutlinedTextField
 * enforces a 56dp minimum height and clips its text when forced smaller.
 */
@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(10.dp)
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.height(44.dp),
        singleLine = true,
        textStyle = TextStyle(fontSize = 13.sp, color = TextPrimary),
        cursorBrush = SolidColor(OrangePrimary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(CardBg, shape)
                    .border(1.dp, CardBorder, shape)
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    Icons.Outlined.Search,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(18.dp),
                )
                Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(
                            "Search by name, team name, email…",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}

@Composable
private fun DropdownButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, CardBorder),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color(0xFFF7F8FA),
            contentColor = TextPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 10.dp),
        modifier = Modifier.height(44.dp),
    ) {
        Icon(icon, null, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
        Icon(Icons.Outlined.KeyboardArrowDown, null, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun ResponseStatTabs(
    selectedTab: ResponseTab,
    responses: List<RegistrationResponseItem>,
    onSelected: (ResponseTab) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        ResponseTab.values().forEach { tab ->
            val count = when (tab) {
                ResponseTab.TOTAL -> responses.size
                ResponseTab.INDIVIDUAL -> responses.count { it.type == RegistrationResponseType.INDIVIDUAL }
                ResponseTab.TEAMS -> responses.count { it.type == RegistrationResponseType.TEAM }
                ResponseTab.PENDING -> responses.count { it.status == RegistrationResponseStatus.PENDING }
            }
            val (bg, fg, icon) = when (tab) {
                ResponseTab.TOTAL -> Triple(TotalBg, TotalFg, Icons.Outlined.Groups)
                ResponseTab.INDIVIDUAL -> Triple(IndividualBg, IndividualFg, Icons.Outlined.Person)
                ResponseTab.TEAMS -> Triple(TeamsBg, TeamsFg, Icons.Outlined.Groups)
                ResponseTab.PENDING -> Triple(PendingBg, PendingFg, Icons.Outlined.Schedule)
            }
            val selected = tab == selectedTab

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelected(tab) },
                shape = RoundedCornerShape(14.dp),
                color = bg,
                border = BorderStroke(if (selected) 1.5.dp else 1.dp, if (selected) fg else fg.copy(alpha = 0.12f)),
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(26.dp))
                    Text(
                        count.toString(),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                    )
                    Text(
                        tab.label,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        textAlign = TextAlign.Center,
                        color = if (tab == ResponseTab.INDIVIDUAL) fg else TextPrimary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    )
                }
            }
        }
    }
}

@Composable
private fun RegistrationResponseCard(response: RegistrationResponseItem, onClick: () -> Unit) {
    val isTeam = response.type == RegistrationResponseType.TEAM

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            if (isTeam) {
                Box(
                    modifier = Modifier.size(48.dp).background(TeamsBg, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Outlined.Groups,
                        contentDescription = null,
                        tint = TeamsFg,
                        modifier = Modifier.size(26.dp),
                    )
                }
            } else {
                AppAvatar(
                    entityId = response.id,
                    displayName = response.name,
                    size = 48.dp,
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    response.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    if (isTeam) "Team${response.memberCount?.let { " ($it members)" } ?: ""}"
                    else "Individual",
                    fontSize = 12.sp,
                    color = TextSecondary,
                )
                Text(
                    response.email,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ResponseStatusChip(response.status)
                Text(response.submittedAt, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
            }

            Icon(
                Icons.Outlined.ChevronRight,
                contentDescription = "Open response",
                tint = TextPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ResponseStatusChip(status: RegistrationResponseStatus) {
    val background = when (status) {
        RegistrationResponseStatus.CONFIRMED -> Color(0xFFDDF6EA)
        RegistrationResponseStatus.PENDING -> Color(0xFFFFEFCF)
        RegistrationResponseStatus.CANCELLED -> Color(0xFFFDE1E3)
    }
    val foreground = when (status) {
        RegistrationResponseStatus.CONFIRMED -> Color(0xFF087443)
        RegistrationResponseStatus.PENDING -> Color(0xFF9A5B00)
        RegistrationResponseStatus.CANCELLED -> Color(0xFFC62828)
    }

    Surface(shape = RoundedCornerShape(50), color = background) {
        Text(
            text = status.name.lowercase().replaceFirstChar { it.uppercase() },
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = foreground,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun EmptyResponsesState() {
    Card(
        shape = RoundedCornerShape(15.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 34.dp, horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Outlined.Inbox, null, tint = TextSecondary, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(8.dp))
            Text("No responses found", fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Text("Try another search or filter.", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

