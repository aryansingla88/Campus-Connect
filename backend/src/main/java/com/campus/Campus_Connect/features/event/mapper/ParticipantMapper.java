package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.util.UserDisplayUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantDisplayResponse;
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
    private final ParticipantDisplayMapper participantDisplayMapper;

    public SoloParticipantResponse toSoloResponse(
            EventRegistration registration
    ) {

        ParticipantDisplayResponse display =
                participantDisplayMapper.toDisplay(
                        registration.getUser()
                );

        return SoloParticipantResponse.builder()
                .registrationId(registration.getId())
                .userId(registration.getUser().getId())
                .name(display.getName())
                .subtitle(display.getSubtitle())
                .avatarUrl(display.getAvatarUrl())
                .build();
    }

    public TeamMemberResponse toTeamMemberResponse(
            EventRegistration registration,
            boolean leader
    ) {

        ParticipantDisplayResponse display =
                participantDisplayMapper.toDisplay(
                        registration.getUser()
                );

        return TeamMemberResponse.builder()
                .registrationId(registration.getId())
                .userId(registration.getUser().getId())
                .name(display.getName())
                .subtitle(display.getSubtitle())
                .avatarUrl(display.getAvatarUrl())
                .leader(leader)
                .build();
    }
}