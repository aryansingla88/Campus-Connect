package com.example.campusconnect.feature.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.campusconnect.model.Event

// ─── Theme ────────────────────────────────────────────────────────────────────
private val OrangePrimary  = Color(0xFFFF6F00)
private val OrangeLight    = Color(0xFFFFF3E0)
private val CardBg         = Color(0xFFFAFAFA)
private val TextPrimary    = Color(0xFF1A1A1A)
private val TextMuted      = Color(0xFF9E9E9E)
private val DividerColor   = Color(0xFFF0F0F0)
private val RemoveRed      = Color(0xFFD32F2F)
private val RemoveRedLight = Color(0xFFFFEBEE)

// ─── UserAccess model ─────────────────────────────────────────────────────────

data class UserAccess(
    val id       : Int,
    val name     : String,
    val subtitle : String,
    val initials : String = name
        .split(" ")
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
)

// ─── Fake data — self-contained, no external service needed ──────────────────

private val ALL_USERS = listOf(
    UserAccess(id = 1,  name = "Rahul Kumar",     subtitle = "MCA 2nd Year"),
    UserAccess(id = 2,  name = "Priya Sharma",    subtitle = "MCA 3rd Year"),
    UserAccess(id = 3,  name = "Amit Singh",      subtitle = "BCA 1st Year"),
    UserAccess(id = 4,  name = "Sneha Patel",     subtitle = "MCA 1st Year"),
    UserAccess(id = 5,  name = "Rohan Verma",     subtitle = "BCA 3rd Year"),
    UserAccess(id = 6,  name = "Pooja Nair",      subtitle = "BCA 2nd Year"),
    UserAccess(id = 7,  name = "Karan Mehta",     subtitle = "MCA 2nd Year"),
    UserAccess(id = 8,  name = "Divya Reddy",     subtitle = "MCA 3rd Year"),
    UserAccess(id = 9,  name = "Nikhil Joshi",    subtitle = "BCA 1st Year"),
    UserAccess(id = 10, name = "Ananya Gupta",    subtitle = "MCA 1st Year"),
    UserAccess(id = 11, name = "Vikram Tiwari",   subtitle = "BCA 2nd Year"),
    UserAccess(id = 12, name = "Ishaan Malhotra", subtitle = "MCA 2nd Year"),
    UserAccess(id = 13, name = "Neha Kapoor",     subtitle = "BCA 3rd Year"),
    UserAccess(id = 14, name = "Arjun Yadav",     subtitle = "MCA 1st Year"),
    UserAccess(id = 15, name = "Simran Kaur",     subtitle = "BCA 1st Year"),
)

/** Default seed: first two people always have access when the dialog opens. */
private val DEFAULT_ACCESS = ALL_USERS.take(2)

private fun searchUsers(query: String): List<UserAccess> {
    if (query.isBlank()) return emptyList()
    return ALL_USERS.filter { it.name.contains(query, ignoreCase = true) }
}

// ─── EventAccessDialog ────────────────────────────────────────────────────────

