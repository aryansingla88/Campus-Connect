package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class TeamPortalResponse {
    Integer teamId;
    Integer leaderUserId;
    String teamName;
    String leaderName;
    Integer memberCount;
    Integer minMembers;
    Integer maxMembers;
    String status;
    Boolean currentUserIsLeader;
    List<Member> members;
    List<TeamRequestResponse> pendingJoinRequests;

    @Value
    @Builder
    public static class Member {
        Integer registrationId;
        Integer userId;
        String displayName;
        String status;
    }
}
