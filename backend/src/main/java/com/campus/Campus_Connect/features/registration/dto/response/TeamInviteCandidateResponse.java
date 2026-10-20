package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamInviteCandidateResponse {
    Integer registrationId;
    Integer participantId;
    String name;
    String email;
    String program;
}
