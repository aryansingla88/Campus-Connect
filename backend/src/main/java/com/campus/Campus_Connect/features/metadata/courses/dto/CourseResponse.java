package com.campus.Campus_Connect.features.metadata.courses.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseResponse {

    private Integer courseId;

    private String degree;

    private String programName;

    private String courseCode;
}