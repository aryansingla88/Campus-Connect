package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationAnswerRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import com.campus.Campus_Connect.features.registration.entity.RegistrationAnswer;
import com.campus.Campus_Connect.features.registration.entity.RegistrationAnswerId;
import com.campus.Campus_Connect.features.registration.entity.RegistrationConfig;
import com.campus.Campus_Connect.features.registration.entity.RegistrationField;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.repository.EventRegistrationRepository;
import com.campus.Campus_Connect.features.registration.repository.RegistrationAnswerRepository;
import com.campus.Campus_Connect.features.registration.repository.RegistrationConfigRepository;
import com.campus.Campus_Connect.features.registration.repository.RegistrationFieldRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationConfigRepository configRepository;
    private final EventRegistrationRepository registrationRepository;
    private final RegistrationFieldRepository fieldRepository;
    private final RegistrationAnswerRepository answerRepository;
    private final EventTeamRepository teamRepository;

    @Transactional
    public ApiResponse<RegistrationResponse> registerForEvent(Integer eventId, RegistrationRequest request) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) {
            return ApiResponse.failure("User is not authenticated.");
        }

        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null) {
            return ApiResponse.failure("Registration is not configured for this event.");
        }

        Instant now = Instant.now();
        if (config.getRegistrationCreatedAt() != null && now.isBefore(config.getRegistrationCreatedAt())) {
            return ApiResponse.failure("Registration has not opened yet.");
        }
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt())) {
            return ApiResponse.failure("Registration has closed.");
        }

        if (registrationRepository.existsByEventIdAndUserId(eventId, currentUser.getId())) {
            return ApiResponse.failure("You are already registered for this event.");
        }

        EventTeam team = null;
        boolean isLeader = false;
        boolean isTeamMember = false;

        if (request.getTeamName() != null && !request.getTeamName().isBlank()) {
            if (config.getMaxTeamMembers() == null || config.getMaxTeamMembers() <= 1) {
                return ApiResponse.failure("Team registrations are not allowed for this event.");
            }
            if (teamRepository.findByEvent_IdAndTeamName(eventId, request.getTeamName()).isPresent()) {
                return ApiResponse.failure("Team name is already taken for this event.");
            }
            team = EventTeam.builder()
                    .event(event)
                    .teamName(request.getTeamName())
                    .leader(currentUser)
                    .build();
            team = teamRepository.save(team);
            isLeader = true;
            isTeamMember = true;
        } else if (request.getTeamId() != null) {
            if (config.getMaxTeamMembers() == null || config.getMaxTeamMembers() <= 1) {
                return ApiResponse.failure("Team registrations are not allowed for this event.");
            }
            team = teamRepository.findById(request.getTeamId()).orElse(null);
            if (team == null || !team.getEvent().getId().equals(eventId)) {
                return ApiResponse.failure("Team not found for this event.");
            }
            long currentTeamCount = registrationRepository.countByEventIdAndTeamId(eventId, team.getId());
            if (currentTeamCount >= config.getMaxTeamMembers()) {
                return ApiResponse.failure("Team is full.");
            }
            isLeader = team.getLeader().getId().equals(currentUser.getId());
            isTeamMember = true;
        } else {
            if (config.getMinTeamMembers() != null && config.getMinTeamMembers() > 1) {
                return ApiResponse.failure("Solo registrations are not allowed for this event. You must create or join a team.");
            }
            team = null;
            isLeader = false;
            isTeamMember = false;
        }

        List<RegistrationField> fields = fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(eventId);
        List<RegistrationAnswerRequest> submittedAnswers = request.getAnswers() != null ? request.getAnswers() : List.of();

        for (RegistrationAnswerRequest submitted : submittedAnswers) {
            boolean validField = fields.stream().anyMatch(f -> f.getId().equals(submitted.getFieldId()));
            if (!validField) {
                return ApiResponse.failure("Invalid registration field ID: " + submitted.getFieldId());
            }
        }

        if (isTeamMember && !isLeader) {
            boolean submittedTeamAnswer = submittedAnswers.stream().anyMatch(ans -> {
                RegistrationField field = fields.stream()
                        .filter(f -> f.getId().equals(ans.getFieldId()))
                        .findFirst().orElse(null);
                return field != null && Boolean.FALSE.equals(field.getIndividual());
            });
            if (submittedTeamAnswer) {
                return ApiResponse.failure("Team questions must be answered by the team leader.");
            }
        }

        for (RegistrationField field : fields) {
            if (!Boolean.TRUE.equals(field.getRequired())) continue;

            if (Boolean.FALSE.equals(field.getIndividual()) && isTeamMember && !isLeader) {
                continue;
            }

            boolean answered = submittedAnswers.stream().anyMatch(ans ->
                    ans.getFieldId().equals(field.getId())
                            && ans.getAnswer() != null
                            && !ans.getAnswer().isBlank());
            if (!answered) {
                return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
            }
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(currentUser)
                .status(RegistrationStatus.PENDING)
                .submittedAt(Instant.now())
                .team(team)
                .build();

        registration = registrationRepository.save(registration);

        for (RegistrationAnswerRequest submitted : submittedAnswers) {
            if (isTeamMember && !isLeader) {
                RegistrationField f = fields.stream().filter(field -> field.getId().equals(submitted.getFieldId())).findFirst().orElse(null);
                if (f != null && Boolean.FALSE.equals(f.getIndividual())) {
                    continue;
                }
            }
            RegistrationAnswer answer = RegistrationAnswer.builder()
                    .id(new RegistrationAnswerId(registration.getId(), submitted.getFieldId()))
                    .registration(registration)
                    .field(fieldRepository.findById(submitted.getFieldId()).orElseThrow())
                    .answer(submitted.getAnswer())
                    .build();
            answerRepository.save(answer);
        }

        return ApiResponse.success(
                RegistrationResponse.builder()
                        .registrationId(registration.getId())
                        .registered(true)
                        .status(registration.getStatus().name())
                        .build(),
                "Registration submitted successfully.");
    }

    public ApiResponse<RegistrationResponse> getRegistration(Integer eventId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ApiResponse.failure("User is not authenticated.");
        if (!eventRepository.existsById(eventId)) return ApiResponse.failure("Event not found.");

        Optional<EventRegistration> registrationOpt =
                registrationRepository.findByEventIdAndUserId(eventId, currentUser.getId());

        if (registrationOpt.isEmpty()) {
            return ApiResponse.success(RegistrationResponse.builder().registered(false).build(),
                    "You are not registered for this event.");
        }

        EventRegistration data = registrationOpt.get();
        return ApiResponse.success(
                RegistrationResponse.builder()
                        .registrationId(data.getId())
                        .registered(true)
                        .status(data.getStatus().name())
                        .build(),
                "Registration found.");
    }

    @Transactional
    public ApiResponse<Void> cancelRegistration(Integer eventId) {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ApiResponse.failure("User is not authenticated.");

        Optional<EventRegistration> registrationOpt =
                registrationRepository.findByEventIdAndUserId(eventId, currentUser.getId());

        if (registrationOpt.isEmpty()) return ApiResponse.failure("You are not registered for this event.");
        registrationRepository.delete(registrationOpt.get());
        return ApiResponse.success(null, "Registration cancelled successfully.");
    }

    public ApiResponse<List<RegistrationResponse>> getMyRegistrations() {
        User currentUser = SecurityUtils.getCurrentUser();
        if (currentUser == null) return ApiResponse.failure("User is not authenticated.");

        List<RegistrationResponse> response = registrationRepository.findByUserId(currentUser.getId())
                .stream()
                .map(registration -> RegistrationResponse.builder()
                        .registrationId(registration.getId())
                        .registered(true)
                        .status(registration.getStatus().name())
                        .build())
                .toList();

        return ApiResponse.success(response, "Registrations fetched successfully.");
    }
}
