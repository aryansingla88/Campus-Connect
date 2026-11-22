package com.example.campusconnect.feature.posts.screens

import androidx.compose.ui.graphics.Color
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.Check
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeBorder
import com.example.campusconnect.core.ui.theme.OrangeLight
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.PostsBackground
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.feature.posts.models.PostTag
import com.example.campusconnect.feature.posts.models.PostEvent
import com.example.campusconnect.feature.posts.models.PostEventCategory
import com.example.campusconnect.feature.posts.components.CreatePostHeaderArt
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
    var postType by remember(initialPostType) {
        mutableStateOf(if (initialPostType.equals("EVENTS", ignoreCase = true)) "EVENTS" else "GENERAL")
    }
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
                    selectedTags = post.tags.firstOrNull()?.let { setOf(it.id) }.orEmpty()
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
            if (eventPostMode) {
                TopAppBar(
                    title = { Text(if (editPostId == null) "Create Post" else "Edit Post") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            } else {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(bottomStart = 30.dp, bottomEnd = 30.dp),
                    color = OrangePrimary,
                    shadowElevation = 3.dp
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(82.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colorStops = arrayOf(
                                        0f to Color(0xFFFF5B00),
                                        0.52f to OrangePrimary,
                                        1f to Color(0xFFFF9A1F)
                                    )
                                )
                            )
                    ) {
                        CreatePostHeaderArt(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .width(175.dp)
                                .fillMaxSize()
                        )
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBackClick) {
                                Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = White)
                            }
                            Text(
                                text = if (editPostId == null) "Create Post" else "Edit Post",
                                color = White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = if (eventPostMode) PostsBackground else Color(0xFFFFFAF6)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(if (eventPostMode) 16.dp else 18.dp),
            verticalArrangement = Arrangement.spacedBy(if (eventPostMode) 14.dp else 16.dp)
        ) {
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
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
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
                            val lockedCategoryName = eventCategories
                                .firstOrNull { it.id == selectedEventCategoryId }
                                ?.name
                                ?.takeIf(String::isNotBlank)
                                ?: event.categoryName?.takeIf(String::isNotBlank)
                            if (lockedCategoryName != null) {
                                Surface(
                                    color = White,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, OrangePrimary)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                                    ) {
                                        Text(
                                            text = lockedCategoryName,
                                            color = OrangePrimary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                        Icon(
                                            imageVector = Icons.Outlined.Lock,
                                            contentDescription = "Event category is locked",
                                            tint = OrangePrimary,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            } else if (categoryLoadError != null) {
                                Text(categoryLoadError!!, color = ErrorRed)
                            } else {
                                Text("This event has no category.", color = ErrorRed)
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
                                    focusedContainerColor = White,
                                    unfocusedContainerColor = White,
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
                PostComposerTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = "Title",
                    modifier = Modifier.fillMaxWidth().height(68.dp),
                    singleLine = true,
                    readOnly = editPostId != null,
                    isError = title.isBlank() && errorMessage != null
                )

                PostComposerTextField(
                    value = body,
                    onValueChange = { body = it },
                    placeholder = "What do you want to share?",
                    modifier = Modifier.fillMaxWidth().height(220.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocalOffer,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text("Select Categories", style = MaterialTheme.typography.titleMedium, color = LabelColor)
                }

                if (tags.isEmpty()) {
                    Text("No tags are available yet.", color = ErrorRed)
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tags.forEach { tag ->
                            val selected = selectedTags.contains(tag.id)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedTags = if (selected) emptySet() else setOf(tag.id)
                                },
                                label = { Text(tag.name) },
                                trailingIcon = if (selected) {
                                    {
                                        Icon(
                                            Icons.Outlined.Check,
                                            contentDescription = null,
                                            tint = White,
                                            modifier = Modifier.size(17.dp)
                                        )
                                    }
                                } else null,
                                shape = RoundedCornerShape(24.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = White,
                                    labelColor = LabelColor,
                                    selectedContainerColor = OrangePrimary,
                                    selectedLabelColor = White
                                ),
                                border = BorderStroke(1.dp, if (selected) OrangePrimary else OrangeBorder)
                            )
                        }
                    }
                }

                if ((editPostId == null || existingPost != null) && existingPost?.imageUrl == null) {
                    val imageShape = RoundedCornerShape(20.dp)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(104.dp)
                            .clip(imageShape)
                            .background(Color(0xFFFFF3E8))
                            .drawWithContent {
                                drawContent()
                                val dash = 9.dp.toPx()
                                drawRoundRect(
                                    color = OrangePrimary,
                                    cornerRadius = CornerRadius(20.dp.toPx()),
                                    style = Stroke(
                                        width = 1.2.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(dash, 6.dp.toPx()))
                                    )
                                )
                            }
                            .clickable { imagePicker.launch("image/*") }
                            .padding(horizontal = 18.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Surface(color = OrangeLight, shape = RoundedCornerShape(15.dp)) {
                            Icon(
                                Icons.Outlined.AddPhotoAlternate,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.padding(12.dp).size(28.dp)
                            )
                        }
                        Column {
                            Text(
                                if (imageUri == null) "Add Image (Optional)" else "Change Image",
                                style = MaterialTheme.typography.titleSmall,
                                color = LabelColor
                            )
                            Text(
                                "Tap to upload or choose from gallery",
                                style = MaterialTheme.typography.bodySmall,
                                color = LabelColor.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                if (existingPost?.imageUrl != null) {
                    AsyncImage(
                        model = resolvePostImageUrl(existingPost?.imageUrl),
                        contentDescription = "Post image (read only)",
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )
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
                        modifier = Modifier.fillMaxWidth().height(220.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            errorMessage?.let {
                Text(it, color = ErrorRed)
            }

            Button(
                shape = if (eventPostMode) RoundedCornerShape(12.dp) else RoundedCornerShape(32.dp),
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (eventPostMode) 48.dp else 58.dp)
            ) {
                if (!eventPostMode) {
                    Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                }
                Text(when { isLoading -> if (editPostId == null) "Creating..." else "Saving..."; isLoadingPost -> "Loading..."; editPostId != null -> "Save Changes"; else -> "Create Post" })
            }

        }
    }
}

@Composable
private fun PostComposerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    readOnly: Boolean = false,
    isError: Boolean = false
) {
    val fieldShape = RoundedCornerShape(22.dp)
    val inputTextStyle = MaterialTheme.typography.bodyLarge.copy(color = LabelColor)
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    val fieldBorderColor = when {
        isFocused -> OrangePrimary
        isError -> ErrorRed
        else -> OrangeBorder
    }
    Surface(
        modifier = modifier,
        shape = fieldShape,
        color = White,
        border = BorderStroke(if (isFocused) 1.5.dp else 1.dp, fieldBorderColor),
        shadowElevation = if (isFocused) 2.dp else 0.dp
    ) {
        if (singleLine) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = readOnly,
                    singleLine = true,
                    interactionSource = interactionSource,
                    textStyle = inputTextStyle,
                    cursorBrush = SolidColor(OrangePrimary),
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = inputTextStyle.copy(color = LabelColor.copy(alpha = 0.65f))
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Top
            ) {
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    interactionSource = interactionSource,
                    textStyle = inputTextStyle,
                    cursorBrush = SolidColor(OrangePrimary),
                    maxLines = Int.MAX_VALUE,
                    decorationBox = { innerTextField ->
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                            if (value.isEmpty()) {
                                Text(
                                    text = placeholder,
                                    style = inputTextStyle.copy(color = LabelColor.copy(alpha = 0.65f))
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

