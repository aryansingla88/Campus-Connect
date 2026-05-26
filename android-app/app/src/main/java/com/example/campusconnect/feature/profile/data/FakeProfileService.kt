package com.example.campusconnect.feature.profile.data

import com.example.campusconnect.feature.profile.model.PublicUserProfile

object FakeProfileService {

    fun getMyProfile(): PublicUserProfile {

        return PublicUserProfile(
            userId = "4",
            fullName = "Aryan Singla",
            username = "@aryan.singla",
            bio = "Tech enthusiast, problem solver and always up for new ideas.",
            initials = "AS",
            course = "Masters of Computer Application",
            year = "2nd Year (2024 – 2028)",
            hostel = "H6",
            hometown = "Karnal",
            gender = "Male",
            dob = "2004-08-17",
            phone = "+91 91234 56789",
            email = "525110036@nitkkr.ac.in",
            memberSince = "August 2024",
            github = "github.com/aryan",
            linkedin = "linkedin.com/in/aryan",
            instagram = "@aryan.singla",
            connectionCount = 24,
            honorRank = 12,
            clubCount = 6,
            interestCount = 4,
        )
    }

    fun getProfile(userId: String): PublicUserProfile {

        return PublicUserProfile(
            userId = "1",
            fullName = "Rahul Kumar",
            username = "@rahul.kumar",
            bio = "Tech enthusiast, problem solver and always up for new ideas.",
            initials = "RK",
            course = "Masters of Computer Application",
            year = "2nd Year (2024 – 2028)",
            hostel = "H4",
            hometown = "Delhi",
            gender = "Male",
            dob = "2005-08-17",
            phone = "+91 91234 56789",
            email = "525110036@nitkkr.ac.in",
            memberSince = "August 2024",
            github = "github.com/rahul",
            linkedin = "linkedin.com/in/rahul",
            instagram = "@rahul.kumar",
            connectionCount = 18,
            honorRank = 3,
            clubCount = 2,
            interestCount = 3,
        )
    }
}