package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationTeamDetailResponse {

    private Integer teamId;

    private String teamName;

    private Integer memberCount;

    private RegistrationMemberResponse leader;

    private List<RegistrationAnswerResponse> answers;
}