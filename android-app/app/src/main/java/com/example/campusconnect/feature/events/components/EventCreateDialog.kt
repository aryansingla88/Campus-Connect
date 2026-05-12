package com.example.campusconnect.feature.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.campusconnect.feature.events.EventUiState

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun EventCreateDialog(

    state: EventUiState,

    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,

    onDateChange: (String) -> Unit,
    onVenueChange: (String) -> Unit,

    onStartTimeChange: (String) -> Unit,
    onEndTimeChange: (String) -> Unit,

    onPosterToggle: (Boolean) -> Unit,
    onPosterUrlChange: (String) -> Unit,

    onDismiss: () -> Unit,
    onCreate: () -> Unit,

    onClubNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,

    onVisibilityTypeChange: (String) -> Unit,
    onVisibilityValueChange: (String) -> Unit,

    onRegistrationToggle: (Boolean) -> Unit,
    onRegistrationLinkChange: (String) -> Unit,
    onInAppRegistrationToggle: () -> Unit,

    onEnableChatToggle: (Boolean) -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x99000000)),
        contentAlignment = Alignment.Center
    ) {

        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.90f),

            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

            // 🔶 FIXED HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 20.dp,
                        end = 20.dp,
                        top = 20.dp
                    ),

                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFFF6F00)
                )

                Text(
                    text = "Create Event",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF2A2A2A)
                )
            }

                Box(
                    modifier = Modifier
                        .weight(1f, fill = true)
                ) {

                    // 🔶 SCROLLABLE CONTENT
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(20.dp),

                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // 🔶 TITLE
                        OutlinedTextField(
                            value = state.title,
                            onValueChange = onTitleChange,
                            label = {
                                Text("Title *")
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 🔶 DESCRIPTION
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = onDescriptionChange,
                            label = {
                                Text("Description *")
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                        )

                        // 🔶 POSTER SECTION
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            Text(
                                text = "Is Poster?",
                                fontWeight = FontWeight.Medium
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                // YES
                                Button(
                                    onClick = {
                                        onPosterToggle(true)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (state.isPoster)
                                                Color(0xFFFF6F00)
                                            else
                                                Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        "Yes",
                                        color =
                                            if (state.isPoster)
                                                Color.White
                                            else
                                                Color.Black
                                    )
                                }

                                // NO
                                Button(
                                    onClick = {
                                        onPosterToggle(false)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (!state.isPoster)
                                                Color(0xFFFF6F00)
                                            else
                                                Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        "No",
                                        color =
                                            if (!state.isPoster)
                                                Color.White
                                            else
                                                Color.Black
                                    )
                                }
                            }

                            // 🔶 POSTER URL
                            if (state.isPoster) {

                                OutlinedTextField(
                                    value = state.posterUrl,
                                    onValueChange = onPosterUrlChange,
                                    label = {
                                        Text("Poster Source")
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        // 🔶 LOCATION CARD
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFFFFF8F2),
                                    RoundedCornerShape(18.dp)
                                )
                                .border(
                                    1.dp,
                                    Color(0xFFFFE0B2),
                                    RoundedCornerShape(18.dp)
                                )
                                .padding(16.dp),

                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6F00)
                                )

                                Text(
                                    text = "Selected Location",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Text(
                                text =
                                    "X: ${
                                        state.selectedRatio?.first?.toString()
                                            ?: "0.0"
                                    }",
                                color = Color.Gray
                            )

                            Text(
                                text =
                                    "Y: ${
                                        state.selectedRatio?.second?.toString()
                                            ?: "0.0"
                                    }",
                                color = Color.Gray
                            )
                        }

                        // 🔶 DATE + VENUE
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = state.date,
                                onValueChange = onDateChange,
                                label = { Text("Date *") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.venue,
                                onValueChange = onVenueChange,
                                label = { Text("Venue *") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

// 🔶 START + END TIME
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = state.startTime,
                                onValueChange = onStartTimeChange,
                                label = { Text("Start Time *") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.endTime,
                                onValueChange = onEndTimeChange,
                                label = { Text("End Time") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

// 🔶 CLUB + CATEGORY
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = state.clubName,
                                onValueChange = onClubNameChange,
                                label = { Text("Club Name") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.category,
                                onValueChange = onCategoryChange,
                                label = { Text("Category") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

// 🔶 VISIBILITY
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {

                            OutlinedTextField(
                                value = state.visibilityType,
                                onValueChange = onVisibilityTypeChange,
                                label = { Text("Visibility") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = state.visibilityValue,
                                onValueChange = onVisibilityValueChange,
                                label = { Text("Value") },
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

// 🔶 REGISTRATION REQUIRED
                        Column {

                            Text(
                                text = "Registration Required?",
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                Button(
                                    onClick = {
                                        onRegistrationToggle(true)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (state.registrationRequired)
                                                Color(0xFFFF6F00)
                                            else
                                                Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        "Yes",
                                        color =
                                            if (state.registrationRequired)
                                                Color.White
                                            else
                                                Color.Black
                                    )
                                }

                                Button(
                                    onClick = {
                                        onRegistrationToggle(false)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor =
                                            if (!state.registrationRequired)
                                                Color(0xFFFF6F00)
                                            else
                                                Color(0xFFFFF3E0)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text(
                                        "No",
                                        color =
                                            if (!state.registrationRequired)
                                                Color.White
                                            else
                                                Color.Black
                                    )
                                }
                            }
                        }

// 🔶 REGISTRATION SECTION
                        if (state.registrationRequired) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {

                                OutlinedButton(
                                    onClick = onInAppRegistrationToggle,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("+")
                                }

                                OutlinedTextField(
                                    value = state.registrationLink,
                                    onValueChange = onRegistrationLinkChange,
                                    label = { Text("Registration Link") },
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Column {

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Enable Chat?",
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {

                                    Button(
                                        onClick = {
                                            onEnableChatToggle(true)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (state.enableChat)
                                                    Color(0xFFFF6F00)
                                                else
                                                    Color(0xFFFFF3E0)
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            "Yes",
                                            color =
                                                if (state.enableChat)
                                                    Color.White
                                                else
                                                    Color.Black
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            onEnableChatToggle(false)
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor =
                                                if (!state.enableChat)
                                                    Color(0xFFFF6F00)
                                                else
                                                    Color(0xFFFFF3E0)
                                        ),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Text(
                                            "No",
                                            color =
                                                if (!state.enableChat)
                                                    Color.White
                                                else
                                                    Color.Black
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

            // 🔶 FIXED BOTTOM BUTTONS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),

                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = onCreate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF6F00)
                    )
                ) {
                    Text("Create Event")
                }
            }
        }
    }
}
}