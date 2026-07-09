package com.campus.Campus_Connect.features.metadata.courses;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.metadata.courses.dto.CourseResponse;
import com.campus.Campus_Connect.features.metadata.courses.dto.DetailedCourseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/metadata/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @GetMapping
    public ApiResponse<List<CourseResponse>> getCourses() {
        return courseService.getCourses();
    }

    @GetMapping("/details")
    public ApiResponse<List<DetailedCourseResponse>> getCourseDetails() {
        return courseService.getCourseDetails();
    }

    @GetMapping("/{courseId}")
    public ApiResponse<CourseResponse> getCourseById(
            @PathVariable Integer courseId
    ) {
        return courseService.getCourseById(courseId);
    }
}