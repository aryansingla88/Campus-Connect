package com.campus.Campus_Connect.features.metadata.courses;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.dto.DetailedCourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;

    private final CourseMapper courseMapper;

    public ApiResponse<List<CourseResponse>> getCourses() {

        List<Course> courses = courseRepository.findAllByOrderByDegreeAscProgramAsc();

        List<CourseResponse> response = new ArrayList<>();

        for (Course course : courses) {
            response.add(courseMapper.toCourseResponse(course));
        }

        return ApiResponse.success(
                response,
                "Courses fetched successfully."
        );
    }

    public ApiResponse<List<DetailedCourseResponse>> getCourseDetails() {

        List<Course> courses = courseRepository.findAllByOrderByDegreeAscProgramAsc();

        List<DetailedCourseResponse> response = new ArrayList<>();

        for (Course course : courses) {
            response.add(courseMapper.toDetailedCourseResponse(course));
        }

        return ApiResponse.success(
                response,
                "Course details fetched successfully."
        );
    }

    public ApiResponse<CourseResponse> getCourseById(Integer courseId) {

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Course not found.")
                );

        return ApiResponse.success(
                courseMapper.toCourseResponse(course),
                "Course fetched successfully."
        );
    }
}