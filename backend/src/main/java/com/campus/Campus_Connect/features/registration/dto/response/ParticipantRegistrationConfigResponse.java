package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class ParticipantRegistrationConfigResponse {
    Integer eventId;
    Integer minTeamMembers;
    Integer maxTeamMembers;
    Instant registrationCreatedAt;
    Instant registrationEndAt;
}
