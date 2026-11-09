package com.campus.Campus_Connect.features.security;

import com.campus.Campus_Connect.features.auth.entity.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccountRolePolicyTest {
    @Test
    void authAndCourseMetadataEndpointsArePublic() {
        assertTrue(AccountRolePolicy.isPublic("POST", "/auth/login"));
        assertTrue(AccountRolePolicy.isPublic("POST", "/auth/register"));
        assertTrue(AccountRolePolicy.isPublic("POST", "/auth/password-reset/request"));
        assertTrue(AccountRolePolicy.isPublic("GET", "/metadata/courses"));
        assertTrue(AccountRolePolicy.isPublic("GET", "/metadata/courses/details"));
        assertFalse(AccountRolePolicy.isPublic("GET", "/auth/me"));
        assertFalse(AccountRolePolicy.isPublic("POST", "/metadata/courses"));
    }

    @Test
    void teacherCanReadMapAndEventsButCannotAccessStudentFeatures() {
        assertTrue(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/map/poi"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/events/22"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/event-categories"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.TEACHER, "PATCH", "/map/presence/me"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/auth/me"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/posts"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.TEACHER, "POST", "/events"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/events/22/registration"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.TEACHER, "GET", "/events/registrations"));
    }

    @Test
    void platformIdentityCanPublishEventsButNotUseStudentWorkflows() {
        assertTrue(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "GET", "/map/events"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "POST", "/events"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "PATCH", "/events/22"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "DELETE", "/events/22" + "/registration"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "GET", "/events/registrations"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.CAMPUS_CONNECT, "POST", "/posts"));
    }

    @Test
    void studentsAndDevelopersKeepTheirFeaturesAndLegacyRolesFailClosed() {
        assertTrue(AccountRolePolicy.canAccess(User.Role.STUDENT, "GET", "/posts"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.CLUB_ADMIN, "POST", "/events/77/registration"));
        assertTrue(AccountRolePolicy.canAccess(User.Role.DEV, "GET", "/auth/me"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.ADMIN, "GET", "/posts"));
        assertFalse(AccountRolePolicy.canAccess(User.Role.MODERATOR, "GET", "/posts"));
        assertFalse(AccountRolePolicy.canAccess(null, "GET", "/events"));
    }
}
