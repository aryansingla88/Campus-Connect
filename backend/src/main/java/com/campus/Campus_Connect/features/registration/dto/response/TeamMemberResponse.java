package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamMemberResponse {
    Integer registrationId;
    Integer userId;
    String name;
    String email;
    String role;
    String program;
}
