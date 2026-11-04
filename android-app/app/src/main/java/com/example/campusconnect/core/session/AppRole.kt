package com.example.campusconnect.core.session

/** Account-level roles returned by the authenticated backend. */
enum class AppRole {
    STUDENT,
    CLUB_ADMIN,
    TEACHER,
    DEV,
    CAMPUS_CONNECT;

    companion object {
        fun parse(value: String?): AppRole? =
            entries.firstOrNull { it.name == value?.trim()?.uppercase() }
    }
}

object AppAccessPolicy {
    fun startRoute(role: AppRole): String =
        if (role == AppRole.DEV) "test2" else "map"

    fun canAccess(role: AppRole, route: String): Boolean {
        if (route == "splash" || route == "login" || route == "register") return true
        return when (role) {
            AppRole.DEV -> true
            AppRole.STUDENT, AppRole.CLUB_ADMIN -> route != "test2"
            AppRole.TEACHER, AppRole.CAMPUS_CONNECT ->
                route == "map" || route == "events_root" ||
                        route.startsWith("events_")
        }
    }

    fun canAccessOptional(role: AppRole?, route: String): Boolean = when {
        route == "splash" || route == "login" || route == "register" -> true
        route == "offline_fallback" -> true
        role == null -> false
        else -> canAccess(role, route)
    }
}
