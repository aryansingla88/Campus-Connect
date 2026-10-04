package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationShortResponse {

    private Integer registrationId;

    private String type;

    // Team registration
    private String teamName;
    private Integer memberCount;
    private String leaderEmail;

    // Individual registration
    private String name;
    private String email;

    private String status;
}