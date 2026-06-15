package com.example.campusconnect.feature.profile.data.fake

import com.example.campusconnect.feature.profile.model.ConnectionRequest
import com.example.campusconnect.feature.profile.model.RequestType

object FakeRequestsService {

    fun getIncomingRequests(): List<ConnectionRequest> {

        return listOf(

            ConnectionRequest(
                userId = "11",
                fullName = "Aarav Sharma",
                course = "MCA",
                academicYear = 1,
                avatarUrl = null,
                type = RequestType.INCOMING
            ),

            ConnectionRequest(
                userId = "12",
                fullName = "Neha Kapoor",
                course = "BCA",
                academicYear = 3,
                avatarUrl = null,
                type = RequestType.INCOMING
            )
        )
    }

    fun getSentInvites(): List<ConnectionRequest> {

        return listOf(

            ConnectionRequest(
                userId = "21",
                fullName = "Rohan Mehta",
                course = "MCA",
                academicYear = 2,
                avatarUrl = null,
                type = RequestType.OUTGOING
            ),

            ConnectionRequest(
                userId = "22",
                fullName = "Priya Singh",
                course = "B.Tech CSE",
                academicYear = 2,
                avatarUrl = null,
                type = RequestType.OUTGOING
            )
        )
    }
}