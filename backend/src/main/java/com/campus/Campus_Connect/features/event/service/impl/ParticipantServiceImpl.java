package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantTeamResponse;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantsResponse;
import com.campus.Campus_Connect.features.event.dto.response.SoloParticipantResponse;
import com.campus.Campus_Connect.features.event.dto.response.TeamMemberResponse;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.mapper.ParticipantMapper;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.event.security.EventValidationService;
import com.campus.Campus_Connect.features.event.service.ParticipantService;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import com.campus.Campus_Connect.features.registration.repository.EventRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ParticipantServiceImpl implements ParticipantService {

    private final EventValidationService eventValidationService;

    private final EventPermissionService permissionService;

    private final EventRegistrationRepository registrationRepository;

    private final ParticipantMapper participantMapper;

    @Override
    public ApiResponse<ParticipantsResponse> getParticipants(
            Integer eventId
    ) {

        // Validate event exists
        eventValidationService.getEvent(eventId);

        // Only Creator/Admin can view participants
        permissionService.requireManager(eventId);

        List<EventRegistration> registrations =
                registrationRepository.findByEventId(eventId);

        List<SoloParticipantResponse> soloParticipants =
                new ArrayList<>();

        Map<Integer, List<EventRegistration>> teamRegistrations =
                new LinkedHashMap<>();

        // Separate solo participants and team registrations
        for (EventRegistration registration : registrations) {

            if (registration.getTeam() == null ||
                    registration.getTeam().getId() == null) {

                soloParticipants.add(
                        participantMapper.toSoloResponse(
                                registration
                        )
                );

            } else {

                teamRegistrations
                        .computeIfAbsent(
                                registration.getTeam().getId(),
                                id -> new ArrayList<>()
                        )
                        .add(registration);
            }
        }

        // Build team responses
        List<ParticipantTeamResponse> teams =
                new ArrayList<>();

        for (Map.Entry<Integer, List<EventRegistration>> entry
                : teamRegistrations.entrySet()) {

            List<EventRegistration> teamRegistrationList =
                    entry.getValue();

            if (teamRegistrationList.isEmpty()) {
                continue;
            }

            EventTeam team =
                    teamRegistrationList.get(0).getTeam();

            List<TeamMemberResponse> members =
                    teamRegistrationList.stream()
                            .map(registration ->
                                    participantMapper.toTeamMemberResponse(
                                            registration,
                                            team.getLeader() != null &&
                                                    team.getLeader().getId()
                                                            .equals(
                                                                    registration.getUser().getId()
                                                            )
                                    )
                            )
                            .toList();

            ParticipantTeamResponse teamResponse =
                    ParticipantTeamResponse.builder()
                            .teamId(team.getId())
                            .teamName(team.getTeamName())
                            .leaderName(
                                    team.getLeader() != null
                                            ? participantMapper
                                            .toSoloResponse(
                                                    teamRegistrationList.stream()
                                                            .filter(registration ->
                                                                    registration.getUser().getId()
                                                                            .equals(
                                                                                    team.getLeader().getId()
                                                                            )
                                                            )
                                                            .findFirst()
                                                            .orElse(
                                                                    teamRegistrationList.get(0)
                                                            )
                                            )
                                            .getName()
                                            : null
                            )
                            .memberCount(members.size())
                            .members(members)
                            .build();

            teams.add(teamResponse);
        }

        ParticipantsResponse response =
                ParticipantsResponse.builder()
                        .teams(teams)
                        .soloParticipants(soloParticipants)
                        .totalParticipants(registrations.size())
                        .build();

        return ApiResponse.success(
                response,
                "Participants fetched successfully."
        );
    }
}