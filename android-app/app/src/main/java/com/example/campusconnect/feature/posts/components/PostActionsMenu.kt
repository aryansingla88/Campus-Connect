package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.White

@Composable
fun PostActionsMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val menuShape = RoundedCornerShape(8.dp)

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            surface = White,
            surfaceTint = White,
            onSurface = Color.Black,
            primary = OrangePrimary
        )
    ) {
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = onDismiss,
            offset = DpOffset(x = 8.dp, y = 4.dp),
            modifier = Modifier
                .clip(menuShape)
        ) {
            DropdownMenuItem(
                text = { Text("Edit post", color = Color.Black) },
                onClick = {
                    onDismiss()
                    onEdit()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 10.dp),
                color = Color(0xFFE2E2E2),
                thickness = 1.dp
            )

            DropdownMenuItem(
                text = { Text("Delete post", color = Color.Black) },
                onClick = {
                    onDismiss()
                    onDelete()
                }
            )
        }
    }
}
