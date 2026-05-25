package com.example.campusconnect.feature.profile.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusconnect.feature.profile.model.StatPanel

@Composable
fun ProfileBottomBar(
    activePanel: StatPanel?,
    onEditProfile: () -> Unit,
    onRequestsClick: () -> Unit,
    onManageCollectionClick: () -> Unit
) {
    val config: Triple<String, ImageVector, () -> Unit>? = when (activePanel) {
        null                  -> Triple("Edit Profile",      Icons.Outlined.Edit,             onEditProfile)
        StatPanel.CONNECTIONS -> Triple("Requests",          Icons.Outlined.PersonAdd,        onRequestsClick)
        StatPanel.HONOR       -> Triple("Manage Collection", Icons.Outlined.WorkspacePremium, onManageCollectionClick)
        StatPanel.CLUBS,
        StatPanel.INTERESTS   -> null
    }

    AnimatedVisibility(
        visible = config != null,
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut()
    ) {
        config?.let { (text, icon, click) ->
            Surface(color = PageBg, shadowElevation = 8.dp, tonalElevation = 0.dp) {
                Box(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Button(
                        onClick  = click,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}