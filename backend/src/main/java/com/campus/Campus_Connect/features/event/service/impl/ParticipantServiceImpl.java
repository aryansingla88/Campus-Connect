package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantTeamResponse;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantsResponse;
import com.campus.Campus_Connect.features.event.dto.response.SoloParticipantResponse;
import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import com.campus.Campus_Connect.features.event.mapper.ParticipantMapper;
import com.campus.Campus_Connect.features.event.repository.EventRegistrationRepository;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.event.security.EventValidationService;
import com.campus.Campus_Connect.features.event.service.ParticipantService;
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

        /*TODO:
         * Keeping team grouping ready.
         * Team mapping will be implemented once Team Registration is completed.
         */
        Map<Integer, List<EventRegistration>> teamRegistrations =
                new LinkedHashMap<>();

        for (EventRegistration registration : registrations) {

            if (registration.getTeam().getId() == null) {

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

        //TODO: Will be populated once Team Registration feature is implemented.
        List<ParticipantTeamResponse> teams =
                new ArrayList<>();

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