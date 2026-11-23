package com.example.campusconnect.feature.profile.model


enum class ClubStatus {
    NOT_JOINED,
    PENDING,
    JOINED,
    LEAD
}

enum class ClubMemberRole {
    PRESIDENT,
    VICE_PRESIDENT,
    SECRETARY,
    JOINT_SECRETARY,
    ADDITIONAL_SECRETARY,
    COMMITTEE_HEAD,
    MEMBER
}

enum class ClubMemberFilter {
    ALL,
    CORE,
    COORDINATORS,
    MEMBERS
}

data class ClubDetails(
    val clubId: Int,
    val name: String,
    val clubType: String,
    val description: String?,
    val memberCount: Int,
    val status: ClubStatus,
    val role: ClubMemberRole?
)

data class ClubMember(
    val userId: Int,
    val fullName: String,
    val avatarUrl: String?,
    val role: ClubMemberRole,
    val joinedAt: String?
)

data class ClubMembershipRequest(
    val clubId: Int,
    val userId: Int,
    val fullName: String,
    val username: String?,
    val avatarUrl: String?,
    val role: ClubMemberRole,
    val requestedAt: String?
)

sealed interface ClubLoadState<out T> {
    data object Loading : ClubLoadState<Nothing>
    data class Loaded<T>(val value: T) : ClubLoadState<T>
    data class Error(val message: String) : ClubLoadState<Nothing>
}

fun <T> clubLoadState(
    clubId: Int,
    values: Map<Int, T>,
    loadingIds: Collection<Int>,
    errors: Map<Int, String>
): ClubLoadState<T> = when {
    values.containsKey(clubId) -> ClubLoadState.Loaded(values.getValue(clubId))
    clubId in loadingIds -> ClubLoadState.Loading
    errors[clubId] != null -> ClubLoadState.Error(errors.getValue(clubId))
    else -> ClubLoadState.Loading
}

data class Club(
    val clubId: Int,
    val name: String,
    val clubType: String,
    val logoUrl: String?,
    val memberCount: Int,
    val status: ClubStatus
)

fun filterClubMembers(
    members: List<ClubMember>,
    filter: ClubMemberFilter,
    query: String
): List<ClubMember> {
    val byRole = when (filter) {
        ClubMemberFilter.ALL -> members
        ClubMemberFilter.CORE -> members.filter {
            it.role in setOf(ClubMemberRole.PRESIDENT, ClubMemberRole.VICE_PRESIDENT)
        }
        ClubMemberFilter.COORDINATORS -> members.filter {
            it.role in setOf(
                ClubMemberRole.SECRETARY,
                ClubMemberRole.JOINT_SECRETARY,
                ClubMemberRole.ADDITIONAL_SECRETARY,
                ClubMemberRole.COMMITTEE_HEAD
            )
        }
        ClubMemberFilter.MEMBERS -> members.filter { it.role == ClubMemberRole.MEMBER }
    }
    return if (query.isBlank()) byRole else byRole.filter {
        it.fullName.contains(query.trim(), ignoreCase = true)
    }
}
