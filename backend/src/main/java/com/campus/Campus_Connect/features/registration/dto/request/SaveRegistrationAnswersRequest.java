package com.campus.Campus_Connect.features.registration.dto.request;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveRegistrationAnswersRequest {

    private AnswerType type;
    private List<Answer> answers;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Answer {
        private Integer fieldId;
        private String answer;
    }
}
