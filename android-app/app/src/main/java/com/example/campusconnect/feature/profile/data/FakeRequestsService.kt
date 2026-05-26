package com.example.campusconnect.feature.profile.data

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.ui.components.OrangeDark

object FakeRequestsService {

    fun getIncomingRequests(): List<Connection> {
        return listOf(

            Connection(
                userId = "11",
                initials = "AS",
                name = "Aarav Sharma",
                sub = "MCA 1st Year",
                avatarBg = Color(0xFFFEF0E6),
                avatarText = OrangeDark,
                status = ConnectionStatus.PENDING
            ),

            Connection(
                userId = "12",
                initials = "NK",
                name = "Neha Kapoor",
                sub = "BCA 3rd Year",
                avatarBg = Color(0xFFE6F1FB),
                avatarText = Color(0xFF0C447C),
                status = ConnectionStatus.PENDING
            )
        )
    }

    fun getSentInvites(): List<Connection> {
        return listOf(

            Connection(
                userId = "21",
                initials = "RM",
                name = "Rohan Mehta",
                sub = "MCA 2nd Year",
                avatarBg = Color(0xFFE1F5EE),
                avatarText = Color(0xFF085041),
                status = ConnectionStatus.PENDING
            ),

            Connection(
                userId = "22",
                initials = "PS",
                name = "Priya Singh",
                sub = "B.Tech CSE 2nd Year",
                avatarBg = Color(0xFFFFF4D9),
                avatarText = Color(0xFF8C6A00),
                status = ConnectionStatus.PENDING
            )
        )
    }
}