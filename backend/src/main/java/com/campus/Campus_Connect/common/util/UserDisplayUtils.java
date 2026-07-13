package com.campus.Campus_Connect.common.util;

import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;

import java.time.LocalDate;

public final class UserDisplayUtils {

    private UserDisplayUtils() {
    }

    public static String buildSubtitle(
            UserProfile profile,
            Course course
    ) {

        int studyYear = Math.max(
                1,
                getCurrentAcademicYear() - profile.getAdmissionYear()
        );

        String courseName = buildCourseName(course);

        return courseName
                + " • "
                + studyYear
                + getOrdinal(studyYear)
                + " Year";
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

    private static int getCurrentAcademicYear() {

        LocalDate today = LocalDate.now();

        int academicYear = today.getYear();

        // Academic session starts in July
        if (today.getMonthValue() >= 7) {
            academicYear++;
        }

        return academicYear;
    }

    private static String getOrdinal(int year) {

        return switch (year) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}