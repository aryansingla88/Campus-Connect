package com.example.campusconnect.feature.profile.data.fake

import com.example.campusconnect.feature.profile.model.PublicUserProfile

object FakeProfileService {

    fun getMyProfile(): PublicUserProfile {

        return PublicUserProfile(
            userId = "4",
            fullName = "Aryan Singla",
            username = "aryan.singla",
            bio = "Tech enthusiast, problem solver and always up for new ideas.",
            avatarUrl = null,

            course = "MCA",
            branch = "Computer Applications",
            academicYear = "2nd Year",
            batch = "2024-2026",
            hostel = "H6",
            hometown = "Karnal",

            gender = "Male",
            dob = "2004-08-17",

            phone = "+91 91234 56789",
            email = "525110036@nitkkr.ac.in",

            github = "github.com/aryan",
            linkedin = "linkedin.com/in/aryan",
            instagram = "@aryan.singla",

            memberSince = "August 2024",

            showPhone = true,
            showSocials = true
        )
    }

    fun getProfile(userId: String): PublicUserProfile {

        return PublicUserProfile(
            userId = userId,
            fullName = "Rahul Kumar",
            username = "rahul.kumar",
            bio = "Tech enthusiast, problem solver and always up for new ideas.",
            avatarUrl = null,

            course = "MCA",
            branch = "Computer Applications",
            academicYear = "2nd Year",
            batch = "2024-2026",
            hostel = "H4",
            hometown = "Delhi",

            gender = "Male",
            dob = "2005-08-17",

            phone = "+91 91234 56789",
            email = "rahul@nitkkr.ac.in",

            github = "github.com/rahul",
            linkedin = "linkedin.com/in/rahul",
            instagram = "@rahul.kumar",

            memberSince = "August 2024",

            showPhone = false,
            showSocials = true
        )
    }
}