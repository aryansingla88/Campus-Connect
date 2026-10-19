package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationResponse {

    private Integer registrationId;

    private String status;

    private Integer eventId;

    private Integer teamId;

    private String teamName;

    private Boolean canResume;

    @Builder.Default
    private List<SavedRegistrationAnswerResponse> answers = List.of();
}
