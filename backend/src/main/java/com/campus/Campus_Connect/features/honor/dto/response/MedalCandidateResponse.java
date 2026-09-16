package com.campus.Campus_Connect.features.honor.dto.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedalCandidateResponse {

    private Integer registrationId;

    private String name;

    private Integer courseId;

    private Integer admissionYear;

    private Boolean team;

    private String avatarUrl;
}