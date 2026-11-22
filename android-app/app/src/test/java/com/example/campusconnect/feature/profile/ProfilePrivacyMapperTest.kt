package com.example.campusconnect.feature.profile

import com.example.campusconnect.feature.profile.data.mapper.ProfileMapper
import com.example.campusconnect.feature.profile.data.remote.response.HonorResponse
import com.example.campusconnect.feature.profile.data.remote.response.ProfileResponse
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePrivacyMapperTest {
    @Test
    fun mapsBackendFilteredFieldsAndHeaderHonors() = runTest {
        val response = ProfileResponse(
            userId = 8,
            username = "viewer",
            email = "viewer@campus.edu",
            fullName = "Viewer",
            bio = null,
            avatarUrl = null,
            courseId = null,
            admissionYear = null,
            hostel = "Hostel 1",
            hometown = null,
            gender = null,
            dob = null,
            age = 21,
            phone = null,
            github = null,
            linkedin = null,
            instagram = null,
            memberSince = null,
            showPhone = false,
            showSocials = false,
            showHometown = false,
            showGender = false,
            showAge = true,
            showHonor = true,
            headerBadges = listOf(
                HonorResponse(1, "BADGE", "Builder", null, null, null, null, 1, "2026-01-01T00:00:00Z")
            ),
            headerMedals = emptyList(),
            relationshipStatus = null
        )

        val model = ProfileMapper.toPublicUserProfile(response)

        assertEquals("viewer@campus.edu", model.email)
        assertEquals("Hostel 1", model.hostel)
        assertEquals("", model.dob)
        assertEquals(21, model.age)
        assertTrue(model.phone.isBlank())
        assertTrue(model.hometown.isBlank())
        assertEquals("Builder", model.headerBadges.single().title)
    }

    @Test
    fun mapperDropsHeaderHonorsWhenBackendSaysHidden() = runTest {
        val response = ProfileResponse(
            userId = 8,
            username = "viewer",
            email = "viewer@campus.edu",
            fullName = "Viewer",
            bio = null,
            avatarUrl = null,
            courseId = null,
            admissionYear = null,
            hostel = "Hostel 1",
            hometown = null,
            gender = null,
            dob = null,
            age = null,
            phone = null,
            github = null,
            linkedin = null,
            instagram = null,
            memberSince = null,
            showPhone = false,
            showSocials = false,
            showHometown = false,
            showGender = false,
            showAge = false,
            showHonor = false,
            headerBadges = listOf(
                HonorResponse(1, "BADGE", "Private", null, null, null, null, 1, "2026-01-01T00:00:00Z")
            ),
            headerMedals = emptyList(),
            relationshipStatus = null
        )

        val model = ProfileMapper.toPublicUserProfile(response)

        assertTrue(model.headerBadges.isEmpty())
        assertTrue(model.headerMedals.isEmpty())
    }
}
