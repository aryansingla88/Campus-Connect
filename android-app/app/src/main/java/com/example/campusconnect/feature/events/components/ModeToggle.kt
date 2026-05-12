package com.example.campusconnect.feature.events.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ModeToggle(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                if (selected)
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFA726), Color(0xFFFF6F00))
                    )
                else
                    Brush.verticalGradient(
                        listOf(Color(0xFFFFF3E0), Color(0xFFFFF3E0)) // light orange
                    )
            )
            .clickable { onClick() }
            .padding(horizontal = 18.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (selected) Color.White else Color(0xFFFF6F00),
            modifier = Modifier.size(18.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFF2A2A2A),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}