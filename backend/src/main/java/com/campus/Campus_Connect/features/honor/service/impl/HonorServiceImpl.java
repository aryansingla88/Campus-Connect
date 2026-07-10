package com.campus.Campus_Connect.features.honor.service.impl;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantDisplayResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.mapper.ParticipantDisplayMapper;
import com.campus.Campus_Connect.features.event.repository.EventRegistrationRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.event.security.EventValidationService;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.honor.dto.request.AwardMedalRequest;
import com.campus.Campus_Connect.features.honor.dto.response.EventMedalsResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalCandidateResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalResponse;
import com.campus.Campus_Connect.features.honor.entity.HonorItem;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import com.campus.Campus_Connect.features.honor.entity.UserHonorId;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
import com.campus.Campus_Connect.features.honor.enums.MedalType;
import com.campus.Campus_Connect.features.honor.mapper.HonorMapper;
import com.campus.Campus_Connect.features.honor.repository.HonorItemRepository;
import com.campus.Campus_Connect.features.honor.repository.UserHonorRepository;
import com.campus.Campus_Connect.features.honor.service.HonorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HonorServiceImpl implements HonorService {

    private final EventRegistrationRepository eventRegistrationRepository;

    private final EventTeamRepository eventTeamRepository;

    private final ParticipantDisplayMapper participantDisplayMapper;

    private final HonorItemRepository honorItemRepository;

    private final UserHonorRepository userHonorRepository;

    private final EventValidationService eventValidationService;

    private final EventPermissionService permissionService;

    private final HonorMapper honorMapper;

    @Override
    public ApiResponse<EventMedalsResponse> getEventMedals(
            Integer eventId
    ) {

        eventValidationService.getEvent(eventId);

        permissionService.requireCreator(eventId);

        MedalResponse gold =
                buildMedal(
                        eventId,
                        MedalType.GOLD
                );

        MedalResponse silver =
                buildMedal(
                        eventId,
                        MedalType.SILVER
                );

        MedalResponse bronze =
                buildMedal(
                        eventId,
                        MedalType.BRONZE
                );

        int awardedCount = 0;

        if (gold.getAwarded()) awardedCount++;
        if (silver.getAwarded()) awardedCount++;
        if (bronze.getAwarded()) awardedCount++;

        EventMedalsResponse response =
                EventMedalsResponse.builder()
                        .gold(gold)
                        .silver(silver)
                        .bronze(bronze)
                        .awardedCount(awardedCount)
                        .build();

        return ApiResponse.success(
                response,
                "Medals fetched successfully."
        );
    }

    private MedalResponse buildMedal(
            Integer eventId,
            MedalType medalType
    ) {

        HonorItem honor =
                honorItemRepository
                        .findByEvent_IdAndTypeAndTitle(
                                eventId,
                                HonorType.MEDAL,
                                medalType.getTitle()
                        )
                        .orElse(null);

        if (honor == null) {

            return MedalResponse.builder()
                    .title(medalType.getTitle())
                    .awarded(false)
                    .recipient(null)
                    .build();
        }

        return honorMapper.toMedalResponse(
                honor,
                userHonorRepository.findAllByHonor_Id(
                        honor.getId()
                )
        );
    }

    @Override
    public ApiResponse<List<MedalCandidateResponse>> getEligibleParticipants(
            Integer eventId
    ) {

        eventValidationService.getEvent(eventId);

        permissionService.requireCreator(eventId);

        List<EventRegistration> registrations =
                eventRegistrationRepository.findByEventId(eventId);

        List<HonorItem> honors =
                honorItemRepository.findAllByEventIdAndType(
                        eventId,
                        HonorType.MEDAL
                );

        List<UserHonor> userHonors =
                userHonorRepository.findAllByHonorIn(honors);

        Set<Integer> awardedUserIds =
                userHonors.stream()
                        .map(userHonor ->
                                userHonor.getUser().getId())
                        .collect(Collectors.toSet());

        Set<Integer> processedTeams = new HashSet<>();

        List<MedalCandidateResponse> response =
                new ArrayList<>();

        for (EventRegistration registration : registrations) {

            if (registration.getTeam() == null) {

                addSoloCandidate(
                        registration,
                        awardedUserIds,
                        response
                );

            } else {

                addTeamCandidate(
                        registration,
                        awardedUserIds,
                        processedTeams,
                        response
                );
            }
        }

        return ApiResponse.success(
                response,
                "Eligible participants fetched successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> awardMedal(
            Integer eventId,
            AwardMedalRequest request
    ) {

        Event event = eventValidationService.getEvent(eventId);

        permissionService.requireCreator(eventId);

        if (event.getEndTime().isAfter(Instant.now())) {
            throw new BadRequestException(
                    "Medals can only be awarded after the event ends."
            );
        }

        EventRegistration registration =
                eventRegistrationRepository
                        .findByIdAndEventId(
                                request.getRegistrationId(),
                                eventId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Participant not found."
                                )
                        );

        if (honorItemRepository.existsByEventIdAndTypeAndTitle(
                eventId,
                HonorType.MEDAL,
                request.getMedalType().getTitle()
        )) {

            throw new BadRequestException(
                    request.getMedalType().getTitle()
                            + " has already been awarded."
            );
        }

        if (registration.getTeam() == null) {

            if (userHonorRepository.existsByUser_IdAndHonor_Event_Id(
                    registration.getUser().getId(),
                    eventId
            )) {

                throw new BadRequestException(
                        "Participant has already received a medal."
                );
            }

        } else {

            List<EventRegistration> members =
                    eventRegistrationRepository.findByEventIdAndTeamId(
                            eventId,
                            registration.getTeam().getId()
                    );

            for (EventRegistration member : members) {

                if (userHonorRepository.existsByUser_IdAndHonor_Event_Id(
                        member.getUser().getId(),
                        eventId
                )) {

                    throw new BadRequestException(
                            "This team has already received a medal."
                    );
                }
            }
        }

        HonorItem honor = createHonorItem(
                event,
                registration,
                request.getMedalType()
        );

        assignUsers(
                registration,
                honor
        );

        return ApiResponse.success(
                null,
                "Medal awarded successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> deleteMedal(
            Integer eventId,
            Integer honorId
    ) {

        Event event = eventValidationService.getEvent(eventId);

        permissionService.requireCreator(eventId);

        HonorItem honor =
                honorItemRepository
                        .findByIdAndEvent_Id(
                                honorId,
                                eventId
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Medal not found."
                                ));

        userHonorRepository.deleteByHonor_Id(
                honorId
        );

        honorItemRepository.delete(
                honor
        );

        return ApiResponse.success(
                null,
                "Medal removed successfully."
        );
    }

    private void addSoloCandidate(
            EventRegistration registration,
            Set<Integer> awardedUserIds,
            List<MedalCandidateResponse> response
    ) {

        if (awardedUserIds.contains(
                registration.getUser().getId()
        )) {
            return;
        }

        ParticipantDisplayResponse display =
                participantDisplayMapper.toDisplay(
                        registration.getUser()
                );

        response.add(
                MedalCandidateResponse.builder()
                        .registrationId(registration.getId())
                        .name(display.getName())
                        .subtitle(display.getSubtitle())
                        .avatarUrl(display.getAvatarUrl())
                        .team(false)
                        .build()
        );
    }

    private void addTeamCandidate(
            EventRegistration registration,
            Set<Integer> awardedUserIds,
            Set<Integer> processedTeams,
            List<MedalCandidateResponse> response
    ) {

        EventTeam team = registration.getTeam();

        if (!processedTeams.add(team.getId())) {
            return;
        }

        for (EventRegistration member : team.getRegistrations()) {

            if (awardedUserIds.contains(
                    member.getUser().getId()
            )) {
                return;
            }
        }

        response.add(
                MedalCandidateResponse.builder()
                        .registrationId(registration.getId())
                        .name(team.getTeamName())
                        .subtitle(
                                "Led by "
                                        + team.getLeader()
                                        .getProfile()
                                        .getFullName()
                        )
                        .avatarUrl(null)
                        .team(true)
                        .build()
        );
    }

    private HonorItem createHonorItem(
            Event event,
            EventRegistration registration,
            MedalType medalType
    ) {

        HonorItem honor =
                HonorItem.builder()
                        .type(HonorType.MEDAL)
                        .title(medalType.getTitle())
                        .subtitle(
                                registration.getTeam() == null
                                        ? "SOLO"
                                        : registration.getTeam().getTeamName()
                        )
                        .event(event)
                        .condition(null)
                        .build();

        return honorItemRepository.save(
                honor
        );
    }

    private void assignUsers(
            EventRegistration registration,
            HonorItem honor
    ) {

        if (registration.getTeam() == null) {

            saveUserHonor(
                    registration.getUser(),
                    honor
            );

            return;
        }

        List<EventRegistration> members =
                eventRegistrationRepository.findByEventIdAndTeamId(
                        registration.getEvent().getId(),
                        registration.getTeam().getId()
                );

        for (EventRegistration member : members) {

            saveUserHonor(
                    member.getUser(),
                    honor
            );
        }
    }

    private void saveUserHonor(
            User user,
            HonorItem honor
    ) {

        UserHonor userHonor =
                UserHonor.builder()
                        .id(
                                new UserHonorId(
                                        user.getId(),
                                        honor.getId()
                                )
                        )
                        .user(user)
                        .honor(honor)
                        .priority(1)
                        .awardedAt(Instant.now())
                        .build();

        userHonorRepository.save(
                userHonor
        );
    }
}