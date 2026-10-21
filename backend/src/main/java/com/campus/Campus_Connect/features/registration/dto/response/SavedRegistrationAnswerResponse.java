package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedRegistrationAnswerResponse {
    private Integer fieldId;
    private String answer;
    private String type;
}
