package com.campus.Campus_Connect.features.connection.dto;

import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
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

    private CourseResponse course;

    private Integer academicYear;

    private ConnectionRelationshipStatus status;
}