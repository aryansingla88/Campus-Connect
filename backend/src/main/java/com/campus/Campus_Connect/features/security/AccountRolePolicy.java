package com.campus.Campus_Connect.features.security;

import com.campus.Campus_Connect.features.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/** Central HTTP-scope policy for account roles. Resource ownership remains enforced by services. */
public final class AccountRolePolicy {
    private AccountRolePolicy() {}

    public static User.Role roleFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority != null && authority.startsWith("ROLE_"))
                .map(authority -> authority.substring("ROLE_".length()))
                .map(value -> {
                    try {
                        return User.Role.valueOf(value);
                    } catch (IllegalArgumentException ignored) {
                        return null;
                    }
                })
                .filter(role -> role != null)
                .findFirst()
                .orElse(null);
    }

    public static boolean isPublic(String method, String path) {
        if (!"POST".equals(method)) return "GET".equals(method) && path.startsWith("/metadata/courses/")
                || "GET".equals(method) && "/metadata/courses".equals(path);
        return path.equals("/auth/login") || path.equals("/auth/register") ||
                path.equals("/auth/password-reset/request") ||
                path.equals("/auth/password-reset/verify") ||
                path.equals("/auth/password-reset/reset");
    }

    public static boolean canAccess(User.Role role, String method, String path) {
        if (isPublic(method, path)) return true;
        if (role == null) return false;
        if (role == User.Role.STUDENT || role == User.Role.CLUB_ADMIN || role == User.Role.DEV) {
            return true;
        }

        if (role == User.Role.TEACHER || role == User.Role.CAMPUS_CONNECT) {
            if ("POST".equals(method) && "/auth/change-password".equals(path)) return true;
            if ("GET".equals(method) && (path.equals("/map") || path.startsWith("/map/"))) return true;
            if (role == User.Role.TEACHER && "PATCH".equals(method) &&
                    path.equals("/map/presence/me")) return true;
            if (isStudentRegistrationPath(path)) return false;
            if (isEventRead(method, path)) return true;
            return role == User.Role.CAMPUS_CONNECT && isPlatformEventWrite(method, path);
        }
        // Legacy account ADMIN/MODERATOR values are deliberately denied pending review.
        return false;
    }

    private static boolean isStudentRegistrationPath(String path) {
        return path.equals("/events/registrations") || path.startsWith("/events/registrations/") ||
                path.equals("/events/teams") || path.startsWith("/events/teams/") ||
                path.equals("/events/team-requests") || path.startsWith("/events/team-requests/") ||
                path.matches("/events/\\d+/registration(?:/.*)?");
    }

    private static boolean isEventRead(String method, String path) {
        if (!"GET".equals(method)) return false;
        if (path.equals("/event-categories") || path.startsWith("/event-categories/")) return true;
        return path.equals("/events") || path.equals("/events/feed") ||
                path.equals("/events/mine") || path.equals("/events/shared") ||
                path.equals("/events/managed") || path.matches("/events/\\d+");
    }

    private static boolean isPlatformEventWrite(String method, String path) {
        return ("POST".equals(method) && path.equals("/events")) ||
                (("PATCH".equals(method) || "DELETE".equals(method)) &&
                        path.matches("/events/\\d+"));
    }
}
