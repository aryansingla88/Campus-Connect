package com.campus.Campus_Connect.features.registration.dto.response;

import lombok.*;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationDetailResponse {

    private List<Answer> teamAnswers;
    private List<Member> members;

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Member {

        private Integer registrationId;

        private Integer userId;
        private List<QuickField> quickFields;
        private List<Answer> individualAnswers;
    }

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QuickField {

        private String field;

        private String value;
    }

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Answer {

        private Integer fieldId;

        private String fieldLabel;

        private String fieldType;

        private String answer;
    }
}
