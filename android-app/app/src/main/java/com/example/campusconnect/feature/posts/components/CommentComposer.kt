package com.example.campusconnect.feature.posts.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.campusconnect.core.ui.theme.ErrorRed
import com.example.campusconnect.core.ui.theme.HintColor
import com.example.campusconnect.core.ui.theme.LabelColor
import com.example.campusconnect.core.ui.theme.OrangeBorder
import com.example.campusconnect.core.ui.theme.OrangePrimary
import com.example.campusconnect.core.ui.theme.White
import com.example.campusconnect.feature.posts.models.Comment
import kotlinx.coroutines.launch

@Composable
fun CommentComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    replyTarget: Comment?,
    onCancelReply: () -> Unit,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSend: () -> Unit
) {
    val buttonScale = remember { Animatable(1f) }
    val animationScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(OrangeBorder)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 6.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            replyTarget?.let { target ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Replying to ${target.username}",
                        color = LabelColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Cancel",
                        color = OrangePrimary,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier
                            .clickable(enabled = !isSubmitting, onClick = onCancelReply)
                            .padding(start = 8.dp, top = 2.dp, bottom = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = draft,
                    onValueChange = onDraftChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 40.dp, max = 96.dp)
                        .background(White, RoundedCornerShape(22.dp))
                        .border(1.dp, OrangeBorder, RoundedCornerShape(22.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    enabled = !isSubmitting,
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = LabelColor),
                    cursorBrush = SolidColor(OrangePrimary),
                    decorationBox = { innerTextField ->
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (draft.isEmpty()) {
                                Text(
                                    text = "Write a comment…",
                                    color = HintColor,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        animationScope.launch {
                            buttonScale.animateTo(
                                targetValue = 1.12f,
                                animationSpec = tween(durationMillis = 80)
                            )
                            buttonScale.animateTo(
                                targetValue = 1f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = 650f
                                )
                            )
                        }
                        onSend()
                    },
                    enabled = !isSubmitting && draft.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .graphicsLayer {
                            scaleX = buttonScale.value
                            scaleY = buttonScale.value
                        }
                        .background(
                            color = OrangePrimary,
                            shape = CircleShape
                        )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Send,
                            contentDescription = "Send comment",
                            modifier = Modifier.size(20.dp),
                            tint = White
                        )
                    }
                }
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = ErrorRed,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
