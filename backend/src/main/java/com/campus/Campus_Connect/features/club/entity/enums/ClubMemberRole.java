package com.campus.Campus_Connect.features.club.entity.enums;

public enum ClubMemberRole {
    PRESIDENT,
    VICE_PRESIDENT,
    SECRETARY,
    JOINT_SECRETARY,
    ADDITIONAL_SECRETARY,
    COMMITTEE_HEAD,
    MEMBER;

    public boolean isLeadershipRole() {
        return switch (this) {
            case PRESIDENT, VICE_PRESIDENT, SECRETARY, JOINT_SECRETARY,
                    ADDITIONAL_SECRETARY, COMMITTEE_HEAD -> true;
            case MEMBER -> false;
        };
    }
}
