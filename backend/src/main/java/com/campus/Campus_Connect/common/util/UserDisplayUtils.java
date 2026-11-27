package com.campus.Campus_Connect.common.util;

import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import com.campus.Campus_Connect.common.academic.AcademicStatus;


public final class UserDisplayUtils {

    private UserDisplayUtils() {
    }

    public static String buildSubtitle(
            Course course,
            AcademicStatus academicStatus
    ) {
        String courseName = buildCourseName(course);

        return courseName
                + " • "
                + (academicStatus == null ? "Academic status unavailable" : academicStatus.displayLabel());
    }

    private static String buildCourseName(Course course) {

        String degree = course.getDegree();
        String courseCode = course.getCourseCode();

        if ("B.Tech".equalsIgnoreCase(degree)
                || "M.Tech".equalsIgnoreCase(degree)) {

            return degree + " " + courseCode;
        }

        return courseCode;
    }

}
