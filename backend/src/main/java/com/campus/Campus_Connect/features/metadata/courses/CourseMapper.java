package com.campus.Campus_Connect.features.metadata.courses;

import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.dto.DetailedCourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {

    public CourseResponse toCourseResponse(Course course) {

        return CourseResponse.builder()
                .courseId(course.getId())
                .degree(course.getDegree())
                .programname(course.getProgramName())
                .courseCode(course.getCourseCode())
                .build();
    }

    public DetailedCourseResponse toDetailedCourseResponse(Course course) {

        return DetailedCourseResponse.builder()
                .courseId(course.getId())
                .degree(course.getDegree())
                .programName(course.getProgramName())
                .courseCode(course.getCourseCode())
                .degreeLevel(course.getDegreeLevel())
                .durationYears(course.getDurationYears())
                .build();
    }
}