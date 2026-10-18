package com.example.campusconnect.feature.posts.screens

import androidx.compose.ui.graphics.Color
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeBg
import com.example.campusconnect.core.ui.theme.OrangeBorder
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.utils.toTemporaryImageFile
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class,ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialPostType: String = "GENERAL",
    onBackClick: () -> Unit,
    onPostCreated: () -> Unit,
    viewModel: FeedViewModel = FeedViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var postType by remember(initialPostType) { mutableStateOf(initialPostType) }
    var tags by remember { mutableStateOf<List<PostTag>>(emptyList()) }
    var selectedTags by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<java.io.File?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        imageFile = uri?.toTemporaryImageFile(context)
        if (uri != null && imageFile == null) {
            errorMessage = "Could not read the selected image."
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getTags().onSuccess { tags = it }
            .onFailure { errorMessage = it.message ?: "Failed to load tags." }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Post") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = OrangeBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Post type", style = MaterialTheme.typography.titleMedium)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("GENERAL", "EVENTS", "CUSTOM").forEach { type ->
                    FilterChip(
                        selected = postType == type,
                        onClick = { postType = type },
                        label = {
                            Text(type.lowercase().replaceFirstChar { it.uppercase() })
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = OrangeBg,
                            labelColor = Color.Black,
                            selectedContainerColor = OrangeBg,
                            selectedLabelColor = OrangePrimary
                        ),
                        border = BorderStroke(
                            1.dp,
                            if (postType == type) OrangePrimary else Color.Black
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    focusedLabelColor = OrangePrimary,
                    cursorColor = OrangePrimary
                ),
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Title") },
                singleLine = true,
                isError = title.isBlank() && errorMessage != null

            )

            OutlinedTextField(
                value = body,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    focusedLabelColor = OrangePrimary,
                    cursorColor = OrangePrimary
                ),
                onValueChange = { body = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                label = { Text("What do you want to share?") },
                minLines = 6
            )

            Text("Tags", style = MaterialTheme.typography.titleMedium)

            if (tags.isEmpty()) {
                Text("No tags are available yet.", color = ErrorRed)
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        FilterChip(
                            selected = selectedTags.contains(tag.id),
                            onClick = {
                                selectedTags = if (selectedTags.contains(tag.id)) {
                                    selectedTags - tag.id
                                } else {
                                    selectedTags + tag.id
                                }
                            },
                            label = { Text(tag.name) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = OrangeBg,
                                labelColor = LabelColor,
                                selectedContainerColor = OrangeLight,
                                selectedLabelColor = OrangePrimary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (selectedTags.contains(tag.id)) OrangePrimary else OrangeBorder
                            )
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = OrangePrimary),
                border = BorderStroke(1.dp, OrangePrimary)
            ) {
                Icon(
                    Icons.Outlined.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(if (imageUri == null) "Add image" else "Change image", color=OrangePrimary)
            }

            imageUri?.let { uri ->
                AsyncImage(
                    model = uri,
                    contentDescription = "Selected image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Crop
                )
            }

            errorMessage?.let {
                Text(it, color = ErrorRed)
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = White
                ),
                onClick = {
                    errorMessage = when {
                        title.isBlank() -> "Title is required."
                        body.isBlank() -> "Body is required."
                        selectedTags.isEmpty() -> "Select at least one tag."
                        else -> null
                    }

                    if (errorMessage != null) return@Button

                    isLoading = true
                    scope.launch {
                        val selected = tags.filter { selectedTags.contains(it.id) }

                        println("IMAGE FILE = $imageFile")
                        println("IMAGE EXISTS = ${imageFile?.exists()}")
                        println("IMAGE SIZE = ${imageFile?.length()} bytes")
                        println("IMAGE PATH = ${imageFile?.absolutePath}")

                        viewModel.createPost(
                            postType = postType,
                            title = title.trim(),
                            body = body.trim(),
                            tags = selected,
                            image = imageUri
                        ).onSuccess {
                            onPostCreated()
                        }.onFailure {
                            errorMessage = it.message ?: "Failed to create post."
                            snackbarHostState.showSnackbar(errorMessage!!)
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (isLoading) "Creating..." else "Create Post")
            }
        }
    }
}

