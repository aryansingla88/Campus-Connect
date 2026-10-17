package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.RegistrationResponseItem
import com.example.campusconnect.feature.registrations.model.RegistrationResponseStatus
import com.example.campusconnect.feature.registrations.model.RegistrationResponseType

object FakeRegistrationResponses {

    val responses = listOf(
        RegistrationResponseItem(1, "Alex Johnson", "alex.johnson@nitkkr.ac.in", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.CONFIRMED, "Mar 15, 2025 10:24 AM"),
        RegistrationResponseItem(2, "CodeCrafters", "teamcodecrafters@gmail.com", RegistrationResponseType.TEAM, RegistrationResponseStatus.CONFIRMED, "Mar 14, 2025 6:12 PM", 4),
        RegistrationResponseItem(3, "Priya Sharma", "priya.sharma@nitkkr.ac.in", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.PENDING, "Mar 14, 2025 2:45 PM"),
        RegistrationResponseItem(4, "Team Innovators", "innovators.3@gmail.com", RegistrationResponseType.TEAM, RegistrationResponseStatus.CONFIRMED, "Mar 13, 2025 11:20 AM", 3),
        RegistrationResponseItem(5, "Rahul Mehta", "rahul.mehta@nitkkr.ac.in", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.CANCELLED, "Mar 12, 2025 4:32 PM"),
        RegistrationResponseItem(6, "ByteBrigade", "bytebrigade@gmail.com", RegistrationResponseType.TEAM, RegistrationResponseStatus.PENDING, "Mar 12, 2025 11:08 AM", 5),
        RegistrationResponseItem(7, "Ananya Gupta", "ananya.gupta@nitkkr.ac.in", RegistrationResponseType.INDIVIDUAL, RegistrationResponseStatus.CONFIRMED, "Mar 11, 2025 3:16 PM"),
        RegistrationResponseItem(8, "Tech Titans", "techtitans@gmail.com", RegistrationResponseType.TEAM, RegistrationResponseStatus.CANCELLED, "Mar 10, 2025 9:40 AM", 4),
    )
}
