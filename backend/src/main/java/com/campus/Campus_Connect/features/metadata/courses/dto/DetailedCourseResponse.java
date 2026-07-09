package com.campus.Campus_Connect.features.metadata.courses.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedCourseResponse {

    private Integer courseId;

    private String degree;

    private String programName;

    private String courseCode;

    private String degreeLevel;

    private Integer durationYears;
}