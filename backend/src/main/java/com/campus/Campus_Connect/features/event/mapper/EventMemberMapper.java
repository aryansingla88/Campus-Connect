package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.util.UserDisplayUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
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

        Course course = courseRepository.findById(profile.getCourseId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Course not found."));

        return UserAccessResponse.builder()
                .id(user.getId())
                .name(profile.getFullName())
                .subtitle(
                        UserDisplayUtils.buildSubtitle(
                                profile,
                                course
                        )
                )
                .role(role)
                .hasAccess(hasAccess)
                .build();
    }

}