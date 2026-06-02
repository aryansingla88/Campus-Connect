package com.example.campusconnect.feature.profile.data.fake

import androidx.compose.ui.graphics.Color
import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionStatus
import com.example.campusconnect.feature.profile.ui.components.OrangeDark

object FakeConnectionsService {

    fun getConnections(): List<Connection> {

        return listOf(

            Connection(
                userId   = "1",
                initials = "RK",
                name = "Rahul Kumar",
                sub = "MCA 2nd Year",
                avatarBg = Color(0xFFFEF0E6),
                avatarText = OrangeDark,
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId   = "2",
                initials = "PS",
                name = "Priya Sharma",
                sub = "MCA 3rd Year",
                avatarBg = Color(0xFFE6F1FB),
                avatarText = Color(0xFF0C447C),
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId   = "3",
                initials = "AV",
                name = "Amit Verma",
                sub = "MCA 2nd Year",
                avatarBg = Color(0xFFE1F5EE),
                avatarText = Color(0xFF085041),
                status = ConnectionStatus.PENDING
            )
        )
    }
}