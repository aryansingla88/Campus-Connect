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

    private Integer registrationId;

    private Integer eventId;

    private String type;

    private String status;

    private Instant submittedAt;

    private Team team;

    private List<Member> members;

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Team {

        private Integer teamId;

        private String teamName;

        private Integer memberCount;

        private Member leader;

        private List<Answer> answers;
    }

    //    ---------------------------
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Member {

        private Integer registrationId;

        private Integer userId;

        private String name;

        private String email;

        private Boolean leader;

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