@Composable
fun EventAccessDialog(
    event     : Event,
    onDismiss : () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val addedUsers = remember {
        mutableStateListOf<UserAccess>().apply { addAll(DEFAULT_ACCESS) }
    }

    val searchResults by remember(searchQuery) {
        derivedStateOf {
            searchUsers(searchQuery)
                .filter { result -> addedUsers.none { it.id == result.id } }
        }
    }

    var pendingRemove by remember { mutableStateOf<UserAccess?>(null) }

    // ── Remove confirmation ───────────────────────────────────────────────────
    pendingRemove?.let { user ->
        AlertDialog(
            onDismissRequest = { pendingRemove = null },
            shape            = RoundedCornerShape(20.dp),
            containerColor   = Color.White,
            title = {
                Text(
                    "Remove Access?",
                    fontWeight = FontWeight.Bold,
                    fontSize   = 17.sp,
                    color      = TextPrimary
                )
            },
            text = {
                Text(
                    "Remove ${user.name} from \"${event.title}\"?",
                    fontSize   = 14.sp,
                    color      = TextMuted,
                    lineHeight = 20.sp
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { pendingRemove = null },
                    shape   = RoundedCornerShape(12.dp),
                    border  = ButtonDefaults.outlinedButtonBorder.copy(width = 1.5.dp),
                    colors  = ButtonDefaults.outlinedButtonColors(contentColor = OrangePrimary)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        addedUsers.remove(user)
                        pendingRemove = null
                    },
                    shape  = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RemoveRed)
                ) {
                    Text("Remove", color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    // ── Main dialog ───────────────────────────────────────────────────────────
    Dialog(
        onDismissRequest = onDismiss,
        properties       = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier         = Modifier
                .fillMaxSize()
                .background(Color(0x99000000)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier  = Modifier
                    .fillMaxWidth(0.88f)        // ← narrower than before (was 0.92f)
                    .fillMaxHeight(0.72f),       // ← shorter than before (was 0.82f)
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {

                    // ── Header ────────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Access & Permissions",
                                fontSize   = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary
                            )
                            // ── Event name — prominent, orange, not tiny ──────
                            Text(
                                text       = event.title,
                                fontSize   = 14.sp,          // was 12.sp — now clearly readable
                                fontWeight = FontWeight.SemiBold,
                                color      = OrangePrimary,  // orange so it stands out
                                maxLines   = 1,
                                overflow   = TextOverflow.Ellipsis,
                                modifier   = Modifier.padding(top = 2.dp)
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint               = TextMuted
                            )
                        }
                    }

                    HorizontalDivider(color = DividerColor)

                    // ── Search bar ────────────────────────────────────────────
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                        AccessSearchBar(
                            value         = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder   = "Search people to connect..."
                        )
                    }

                    // ── List ──────────────────────────────────────────────────
                    LazyColumn(
                        modifier            = Modifier.weight(1f),
                        contentPadding      = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (searchQuery.isNotBlank()) {
                            // ── Search results ────────────────────────────────
                            if (searchResults.isEmpty()) {
                                item {
                                    Box(
                                        modifier         = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No users found for \"$searchQuery\"",
                                            fontSize = 13.sp,
                                            color    = TextMuted
                                        )
                                    }
                                }
                            } else {
                                items(searchResults, key = { it.id }) { user ->
                                    AccessUserCard(user = user) {
                                        Button(
                                            onClick = {
                                                addedUsers.add(user)
                                                searchQuery = ""
                                            },
                                            shape          = RoundedCornerShape(10.dp),
                                            colors         = ButtonDefaults.buttonColors(
                                                containerColor = OrangePrimary
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 14.dp,
                                                vertical   = 0.dp
                                            ),
                                            modifier       = Modifier.height(34.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Add,
                                                null,
                                                tint     = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                "Add",
                                                color      = Color.White,
                                                fontSize   = 12.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            // ── Added users list ──────────────────────────────
                            if (addedUsers.isEmpty()) {
                                item {
                                    Box(
                                        modifier         = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 28.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No one has access yet.\nSearch above to add people.",
                                            fontSize   = 13.sp,
                                            color      = TextMuted,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            } else {
                                item {
                                    Text(
                                        "People with access  ·  ${addedUsers.size}",
                                        fontSize   = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color      = TextMuted,
                                        modifier   = Modifier.padding(bottom = 2.dp)
                                    )
                                }
                                items(addedUsers, key = { it.id }) { user ->
                                    AccessUserCard(user = user) {
                                        // ── Light-red Remove button ───────────
                                        OutlinedButton(
                                            onClick        = { pendingRemove = user },
                                            shape          = RoundedCornerShape(10.dp),
                                            border         = ButtonDefaults.outlinedButtonBorder.copy(
                                                width = 1.dp
                                            ),
                                            colors         = ButtonDefaults.outlinedButtonColors(
                                                containerColor = RemoveRedLight,   // light red bg
                                                contentColor   = RemoveRed         // red text
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 14.dp,
                                                vertical   = 0.dp
                                            ),
                                            modifier       = Modifier.height(34.dp)
                                        ) {
                                            Text(
                                                "Remove",
                                                fontSize   = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color      = RemoveRed
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ── Footer Done button ────────────────────────────────────
                    HorizontalDivider(color = DividerColor)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Button(
                            onClick  = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            shape  = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                        ) {
                            Text(
                                "Done",
                                color      = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── AccessSearchBar — no + button, just search icon + clear ─────────────────

@Composable
private fun AccessSearchBar(
    value         : String,
    onValueChange : (String) -> Unit,
    placeholder   : String
) {
    val focusRequester = remember { FocusRequester() }

    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { focusRequester.requestFocus() }
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Search,
                null,
                tint     = TextMuted,
                modifier = Modifier.size(18.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                androidx.compose.foundation.text.BasicTextField(
                    value         = value,
                    onValueChange = onValueChange,
                    modifier      = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    singleLine    = true,
                    textStyle     = TextStyle(fontSize = 13.sp, color = TextPrimary),
                    decorationBox = { inner ->
                        if (value.isEmpty()) {
                            Text(placeholder, fontSize = 13.sp, color = TextMuted)
                        }
                        inner()
                    }
                )
            }

            // Show × only when there is text to clear — no + button
            if (value.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF0F0F0))
                        .clickable { onValueChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Clear",
                        tint               = TextMuted,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ─── AccessUserCard ───────────────────────────────────────────────────────────

@Composable
private fun AccessUserCard(
    user            : UserAccess,
    trailingContent : @Composable () -> Unit
) {
    Card(
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardBg),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier         = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(OrangeLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = user.initials,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = OrangePrimary
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = user.name,
                    fontSize   = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis
                )
                Text(
                    text     = user.subtitle,
                    fontSize = 11.sp,
                    color    = TextMuted
                )
            }

            trailingContent()
        }
    }
}