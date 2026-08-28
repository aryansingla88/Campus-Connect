package com.example.campusconnect.feature.events.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ModeToggle(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(26.dp))
            .background(
                if (selected)
                    Color(0xFFFF4D00)
                else
                    Color.Transparent
            )
            .clickable { onClick() }
            .padding(
                horizontal = 18.dp,
                vertical = 10.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier.size(21.dp),
            contentAlignment = Alignment.Center
        ) {

            // Small black shadow behind selected white icon
            if (selected) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.Black.copy(alpha = 0.30f),
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 1.dp, y = 1.dp)
                )
            }

            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = if (selected)
                    Color.White
                else
                    Color.Black,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(7.dp))

        Text(
            text = text,
            color = if (selected)
                Color.White
            else
                Color.Black,
            fontSize = 16.sp
        )
    }
}