package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AvailableTeamResponse {
    Integer teamId;
    String teamName;
    Integer memberCount;
    Integer capacity;
    String leaderName;
}
