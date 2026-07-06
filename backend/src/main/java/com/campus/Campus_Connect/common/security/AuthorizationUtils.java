package com.campus.Campus_Connect.common.security;

import com.campus.Campus_Connect.common.exception.UnauthorizedException;
import com.campus.Campus_Connect.features.auth.entity.User;

public final class AuthorizationUtils {

    private AuthorizationUtils() {
    }

    /**
     * Allows only the owner of a resource.
     */
    public static void requireOwner(Integer ownerId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!ownerId.equals(currentUser.getId())) {
            throw new UnauthorizedException(
                    "You are not authorized to perform this action."
            );
        }
    }
}