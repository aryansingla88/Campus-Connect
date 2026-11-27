package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.common.academic.AcademicStatusCalculator;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventMemberMapper {

    private final CourseRepository courseRepository;

    public UserAccessResponse toResponse(EventMember member) {

        return toResponse(
                member.getUser(),
                member.getRole(),
                true
        );
    }

    public UserAccessResponse toResponse(
            User user,
            EventMemberRole role,
            boolean hasAccess
    ) {

        UserProfile profile = user.getProfile();
        String academicStatus = profile == null || profile.getCourseId() == null ? null
                : courseRepository.findById(profile.getCourseId())
                .map(course -> AcademicStatusCalculator.currentStatus(profile.getAdmissionYear(),
                        course.getDurationYears(), java.time.Clock.systemDefaultZone()))
                .map(com.campus.Campus_Connect.common.academic.AcademicStatus::apiValue)
                .orElse(null);

        return UserAccessResponse.builder()
                .id(user.getId())
                .name(profile.getFullName())
                .courseId(profile.getCourseId())
                .admissionYear(profile.getAdmissionYear())
                .academicStatus(academicStatus)
                .avatarUrl(profile.getAvatarUrl())
                .role(role)
                .hasAccess(hasAccess)
                .build();
    }
}
