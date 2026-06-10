package com.example.campusconnect.feature.profile.data.fake

import com.example.campusconnect.feature.profile.model.Connection
import com.example.campusconnect.feature.profile.model.ConnectionStatus

object FakeConnectionsService {

    fun getConnections(): List<Connection> {

        return listOf(

            Connection(
                userId = "1",
                fullName = "Rahul Kumar",
                course = "MCA",
                academicYear = 2,
                avatarUrl = null,
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId = "2",
                fullName = "Priya Sharma",
                course = "MCA",
                academicYear = 3,
                avatarUrl = null,
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId = "3",
                fullName = "Amit Verma",
                course = "MCA",
                academicYear = 2,
                avatarUrl = null,
                status = ConnectionStatus.PENDING
            ),

            Connection(
                userId = "4",
                fullName = "Neha Gupta",
                course = "MCA",
                academicYear = 1,
                avatarUrl = null,
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId = "5",
                fullName = "Arjun Singh",
                course = "BCA",
                academicYear = 3,
                avatarUrl = null,
                status = ConnectionStatus.CONNECTED
            ),

            Connection(
                userId = "6",
                fullName = "Sneha Mehta",
                course = "B.Tech CSE",
                academicYear = 2,
                avatarUrl = null,
                status = ConnectionStatus.PENDING
            )
        )
    }
}