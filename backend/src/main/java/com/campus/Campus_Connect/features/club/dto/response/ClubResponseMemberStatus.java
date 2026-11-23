package com.campus.Campus_Connect.features.club.dto.response;

/** Membership state exposed by club-list responses, separate from persisted status and role. */
public enum ClubResponseMemberStatus {
    LEAD,
    APPROVED,
    PENDING,
    REJECTED
}
