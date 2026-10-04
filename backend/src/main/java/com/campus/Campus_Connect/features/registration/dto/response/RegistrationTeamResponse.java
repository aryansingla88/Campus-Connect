package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationTeamResponse {

    private Integer teamId;
    private String teamName;
    private RegistrationParticipantResponse leader;
}
