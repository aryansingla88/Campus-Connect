package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationShortResponse {

    private Integer registrationId;

    private String type;

    private String name;
    private Integer teamCount;
    private String email;

    private String status;
    private Instant submittedAt;
}