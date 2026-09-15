package com.example.campusconnect.core.utils.Image


import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.FileProvider
import androidx.compose.ui.platform.LocalContext
import java.io.File

enum class ImageSource {
    CAMERA,
    GALLERY
}

@Composable
fun rememberImagePicker(
    onImagePicked: (Uri) -> Unit
): (ImageSource) -> Unit {

    val context = LocalContext.current

    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File.createTempFile(
                "campusconnect_image_",
                ".jpg",
                context.cacheDir
            )
        )
    }

    val galleryLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri ->
            uri?.let(onImagePicked)
        }

    val cameraLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                onImagePicked(cameraUri)
            }
        }

    return remember {
        { source: ImageSource ->
            when (source) {

                ImageSource.GALLERY -> {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }

                ImageSource.CAMERA -> {
                    cameraLauncher.launch(cameraUri)
                }
            }
        }
    }
}