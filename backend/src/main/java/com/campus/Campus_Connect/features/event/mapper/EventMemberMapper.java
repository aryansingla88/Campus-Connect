package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class EventMemberMapper {

    private final CourseRepository courseRepository;

    public UserAccessResponse toResponse(EventMember member) {

        User user = member.getUser();
        UserProfile profile = user.getProfile();

        Course course = courseRepository.findById(profile.getCourseId())
                .orElseThrow(() ->
                        new RuntimeException("Course not found."));

        return UserAccessResponse.builder()
                .id(user.getId())
                .name(profile.getFullName())
                .subtitle(buildSubtitle(profile, course))
                .role(member.getRole())
                .build();
    }

    private String buildSubtitle(UserProfile profile, Course course) {

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

    private String buildCourseName(Course course) {

        String degree = course.getDegree();
        String courseCode = course.getCourseCode();

        if ("B.Tech".equalsIgnoreCase(degree)
                || "M.Tech".equalsIgnoreCase(degree)) {

            return degree + " " + courseCode;
        }

        return courseCode;
    }

    private int getCurrentAcademicYear() {

        LocalDate today = LocalDate.now();

        int academicYear = today.getYear();

        // Academic session starts in July
        if (today.getMonthValue() >= 7) {
            academicYear++;
        }

        return academicYear;
    }

    private String getOrdinal(int year) {

        return switch (year) {
            case 1 -> "st";
            case 2 -> "nd";
            case 3 -> "rd";
            default -> "th";
        };
    }
}