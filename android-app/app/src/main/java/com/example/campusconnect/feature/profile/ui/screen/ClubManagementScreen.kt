package com.example.campusconnect.feature.profile.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.components.AppAvatar
import com.example.campusconnect.core.components.AvatarShape
import com.example.campusconnect.core.util.formatBackendDateTime
import com.example.campusconnect.feature.profile.model.*
import com.example.campusconnect.feature.profile.viewmodel.BaseProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClubManagementRoute(
    club: Club,
    vm: BaseProfileViewModel,
    onBack: () -> Unit
) {
    if (club.status != ClubStatus.LEAD) {
        Scaffold(
            topBar = { ManagementTopBar("Club management", onBack) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).padding(24.dp), contentAlignment = Alignment.Center) {
                Text("Management access is only available to club leaders.")
            }
        }
        return
    }

    LaunchedEffect(club.clubId) { vm.loadClubRequests(club.clubId, force = true) }
    val requests = vm.clubRequests[club.clubId]
    val loading = club.clubId in vm.clubRequestsLoadingIds
    val refreshing = club.clubId in vm.clubRequestsRefreshingIds
    val error = vm.clubRequestsErrors[club.clubId]
    val snackbar = remember { SnackbarHostState() }
    var rejectTarget by remember(club.clubId) { mutableStateOf<ClubMembershipRequest?>(null) }
    var approveTarget by remember(club.clubId) { mutableStateOf<ClubMembershipRequest?>(null) }
    var selectedRole by remember(club.clubId) { mutableStateOf(ClubMemberRole.MEMBER) }

    LaunchedEffect(club.clubId, vm.clubRequestSuccessMessages[club.clubId]) {
        vm.clubRequestSuccessMessages[club.clubId]?.let { message ->
            snackbar.showSnackbar(message)
            vm.clubRequestSuccessMessages.remove(club.clubId)
        }
    }

    Scaffold(
        topBar = { ManagementTopBar("Manage ${club.name}", onBack) },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppAvatar(club.clubId, club.name, club.logoUrl, size = 48.dp, shape = AvatarShape.ROUNDED)
                Column(Modifier.weight(1f)) {
                    Text(club.name, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${club.memberCount} members", style = MaterialTheme.typography.bodySmall)
                }
            }
            TabRow(selectedTabIndex = 0) { Tab(selected = true, onClick = {}, text = { Text("Requests") }) }
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pending requests", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { vm.loadClubRequests(club.clubId, force = true) }, enabled = !loading && !refreshing) {
                    if (refreshing) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    else Icon(Icons.Outlined.Refresh, contentDescription = "Refresh requests")
                }
            }

            if (error != null && requests != null) {
                Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }

            when {
                loading && requests == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null && requests == null -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(onClick = { vm.loadClubRequests(club.clubId, force = true) }) { Text("Try again") }
                }
                requests.isNullOrEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No pending requests", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(requests, key = { "${it.clubId}:${it.userId}" }) { request ->
                        val inProgress = vm.clubRequestActions["${club.clubId}:${request.userId}"] == true
                        RequestCard(
                            request = request,
                            inProgress = inProgress,
                            onApprove = {
                                selectedRole = ClubMemberRole.MEMBER
                                approveTarget = request
                            },
                            onReject = { rejectTarget = request }
                        )
                    }
                }
            }
        }
    }

    approveTarget?.let { request ->
        val roles = approvalRoles(club.clubType)
        AlertDialog(
            onDismissRequest = { approveTarget = null },
            title = { Text("Approve ${request.fullName}") },
            text = {
                Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                    Text("Choose a membership role. Members is selected by default.")
                    roles.forEach { role ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = role == selectedRole, onClick = { selectedRole = role })
                            Text(role.label())
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.approveClubRequest(club.clubId, request.userId, selectedRole)
                        approveTarget = null
                    },
                    enabled = vm.clubRequestActions["${club.clubId}:${request.userId}"] != true
                ) { Text("Approve") }
            },
            dismissButton = { TextButton(onClick = { approveTarget = null }) { Text("Cancel") } }
        )
    }

    rejectTarget?.let { request ->
        AlertDialog(
            onDismissRequest = { rejectTarget = null },
            title = { Text("Reject request?") },
            text = { Text("Reject ${request.fullName}'s request to join ${club.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.rejectClubRequest(club.clubId, request.userId)
                        rejectTarget = null
                    },
                    enabled = vm.clubRequestActions["${club.clubId}:${request.userId}"] != true
                ) { Text("Reject", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { rejectTarget = null }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ManagementTopBar(title: String, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
        }
    )
}

@Composable
private fun RequestCard(
    request: ClubMembershipRequest,
    inProgress: Boolean,
    onApprove: () -> Unit,
    onReject: () -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AppAvatar(request.userId, request.fullName, request.avatarUrl, size = 48.dp)
                Column(Modifier.weight(1f)) {
                    Text(request.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    request.username?.let {
                        Text(if (it.startsWith("@")) it else "@$it", style = MaterialTheme.typography.bodySmall,
                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    request.requestedAt?.let {
                        Text("Requested ${formatBackendDateTime(it)}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = onReject, enabled = !inProgress, modifier = Modifier.weight(1f)) {
                    Text("Reject")
                }
                Button(onClick = onApprove, enabled = !inProgress, modifier = Modifier.weight(1f)) {
                    if (inProgress) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("Approve")
                }
            }
        }
    }
}

private fun approvalRoles(clubType: String): List<ClubMemberRole> {
    val normalized = clubType.trim().uppercase().replace("_", " ")
    val leadershipRole = when (normalized) {
        "CLUB", "STUDENT CLUB" -> ClubMemberRole.SECRETARY
        "SOCIETY" -> ClubMemberRole.PRESIDENT
        else -> null
    }
    return ClubMemberRole.entries.filter { it != leadershipRole }
}

private fun ClubMemberRole.label(): String = name.lowercase().replace('_', ' ')
    .replaceFirstChar { it.uppercase() }
