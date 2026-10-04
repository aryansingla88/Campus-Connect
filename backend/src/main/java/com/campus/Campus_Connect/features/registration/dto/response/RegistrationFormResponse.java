package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationFormResponse {

    private RegistrationConfigResponse config;

    private Long quickFieldMask;

    private Boolean individualAllowed;

    private Boolean teamAllowed;

    private Integer minTeamMembers;

    private Integer maxTeamMembers;

    private Instant registrationCreatedAt;

    private Instant registrationEndAt;

    private List<RegistrationFieldResponse> individualFields;

    private List<RegistrationFieldResponse> teamFields;
}
