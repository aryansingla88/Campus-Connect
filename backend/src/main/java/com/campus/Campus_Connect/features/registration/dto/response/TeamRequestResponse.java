package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequestResponse {
    private Integer requestId;
    private Integer teamId;
    private String teamName;
    private Integer registrationId;
    private Integer participantId;
    private String participantName;
    private String initiatedBy;
    private String status;
    private Instant createdAt;
}
