package com.example.campusconnect.feature.events.data.fake

import com.example.campusconnect.feature.events.model.UserAccess

object FakeUserAccessService {

    val allUsers = listOf(
        UserAccess(id = 1,  name = "Rahul Kumar",     subtitle = "MCA 2nd Year"),
        UserAccess(id = 2,  name = "Priya Sharma",    subtitle = "MCA 3rd Year"),
        UserAccess(id = 3,  name = "Amit Singh",      subtitle = "BCA 1st Year"),
        UserAccess(id = 4,  name = "Sneha Patel",     subtitle = "MCA 1st Year"),
        UserAccess(id = 5,  name = "Rohan Verma",     subtitle = "BCA 3rd Year"),
        UserAccess(id = 6,  name = "Pooja Nair",      subtitle = "BCA 2nd Year"),
        UserAccess(id = 7,  name = "Karan Mehta",     subtitle = "MCA 2nd Year"),
        UserAccess(id = 8,  name = "Divya Reddy",     subtitle = "MCA 3rd Year"),
        UserAccess(id = 9,  name = "Nikhil Joshi",    subtitle = "BCA 1st Year"),
        UserAccess(id = 10, name = "Ananya Gupta",    subtitle = "MCA 1st Year"),
        UserAccess(id = 11, name = "Vikram Tiwari",   subtitle = "BCA 2nd Year"),
        UserAccess(id = 12, name = "Ishaan Malhotra", subtitle = "MCA 2nd Year"),
        UserAccess(id = 13, name = "Neha Kapoor",     subtitle = "BCA 3rd Year"),
        UserAccess(id = 14, name = "Arjun Yadav",     subtitle = "MCA 1st Year"),
        UserAccess(id = 15, name = "Simran Kaur",     subtitle = "BCA 1st Year")
    )

    /** Default users pre-seeded with access when dialog opens. */
    val defaultAccess = allUsers.take(2)

    fun search(query: String): List<UserAccess> {
        if (query.isBlank()) return emptyList()
        return allUsers.filter { it.name.contains(query, ignoreCase = true) }
    }
}