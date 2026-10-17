package com.example.campusconnect.core.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@Composable
fun <T> SearchPopup(
    visible: Boolean,
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    items: List<T>,
    onSearch: (String) -> Unit,
    itemContent: @Composable (T) -> Unit,
    onDismiss: () -> Unit,
    showEmbeddedPlus: Boolean = false
) {
    if (!visible) return

    LaunchedEffect(query) {
        if (query.length < 2) {
            return@LaunchedEffect
        }

        delay(300)
        onSearch(query)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
                    .padding(14.dp)
            ) {

                PanelSearchBar(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = placeholder,
                    showEmbeddedPlus = showEmbeddedPlus
                )

                Spacer(
                    modifier = Modifier.height(10.dp)
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(
                            rememberScrollState()
                        ),
                    verticalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    items.forEach { item ->
                        itemContent(item)
                    }
                }
            }
        }
    }
}