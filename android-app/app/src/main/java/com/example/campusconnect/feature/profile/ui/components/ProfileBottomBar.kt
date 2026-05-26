package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BottomBarButton(
    val text: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

@Composable
fun ProfileBottomBar(
    buttons: List<BottomBarButton>,
    visible: Boolean = true
) {
    AnimatedVisibility(
        visible = visible && buttons.isNotEmpty(),
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut()
    ) {
        Surface(
            color = PageBg,
            shadowElevation = 8.dp,
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                buttons.forEach { button ->

                    Button(
                        onClick = button.onClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Orange
                        )
                    ) {
                        Icon(
                            button.icon,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = button.text,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}