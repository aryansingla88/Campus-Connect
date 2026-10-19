package com.example.campusconnect.feature.registrations.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.ui.components.AccentPurple
import com.example.campusconnect.feature.registrations.ui.components.CardBg
import com.example.campusconnect.feature.registrations.ui.components.DetailLimits
import com.example.campusconnect.feature.registrations.ui.components.OrangePrimary
import com.example.campusconnect.feature.registrations.ui.components.RegistrationDetailBody
import com.example.campusconnect.feature.registrations.ui.components.SubtleDivider
import com.example.campusconnect.feature.registrations.ui.components.TextPrimary
import com.example.campusconnect.feature.registrations.ui.components.TextSecondary
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationDetailState
import com.example.campusconnect.feature.registrations.viewmodel.RegistrationResponsesViewModel

/**
 * Full-screen response: every question, every quick field, the member selector,
 * no question limits and no answer truncation. Shares [viewModel] with the list,
 * so already-loaded data is reused (no refetch) and the selected member carries over.
 */
@Composable
fun RegistrationResponseDetailScreen(
    response: RegistrationResponseItem,
    viewModel: RegistrationResponsesViewModel,
    onBack: () -> Unit,
    onMore: () -> Unit = {},
) {
    val detailStates by viewModel.details.collectAsState()
    val selectedMembers by viewModel.selectedMembers.collectAsState()
    val state = detailStates[response.id]

    // No-op when the list already loaded this registration.
    LaunchedEffect(response.id) { viewModel.loadDetails(response.id) }

    Scaffold(
        containerColor = CardBg,
        topBar = { DetailTopBar(response, onBack, onMore) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding(),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                RegistrationAvatar(response)
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        response.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(registrationTypeLabel(response), fontSize = 12.sp, color = TextSecondary)
                    Text(
                        response.email,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(response.submittedAt, fontSize = 11.sp, color = TextSecondary)
            }

            SubtleDivider()

            when (state) {
                null, RegistrationDetailState.Loading -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp,
                        color = OrangePrimary,
                    )
                }

                is RegistrationDetailState.Failed -> Column(
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(state.message, fontSize = 14.sp, color = TextSecondary, textAlign = TextAlign.Center)
                    TextButton(onClick = { viewModel.loadDetails(response.id, force = true) }) {
                        Text("Retry", color = AccentPurple)
                    }
                }

                is RegistrationDetailState.Loaded -> RegistrationDetailBody(
                    registrationId = response.id,
                    detail = state.detail,
                    selectedUserId = selectedMembers[response.id],
                    onSelectMember = { viewModel.selectMember(response.id, it) },
                    limits = DetailLimits.Full,
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DetailTopBar(response: RegistrationResponseItem, onBack: () -> Unit, onMore: () -> Unit) {
    Surface(color = CardBg, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxWidth()
                .heightIn(min = 64.dp)
                .padding(start = 4.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column(Modifier.weight(1f).padding(start = 4.dp, end = 8.dp)) {
                Text(
                    response.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                ResponseStatusChip(response.status)
            }
            IconButton(onClick = onMore) {
                Icon(Icons.Outlined.MoreVert, contentDescription = "More options", tint = TextPrimary)
            }
        }
    }
}
