package com.example.campusconnect.feature.profile

import com.example.campusconnect.feature.profile.data.mapper.ProfileMapper
import com.example.campusconnect.feature.profile.data.mapper.withHostelSelection
import com.example.campusconnect.feature.profile.data.remote.response.HonorResponse
import com.example.campusconnect.feature.profile.data.remote.response.ProfileResponse
import com.example.campusconnect.feature.metadata.hostels.Hostel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProfilePrivacyMapperTest {
    @Test
    fun hostelSelectorUsesMetadataIdsAndExplicitClearState() {
        val original = com.example.campusconnect.feature.profile.model.PublicUserProfile(hostelId = 1)
        val metadata = Hostel(6, "DS", "Day Scholar", true)

        assertEquals("DS (Day Scholar)", metadata.displayLabel())

        val selected = original.withHostelSelection(metadata)
        val cleared = selected.withHostelSelection(null)

        assertEquals(6, selected.hostelId)
        assertTrue(!selected.clearHostel)
        assertEquals(null, cleared.hostelId)
        assertTrue(cleared.clearHostel)
    }

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
            hostelId = 1,
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
        assertEquals(1, model.hostelId)
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
