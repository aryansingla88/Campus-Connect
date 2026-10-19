package com.example.campusconnect.feature.registrations.data

import com.example.campusconnect.feature.registrations.model.QuickFieldValue
import com.example.campusconnect.feature.registrations.model.RegistrationDetailResponse
import com.example.campusconnect.feature.registrations.model.RegistrationMemberDetail
import com.example.campusconnect.feature.registrations.model.ResponseAnswer
import kotlinx.coroutines.delay

/** Fake data keyed by the ids in [FakeRegistrationResponses]. */
class FakeRegistrationResponseRepository : RegistrationResponseRepository {

    override suspend fun getDetails(registrationId: Int): Result<RegistrationDetailResponse> {
        delay(400)
        val detail = details[registrationId]
            ?: return Result.failure(IllegalStateException("Couldn't load this registration's responses."))
        return Result.success(detail)
    }

    private companion object {
        const val LONG_EXPECTATION =
            "To gain hands-on experience and build real-world projects using Android. " +
                    "I also want to learn best practices and contribute to open source projects."

        fun shared(
            why: String,
            domain: String,
            experience: String,
            comments: String? = null,
        ): List<ResponseAnswer> = listOfNotNull(
            ResponseAnswer(1, "Why do you want to participate?", "TEXT", why),
            ResponseAnswer(2, "Domain of interest", "SELECT", domain),
            ResponseAnswer(3, "Any prior experience?", "TEXT", experience),
            comments?.let { ResponseAnswer(4, "Additional comments", "TEXT", it) },
        )

        fun member(
            userId: Int,
            fullName: String,
            laptop: Boolean,
            expectation: String,
            extra: String? = "Excited to be a part of this workshop!",
        ): RegistrationMemberDetail {
            val handle = fullName.lowercase().replace(' ', '.')
            val roll = "25MCA" + userId.toString().takeLast(3).padStart(3, '0')
            return RegistrationMemberDetail(
                userId = userId,
                quickFields = listOf(
                    QuickFieldValue("Full Name", fullName),
                    QuickFieldValue("Email", "$handle@nitkkr.ac.in"),
                    QuickFieldValue("Phone", "+91 98765 4${userId.toString().takeLast(4).padStart(4, '0')}"),
                    QuickFieldValue("Course", "MCA"),
                    QuickFieldValue("Year / Batch", "2025"),
                    QuickFieldValue("Roll Number", roll),
                ),
                individualAnswers = listOfNotNull(
                    ResponseAnswer(11, "Do you have a laptop for development?", "SELECT", if (laptop) "Yes" else "No"),
                    ResponseAnswer(12, "What are your expectations from this workshop?", "TEXT", expectation),
                    extra?.let { ResponseAnswer(13, "Anything else you would like to share?", "TEXT", it) },
                ),
            )
        }

        val defaultShared = shared(
            why = "I want to learn Android development and build useful projects.",
            domain = "Android Development",
            experience = "Built a couple of small apps for college projects.",
        )

        val details: Map<Int, RegistrationDetailResponse> = mapOf(
            1 to RegistrationDetailResponse(
                defaultShared,
                listOf(member(1001, "Alex Johnson", true, LONG_EXPECTATION)),
            ),
            2 to RegistrationDetailResponse(
                shared(
                    "We want to ship a polished app together during the workshop.",
                    "Android Development, UI/UX",
                    "Two of us have published apps on the Play Store.",
                ),
                listOf(
                    member(2001, "Riya Malhotra", true, LONG_EXPECTATION),
                    member(2002, "Dev Patel", true, "Learn Jetpack Compose properly."),
                    member(2003, "Isha Rao", false, "Understand app architecture."),
                    member(2004, "Kabir Joshi", true, "Meet other developers.", extra = null),
                ),
            ),
            3 to RegistrationDetailResponse(
                defaultShared,
                listOf(member(3001, "Priya Sharma", true, "Hands-on practice with Compose.")),
            ),
            4 to RegistrationDetailResponse(
                shared(
                    "Looking for a practical introduction to modern Android.",
                    "Android Development",
                    "No prior Android experience, strong Java background.",
                ),
                listOf(
                    member(4001, "Rohan Das", true, LONG_EXPECTATION),
                    member(4002, "Meera Nair", true, "Learn Kotlin."),
                    member(4003, "Tanmay Jain", false, "Build a portfolio project."),
                ),
            ),
            // Intentionally empty: exercises the "no responses" state.
            5 to RegistrationDetailResponse(),
            6 to RegistrationDetailResponse(
                shared(
                    "Hackathon practice before the next season.",
                    "Android Development, Cloud",
                    "Participated in two college hackathons.",
                    "We may need power sockets for five laptops.",
                ),
                listOf(
                    member(6001, "Siddharth Rao", true, LONG_EXPECTATION),
                    member(6002, "Neha Gupta", true, "Learn testing on Android."),
                    member(6003, "Arjun Menon", true, "Learn Compose animations."),
                    member(6004, "Pooja Iyer", false, "Understand Gradle builds."),
                    member(6005, "Vikram Chauhan", true, "Learn about publishing apps."),
                ),
            ),
            7 to RegistrationDetailResponse(
                defaultShared,
                listOf(member(7001, "Ananya Gupta", true, LONG_EXPECTATION)),
            ),
            8 to RegistrationDetailResponse(
                shared(
                    "We want to learn Android development together and build useful projects.",
                    "Android Development, Open Source",
                    "Yes, we have worked on small Android apps and have participated in college " +
                            "hackathons and open source contributions.",
                    "Looking forward to collaborating and learning!",
                ),
                listOf(
                    member(101, "Aarav Sharma", true, LONG_EXPECTATION),
                    member(102, "Priya Verma", true, "Get hands-on with Jetpack Compose."),
                    member(103, "Karan Singh", false, "Learn how real apps are structured."),
                    member(104, "Sneha Kapoor", true, "Contribute to open source projects."),
                ),
            ),
        )
    }
}
