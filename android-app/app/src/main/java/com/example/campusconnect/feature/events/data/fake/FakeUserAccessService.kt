package com.example.campusconnect.feature.events.data.fake

import com.example.campusconnect.feature.events.model.UserAccess

object FakeUserAccessService {

    val allUsers = listOf(
        UserAccess(id = 1,  name = "Rahul Kumar",     courseId = 1, admissionYear = 2024),
        UserAccess(id = 2,  name = "Priya Sharma",    courseId = 1, admissionYear = 2023),
        UserAccess(id = 3,  name = "Amit Singh",      courseId = 2, admissionYear = 2025),
        UserAccess(id = 4,  name = "Sneha Patel",     courseId = 1, admissionYear = 2025),
        UserAccess(id = 5,  name = "Rohan Verma",     courseId = 2, admissionYear = 2023),
        UserAccess(id = 6,  name = "Pooja Nair",      courseId = 2, admissionYear = 2024),
        UserAccess(id = 7,  name = "Karan Mehta",     courseId = 1, admissionYear = 2024),
        UserAccess(id = 8,  name = "Divya Reddy",     courseId = 1, admissionYear = 2023),
        UserAccess(id = 9,  name = "Nikhil Joshi",    courseId = 2, admissionYear = 2025),
        UserAccess(id = 10, name = "Ananya Gupta",    courseId = 1, admissionYear = 2025),
        UserAccess(id = 11, name = "Vikram Tiwari",   courseId = 2, admissionYear = 2024),
        UserAccess(id = 12, name = "Ishaan Malhotra", courseId = 1, admissionYear = 2024),
        UserAccess(id = 13, name = "Neha Kapoor",     courseId = 2, admissionYear = 2023),
        UserAccess(id = 14, name = "Arjun Yadav",     courseId = 1, admissionYear = 2025),
        UserAccess(id = 15, name = "Simran Kaur",     courseId = 2, admissionYear = 2025)
    )

    /** Default users pre-seeded with access when dialog opens. */
    val defaultAccess = allUsers.take(2)

    fun search(query: String): List<UserAccess> {
        if (query.isBlank()) return emptyList()

        return allUsers.filter {
            it.name.contains(query, ignoreCase = true)
        }
    }
}