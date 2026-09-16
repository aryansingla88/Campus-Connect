package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.event.dto.response.SoloParticipantResponse;
import com.campus.Campus_Connect.features.event.dto.response.TeamMemberResponse;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParticipantMapper {

    private final ParticipantDisplayMapper participantDisplayMapper;

    public SoloParticipantResponse toSoloResponse(
            EventRegistration registration
    ) {

        var display =
                participantDisplayMapper.toDisplay(
                        registration.getUser()
                );

        return SoloParticipantResponse.builder()
                .registrationId(registration.getId())
                .userId(registration.getUser().getId())
                .name(display.getName())
                .courseId(registration.getUser().getProfile().getCourseId())
                .admissionYear(registration.getUser().getProfile().getAdmissionYear())
                .avatarUrl(display.getAvatarUrl())
                .build();
    }

    public TeamMemberResponse toTeamMemberResponse(
            EventRegistration registration,
            boolean leader
    ) {

        var display =
                participantDisplayMapper.toDisplay(
                        registration.getUser()
                );

        return TeamMemberResponse.builder()
                .registrationId(registration.getId())
                .userId(registration.getUser().getId())
                .name(display.getName())
                .courseId(registration.getUser().getProfile().getCourseId())
                .admissionYear(registration.getUser().getProfile().getAdmissionYear())
                .avatarUrl(display.getAvatarUrl())
                .leader(leader)
                .build();
    }
}