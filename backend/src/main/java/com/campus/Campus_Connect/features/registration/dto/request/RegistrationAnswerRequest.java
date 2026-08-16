package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationAnswerRequest {

    private Integer fieldId;

    private String answer;
}