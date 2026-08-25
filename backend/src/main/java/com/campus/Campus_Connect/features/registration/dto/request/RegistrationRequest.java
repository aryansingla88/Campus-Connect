package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {

    private List<RegistrationAnswerRequest> answers;
}