package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamCandidateResponse {
    Integer registrationId;
    Integer userId;
    String displayName;
}
