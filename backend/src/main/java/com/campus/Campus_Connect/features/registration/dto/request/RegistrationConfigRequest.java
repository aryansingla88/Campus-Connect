package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationConfigRequest {

    private Integer minTeamMembers;

    private Integer maxTeamMembers;

    private Long quickFieldMask;

    private Instant registrationCreatedAt;

    private Instant registrationEndAt;
}
