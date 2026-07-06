package com.campus.Campus_Connect.common.security;

import com.campus.Campus_Connect.features.auth.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static User getCurrentUser() {

       Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new IllegalStateException("No authenticated user found.");
        }

        return user;
    }

    public static Integer getCurrentUserId() {
        return getCurrentUser().getId();
    }
}