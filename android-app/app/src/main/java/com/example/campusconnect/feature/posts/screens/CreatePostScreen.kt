package com.example.campusconnect.feature.posts.screens

import androidx.compose.ui.graphics.Color
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.models.PostEventCategory
import com.example.campusconnect.feature.posts.utils.formatEventDateTime
import com.example.campusconnect.feature.posts.utils.toTemporaryImageFile
import com.example.campusconnect.feature.posts.utils.resolvePostImageUrl
import com.example.campusconnect.feature.posts.viewmodel.FeedViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class,ExperimentalMaterial3Api::class)
@Composable
fun CreatePostScreen(
    initialPostType: String = "GENERAL",
    editPostId: Int? = null,
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
    var removeExistingImage by remember(editPostId) { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var existingPost by remember(editPostId) { mutableStateOf<com.example.campusconnect.feature.posts.models.Post?>(null) }
    var isLoadingPost by remember(editPostId) { mutableStateOf(editPostId != null) }
    var myEvents by remember { mutableStateOf<List<PostEvent>>(emptyList()) }
    var selectedEvent by remember { mutableStateOf<PostEvent?>(null) }
    var eventCategories by remember { mutableStateOf<List<PostEventCategory>>(emptyList()) }
    var selectedEventCategoryId by remember { mutableStateOf<Int?>(null) }
    var categoryLoadError by remember { mutableStateOf<String?>(null) }
    var isLoadingEvents by remember { mutableStateOf(false) }
    var eventLoadError by remember { mutableStateOf<String?>(null) }
    val eventPostMode = postType == "EVENTS"

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        imageUri = uri
        imageFile = uri?.toTemporaryImageFile(context)
        if (uri != null) removeExistingImage = false
        if (uri != null && imageFile == null) {
            errorMessage = "Could not read the selected image."
        }
    }

    LaunchedEffect(editPostId) {
        if (editPostId != null) {
            viewModel.getPost(editPostId)
                .onSuccess { post ->
                    existingPost = post
                    title = post.title
                    body = post.body
                    selectedTags = post.tags.map { it.id }.toSet()
                    postType = post.postType
                    removeExistingImage = false
                    if (post.postType == "EVENTS") {
                        selectedEventCategoryId = post.eventCategoryId
                        selectedEvent = post.eventId?.let { eventId ->
                            PostEvent(
                                id = eventId,
                                title = post.title,
                                organizerName = post.eventOrganizerName,
                                description = post.body,
                                latitude = post.eventLatitude,
                                longitude = post.eventLongitude,
                                startTime = post.eventStartTime.orEmpty(),
                                venue = post.eventVenue,
                                posterUrl = post.imageUrl,
                                categoryName = post.eventCategoryName,
                                categoryId = post.eventCategoryId,
                                postId = post.id
                            )
                        }
                        if (selectedEvent == null) errorMessage = "Event details could not be loaded."
                    }
                    if (!post.canDelete) errorMessage = "Only the post author can edit this post."
                }
                .onFailure { errorMessage = it.message ?: "Failed to load post." }
            isLoadingPost = false
        }
    }

    LaunchedEffect(editPostId, postType) {
        if (postType != "EVENTS") {
            viewModel.getTags().onSuccess { tags = it }
                .onFailure { errorMessage = it.message ?: "Failed to load tags." }
        }
    }

    LaunchedEffect(editPostId, postType) {
        if (postType == "EVENTS") {
            isLoadingEvents = true
            eventLoadError = null
            if (editPostId == null) {
                viewModel.getMyEvents()
                    .onSuccess { myEvents = it }
                    .onFailure { eventLoadError = it.message ?: "Failed to load your events." }
            }
            categoryLoadError = null
            viewModel.getEventCategories()
                .onSuccess { eventCategories = it }
                .onFailure { categoryLoadError = it.message ?: "Failed to load event categories." }
            isLoadingEvents = false
        } else if (editPostId == null) {
            selectedEvent = null
            selectedEventCategoryId = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editPostId == null) "Create Post" else "Edit Post") },
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
            if (editPostId == null) Text("Post type", style = MaterialTheme.typography.titleMedium)

            if (editPostId == null) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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

            if (eventPostMode) {
                if (editPostId == null) {
                    Text("Choose one of your events", style = MaterialTheme.typography.titleMedium)
                    when {
                        isLoadingEvents -> Text("Loading your events…", color = LabelColor)
                        eventLoadError != null -> Text(eventLoadError!!, color = ErrorRed)
                        myEvents.isEmpty() -> Text("You haven’t created any events yet.", color = LabelColor)
                        else -> myEvents.forEach { event ->
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedEvent = event
                                    selectedEventCategoryId = event.categoryId
                                    body = event.description.orEmpty()
                                    imageUri = null
                                    imageFile = null
                                    removeExistingImage = false
                                    errorMessage = null
                                },
                            colors = CardDefaults.elevatedCardColors(
                                containerColor = if (selectedEvent?.id == event.id) OrangeLight else White
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                resolvePostImageUrl(event.posterUrl)?.let { poster ->
                                    AsyncImage(
                                        model = poster,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(Modifier.size(12.dp))
                                }
                                Column(Modifier.weight(1f)) {
                                    Text(event.title, style = MaterialTheme.typography.titleSmall)
                                    Text(
                                        listOfNotNull(event.categoryName, formatEventDateTime(event.startTime)).joinToString(" · "),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LabelColor
                                    )
                                }
                                if (selectedEvent?.id == event.id) Text("Selected", color = OrangePrimary)
                            }
                        }
                    }
                    }
                }

                selectedEvent?.let { event ->
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.elevatedCardColors(containerColor = White)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Event title", style = MaterialTheme.typography.labelMedium, color = LabelColor)
                            OutlinedTextField(
                                value = event.title,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = OrangeBorder,
                                    focusedLabelColor = OrangePrimary,
                                    cursorColor = OrangePrimary
                                )
                            )
                            (event.organizerName ?: event.hostName)
                                ?.takeIf(String::isNotBlank)
                                ?.let { Text(it, color = LabelColor) }

                            Text("Event category", style = MaterialTheme.typography.titleSmall)
                            if (categoryLoadError != null) {
                                Text(categoryLoadError!!, color = ErrorRed)
                            } else if (eventCategories.isEmpty()) {
                                Text("No event categories are available.", color = LabelColor)
                            } else {
                                FlowRow(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    eventCategories.forEach { category ->
                                        FilterChip(
                                            selected = selectedEventCategoryId == category.id,
                                            onClick = { selectedEventCategoryId = category.id },
                                            label = { Text(category.name) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                containerColor = OrangeBg,
                                                labelColor = LabelColor,
                                                selectedContainerColor = OrangeLight,
                                                selectedLabelColor = OrangePrimary
                                            ),
                                            border = BorderStroke(
                                                1.dp,
                                                if (selectedEventCategoryId == category.id) OrangePrimary else OrangeBorder
                                            )
                                        )
                                    }
                                }
                            }

                            Text("Date & time: ${formatEventDateTime(event.startTime)}", color = LabelColor)
                            Text(
                                "Location: ${event.venue?.takeIf(String::isNotBlank) ?: listOfNotNull(event.latitude, event.longitude).joinToString(", ")}",
                                color = LabelColor
                            )

                            OutlinedTextField(
                                value = body,
                                onValueChange = { body = it },
                                modifier = Modifier.fillMaxWidth().height(150.dp),
                                label = { Text("Event post description") },
                                minLines = 4,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = OrangePrimary,
                                    unfocusedBorderColor = OrangeBorder,
                                    focusedLabelColor = OrangePrimary,
                                    cursorColor = OrangePrimary
                                )
                            )

                            OutlinedButton(
                                onClick = { imagePicker.launch("image/*") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary),
                                border = BorderStroke(1.dp, OrangePrimary)
                            ) {
                                Icon(Icons.Outlined.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.size(8.dp))
                                Text(
                                    when {
                                        imageUri != null -> "Change event post photo"
                                        event.posterUrl != null && !removeExistingImage -> "Change event post photo"
                                        else -> "Add event post photo"
                                    },
                                    color = OrangePrimary
                                )
                            }
                            if (editPostId != null && (event.posterUrl != null || imageUri != null)) {
                                TextButton(
                                    onClick = {
                                        imageUri = null
                                        imageFile = null
                                        removeExistingImage = existingPost?.imageUrl != null
                                    }
                                ) { Text("Remove photo", color = OrangePrimary) }
                            }
                            if (editPostId != null && removeExistingImage) {
                                TextButton(onClick = { removeExistingImage = false }) {
                                    Text("Undo photo removal", color = OrangePrimary)
                                }
                            }
                            val selectedPosterModel: Any? = imageUri ?: if (!removeExistingImage) {
                                resolvePostImageUrl(event.posterUrl)
                            } else {
                                null
                            }
                            selectedPosterModel?.let { poster ->
                                AsyncImage(
                                    model = poster,
                                    contentDescription = "${event.title} poster",
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Text(
                                "The description and photo belong to the post. The selected category is saved on the event.",
                                color = LabelColor,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            } else {
            OutlinedTextField(
                value = title,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OrangePrimary,
                    focusedLabelColor = OrangePrimary,
                    cursorColor = OrangePrimary
                ),
                onValueChange = { title = it },
                readOnly = editPostId != null,
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

            if ((editPostId == null || existingPost != null) && existingPost?.imageUrl == null) OutlinedButton(
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

            if (existingPost?.imageUrl != null) {
                AsyncImage(
                    model = resolvePostImageUrl(existingPost?.imageUrl),
                    contentDescription = "Post image (read only)",
                    modifier = Modifier.fillMaxWidth().height(220.dp),
                    contentScale = ContentScale.Crop
                )
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
                        eventPostMode && selectedEvent == null -> "Select an event to publish."
                        eventPostMode && body.isBlank() -> "Add a description for this event post."
                        eventPostMode && selectedEventCategoryId == null -> "Select an event category."
                        eventPostMode -> null
                        editPostId == null && title.isBlank() -> "Title is required."
                        body.isBlank() -> "Body is required."
                        selectedTags.isEmpty() -> "Select at least one tag."
                        else -> null
                    }

                    if (errorMessage != null) return@Button

                    isLoading = true
                    scope.launch {
                        val selected = tags.filter { selectedTags.contains(it.id) }

                        val result = if (editPostId != null) {
                            viewModel.updatePost(
                                postId = editPostId,
                                body = body.trim(),
                                tags = selected,
                                image = imageUri,
                                eventCategoryId = if (eventPostMode) selectedEventCategoryId else null,
                                removeImage = eventPostMode && removeExistingImage && imageUri == null
                            )
                        } else if (eventPostMode) {
                            val event = selectedEvent!!
                            viewModel.createPost(
                                postType = "EVENTS",
                                title = event.title,
                                body = body.trim(),
                                tags = emptyList(),
                                image = imageUri,
                                eventId = event.id,
                                eventCategoryId = selectedEventCategoryId
                            )
                        } else {
                            viewModel.createPost(
                                postType = postType,
                                title = title.trim(),
                                body = body.trim(),
                                tags = selected,
                                image = imageUri
                            )
                        }
                        result.onSuccess {
                            onPostCreated()
                        }.onFailure {
                            errorMessage = it.message ?: if (eventPostMode) "Failed to publish this event." else "Failed to create post."
                            snackbarHostState.showSnackbar(errorMessage!!)
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading && !isLoadingPost && (editPostId == null || existingPost?.canDelete == true),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(when { isLoading -> if (editPostId == null) "Creating..." else "Saving..."; isLoadingPost -> "Loading..."; editPostId != null -> "Save Changes"; else -> "Create Post" })
            }
        }
    }
}

