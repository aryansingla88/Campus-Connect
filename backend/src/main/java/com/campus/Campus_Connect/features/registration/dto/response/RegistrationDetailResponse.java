package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDetailResponse {

    private Integer registrationId;

    private Integer eventId;

    private String type;

    private String status;

    private Instant submittedAt;

    private RegistrationTeamDetailResponse team;

    private List<RegistrationMemberResponse> members;
}