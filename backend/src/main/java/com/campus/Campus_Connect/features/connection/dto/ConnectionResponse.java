package com.campus.Campus_Connect.features.connection.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionResponse {

    private Integer userId;

    private String username;

    private String fullName;

    private String avatarUrl;

    private Integer courseId;

    private Integer admissionYear;

    private ConnectionRelationshipStatus status;
}