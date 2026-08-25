package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationAnswerResponse {

    private Integer fieldId;

    private String fieldLabel;

    private String answer;
}