package com.example.campusconnect.core.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppAccessPolicyTest {
    @Test
    fun parsesCurrentAndLegacyAccountRolesDuringTransition() {
        AppRole.entries.forEach { assertEquals(it, AppRole.parse(it.name)) }
        assertNull(AppRole.parse("unknown"))
        assertNull(AppRole.parse(null))
    }

    @Test
    fun routesDeveloperToTestScreenAndStudentsToMap() {
        assertEquals("test2", AppAccessPolicy.startRoute(AppRole.DEV))
        listOf(AppRole.STUDENT, AppRole.CLUB_ADMIN, AppRole.TEACHER, AppRole.CAMPUS_CONNECT)
            .forEach { assertEquals("map", AppAccessPolicy.startRoute(it)) }
    }

    @Test
    fun studentAndClubAdminCannotOpenDeveloperScreen() {
        listOf(AppRole.STUDENT, AppRole.CLUB_ADMIN).forEach {
            assertFalse(AppAccessPolicy.canAccess(it, "test2"))
            assertTrue(AppAccessPolicy.canAccess(it, "posts_feed"))
        }
    }

    @Test
    fun teacherAndPlatformIdentityAreLimitedToMapAndEvents() {
        listOf(AppRole.TEACHER, AppRole.CAMPUS_CONNECT).forEach {
            assertTrue(AppAccessPolicy.canAccess(it, "map"))
            assertTrue(AppAccessPolicy.canAccess(it, "events_root"))
            assertTrue(AppAccessPolicy.canAccess(it, "events_main"))
            assertFalse(AppAccessPolicy.canAccess(it, "settings"))
            assertFalse(AppAccessPolicy.canAccess(it, "posts_feed"))
            assertFalse(AppAccessPolicy.canAccess(it, "test2"))
        }
    }

    @Test
    fun missingRoleHasNoProtectedAccess() {
        assertFalse(AppAccessPolicy.canAccessOptional(null, "map"))
        assertFalse(AppAccessPolicy.canAccessOptional(null, "test2"))
        assertTrue(AppAccessPolicy.canAccessOptional(null, "offline_fallback"))
        assertTrue(AppAccessPolicy.canAccessOptional(null, "login"))
    }

    @Test
    fun legacyRolesRemainUsableUntilAccountRemappingIsReviewed() {
        listOf(AppRole.ADMIN, AppRole.MODERATOR).forEach { role ->
            assertEquals("map", AppAccessPolicy.startRoute(role))
            assertTrue(AppAccessPolicy.canAccess(role, "map"))
            assertTrue(AppAccessPolicy.canAccess(role, "events_root"))
            assertTrue(AppAccessPolicy.canAccess(role, "settings_graph"))
        }
    }
}
