package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.util.UserDisplayUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.event.dto.response.SoloParticipantResponse;
import com.campus.Campus_Connect.features.event.dto.response.TeamMemberResponse;
import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.courses.entity.Course;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    private final CourseRepository courseRepository;

    public SoloParticipantResponse toSoloResponse(
            EventRegistration registration
    ) {

        User user = registration.getUser();

        UserProfile profile = user.getProfile();

        Course course = courseRepository.findById(
                        profile.getCourseId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found."
                        )
                );

        return SoloParticipantResponse.builder()
                .registrationId(registration.getId())
                .userId(user.getId())
                .name(profile.getFullName())
                .subtitle(
                        UserDisplayUtils.buildSubtitle(
                                profile,
                                course
                        )
                )
                .avatarUrl(profile.getAvatarUrl())
                .build();
    }

    public TeamMemberResponse toTeamMemberResponse(
            EventRegistration registration,
            boolean leader
    ) {

        User user = registration.getUser();

        UserProfile profile = user.getProfile();

        Course course = courseRepository.findById(
                        profile.getCourseId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Course not found."
                        )
                );

        return TeamMemberResponse.builder()
                .registrationId(registration.getId())
                .userId(user.getId())
                .name(profile.getFullName())
                .subtitle(
                        UserDisplayUtils.buildSubtitle(
                                profile,
                                course
                        )
                )
                .avatarUrl(profile.getAvatarUrl())
                .leader(leader)
                .build();
    }
}