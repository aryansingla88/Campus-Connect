package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationMemberResponse {

    private Integer registrationId;

    private Integer userId;

    private String name;

    private String email;

    private Boolean leader;

    private List<RegistrationQuickFieldResponse> quickFields;

    private List<RegistrationAnswerResponse> individualAnswers;
}