package com.example.campusconnect.core.utils.Image

import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerSheet(
    onDismiss: () -> Unit,
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        ListItem(
            headlineContent = {
                Text("Take Photo")
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable {
                onCameraClick()
            }
        )

        ListItem(
            headlineContent = {
                Text("Choose from Gallery")
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null
                )
            },
            modifier = Modifier.clickable {
                onGalleryClick()
            }
        )
    }
}