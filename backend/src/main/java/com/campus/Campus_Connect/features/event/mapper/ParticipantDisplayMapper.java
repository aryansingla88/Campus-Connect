package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.util.UserDisplayUtils;
import com.campus.Campus_Connect.common.academic.AcademicStatusCalculator;
import com.campus.Campus_Connect.common.academic.AcademicStatus;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantDisplayResponse;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantDisplayMapper {

    private final CourseRepository courseRepository;

    public ParticipantDisplayResponse toDisplay(
            User user
    ) {

        UserProfile profile = user.getProfile();

        Course course = courseRepository.findById(
                        profile.getCourseId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found."
                        )
                );

        AcademicStatus academicStatus = AcademicStatusCalculator.currentStatus(profile.getAdmissionYear(),
                course.getDurationYears(), java.time.Clock.systemDefaultZone());
        return ParticipantDisplayResponse.builder()
                .name(profile.getFullName())
                .subtitle(
                        UserDisplayUtils.buildSubtitle(
                                course,
                                academicStatus
                        )
                )
                .academicStatus(academicStatus == null ? null : academicStatus.apiValue())
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }
}
