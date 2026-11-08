package com.example.campusconnect.feature.registrations.ui.screens
import com.example.campusconnect.feature.registrations.ui.components.*
import com.example.campusconnect.feature.registrations.ui.components.responses.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusconnect.feature.registrations.RegistrationDependencies
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType
import com.example.campusconnect.feature.registrations.ui.model.ResponseSort
import com.example.campusconnect.feature.registrations.ui.model.ResponseTab
import com.example.campusconnect.feature.registrations.ui.model.countIn
import com.example.campusconnect.feature.registrations.ui.model.forResponseList
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationDetailState
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel

@Composable
fun RegistrationResponsesScreen(
    eventId: Int,
    eventName: String,
    isPublished: Boolean,
    onBack: () -> Unit,
    onExport: () -> Unit = {},
    onFullResponse: (RegistrationResponseItem) -> Unit = {},
    onRefresh: () -> Unit = {},
    viewModel: RegistrationResponsesViewModel = viewModel(
        factory = RegistrationDependencies.registrationResponsesViewModelFactory,
    ),
) {
    val detailStates by viewModel.details.collectAsState()
    val selectedMembers by viewModel.selectedMembers.collectAsState()
    val expandedId by viewModel.expandedId.collectAsState()
    val listState by viewModel.listState.collectAsState()
    LaunchedEffect(eventId) { viewModel.loadRegistrations(eventId) }

    var selectedTab by rememberSaveable { mutableStateOf(ResponseTab.TOTAL) }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf(ResponseSort.LATEST) }
    var statusFilter by rememberSaveable { mutableStateOf<RegistrationResponseStatus?>(null) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }

    val allResponses = if (listState.eventId == eventId && listState.loaded) listState.registrations else emptyList()
    val filteredResponses = remember(selectedTab, query, sort, statusFilter, allResponses) {
        allResponses.forResponseList(selectedTab, query, sort, statusFilter)
    }

    Scaffold(
        containerColor = PageBg,
        topBar = {
            RegistrationHeader(
                eventName = eventName,
                isPublished = isPublished,
                onBack = onBack,
                action = {
                    IconButton(onClick = onRefresh) {
                        Icon(
                            Icons.Outlined.Refresh,
                            contentDescription = "Refresh registrations",
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

            if (listState.loaded) item {
                ResponseStatTabs(
                    selectedTab = selectedTab,
                    responses = allResponses,
                    onSelected = { selectedTab = it },
                )
            }

            if (listState.reviewError != null) item {
                Text(listState.reviewError.orEmpty(), color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }

            if (listState.loaded) item {
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
                                text = { Text("Pending Review") },
                                onClick = {
                                    statusFilter = RegistrationResponseStatus.WAITLISTED
                                    showFilterMenu = false
                                },
                            )
                            DropdownMenuItem(
                                text = { Text("Rejected") },
                                onClick = {
                                    statusFilter = RegistrationResponseStatus.REJECTED
                                    showFilterMenu = false
                                },
                            )
                        }
                    }
                }
            }

            when {
                listState.eventId != eventId || listState.loading -> item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = OrangePrimary)
                    }
                }
                listState.error != null -> item {
                    Card(colors = CardDefaults.cardColors(containerColor = CardBg)) {
                        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(listState.error.orEmpty(), color = TextSecondary, textAlign = TextAlign.Center)
                            TextButton(onClick = onRefresh) { Text("Retry", color = AccentPurple) }
                        }
                    }
                }
                listState.registrations.isEmpty() -> item { EmptyResponsesState(genuinelyEmpty = true) }
                filteredResponses.isEmpty() -> item { EmptyResponsesState(genuinelyEmpty = false) }
                else -> {
                    items(filteredResponses, key = { it.id }) { response ->
                        RegistrationResponseCard(
                            response = response,
                            expanded = expandedId == response.id,
                            detailState = detailStates[response.id],
                            selectedUserId = selectedMembers[response.id],
                            onToggle = { viewModel.toggleExpanded(response.id) },
                            onSelectMember = { viewModel.selectMember(response.id, it) },
                            onRetry = { viewModel.loadDetails(eventId, response.id, force = true) },
                            onFullResponse = { onFullResponse(response) },
                            onReview = { approve -> viewModel.reviewRegistration(response.id, approve) },
                            isReviewing = listState.reviewingRegistrationId == response.id,
                        )
                    }
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
                    .border(1.dp, ResponseCardBorder, shape)
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
        border = BorderStroke(1.dp, ResponseCardBorder),
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
            val count = tab.countIn(responses)
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
