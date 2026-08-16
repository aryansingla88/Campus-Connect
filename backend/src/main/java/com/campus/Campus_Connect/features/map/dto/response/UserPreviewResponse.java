package com.campus.Campus_Connect.features.map.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreviewResponse {

    private Integer userId;

    private String fullName;

    private String courseName;

    private String courseCode;

    private Integer courseYear;

    private Integer admissionYear;

    private String avatarUrl;

    private String bio;

    private Integer mutualConnectionsCount;
}