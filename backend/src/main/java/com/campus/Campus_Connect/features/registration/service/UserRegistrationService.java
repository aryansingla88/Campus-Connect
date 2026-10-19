package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.request.AnswerType;
import com.campus.Campus_Connect.features.registration.dto.request.SaveRegistrationAnswersRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldResponse;
import com.campus.Campus_Connect.features.registration.dto.response.AvailableTeamResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.dto.response.SavedRegistrationAnswerResponse;
import com.campus.Campus_Connect.features.registration.entity.*;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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
    private final RegistrationFieldOptionRepository optionRepository;
    private final EventTeamRepository teamRepository;

    /** Initializes only the participant's draft. Team choice and answers are separate resumable steps. */
    @Transactional
    public ApiResponse<RegistrationResponse> registerForEvent(Integer eventId, RegistrationRequest ignored) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (event.getApprovalStatus() != ApprovalStatus.VERIFIED
                || event.getEventState() == EventState.CANCELLED
                || event.getRegistrationType() != RegistrationType.THROUGH_APP)
            return ApiResponse.failure("This event is not accepting in-app registrations.");
        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        Instant now = Instant.now();
        if (config.getRegistrationCreatedAt() != null && now.isBefore(config.getRegistrationCreatedAt()))
            return ApiResponse.failure("Registration has not opened yet.");
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt()))
            return ApiResponse.failure("Registration has closed.");

        Optional<EventRegistration> existing = registrationRepository.findByEventIdAndUserId(eventId, user.getId());
        if (existing.isPresent()) {
            EventRegistration r = existing.get();
            if (r.getStatus() == RegistrationStatus.REJECTED || r.getStatus() == RegistrationStatus.CANCELLED)
                return ApiResponse.failure("This registration is no longer active.");
            return ApiResponse.success(toResponse(r), "Existing registration returned.");
        }
        EventRegistration registration = registrationRepository.save(EventRegistration.builder()
                .event(event).user(user).status(RegistrationStatus.PENDING).submittedAt(now).build());
        return ApiResponse.success(toResponse(registration), "Registration draft created.");
    }

    public ApiResponse<List<RegistrationFieldResponse>> getPublishedFields(Integer eventId, Boolean individual) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (event.getApprovalStatus() != ApprovalStatus.VERIFIED) return ApiResponse.failure("Event is not published.");
        List<RegistrationFieldResponse> fields = fieldRepository
                .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(eventId).stream()
                .filter(f -> individual == null || Boolean.valueOf(individual).equals(f.getIndividual()))
                .map(f -> RegistrationFieldResponse.builder().id(f.getId()).fieldLabel(f.getFieldLabel())
                        .fieldType(f.getFieldType()).required(f.getRequired()).placeholder(f.getPlaceholder())
                        .fieldOrder(f.getFieldOrder()).individual(f.getIndividual())
                        .options(optionRepository.findByFieldIdOrderByOptionOrderAsc(f.getId()).stream()
                                .map(o -> RegistrationFieldResponse.Option.builder().id(o.getId())
                                        .optionValue(o.getOptionValue()).optionOrder(o.getOptionOrder()).build()).toList())
                        .build()).toList();
        return ApiResponse.success(fields, "Registration fields fetched.");
    }

    public ApiResponse<List<AvailableTeamResponse>> getAvailableTeams(Integer eventId, String search) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1) return ApiResponse.failure("Teams are not available for this event.");
        String query = search == null ? "" : search.trim().toLowerCase();
        List<AvailableTeamResponse> teams = teamRepository.findAllByEvent_Id(eventId).stream()
                .filter(team -> team.getTeamName().toLowerCase().contains(query))
                .map(team -> {
                    int count = (int) registrationRepository.countByEventIdAndTeamId(eventId, team.getId());
                    return AvailableTeamResponse.builder().teamId(team.getId()).teamName(team.getTeamName())
                            .memberCount(count).capacity(config.getMaxTeamMembers())
                            .leaderName(team.getLeader() == null ? null : team.getLeader().getUsername()).build();
                }).filter(team -> team.getMemberCount() < team.getCapacity()).toList();
        return ApiResponse.success(teams, "Available teams fetched.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> saveAnswers(Integer registrationId, SaveRegistrationAnswersRequest request) {
        if (request == null || request.getType() == null)
            return ApiResponse.failure("Answer type is required.");

        User user = currentParticipant();
        if (user == null || registrationId == null)
            return ApiResponse.failure("Editable registration not found.");
        EventRegistration registration = registrationRepository.findById(registrationId).orElse(null);
        if (registration == null || registration.getStatus() != RegistrationStatus.PENDING)
            return ApiResponse.failure("Editable registration not found.");

        boolean individual = request.getType() == AnswerType.INDIVIDUAL;
        if (individual) {
            if (!registration.getUser().getId().equals(user.getId()))
                return ApiResponse.failure("Editable registration not found.");
        } else {
            EventTeam assignedTeam = registration.getTeam();
            if (assignedTeam == null || assignedTeam.getLeader() == null
                    || !assignedTeam.getLeader().getId().equals(user.getId()))
                return ApiResponse.failure("Only the assigned team's leader can save team answers.");
        }

        List<SaveRegistrationAnswersRequest.Answer> submitted = request.getAnswers() == null
                ? List.of() : request.getAnswers();
        List<RegistrationField> fields = fieldRepository
                .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(registration.getEvent().getId());
        // Validate the whole batch before writing anything so an invalid later field cannot
        // leave a partially applied answer update.
        for (SaveRegistrationAnswersRequest.Answer answer : submitted) {
            if (answer == null || answer.getFieldId() == null) return ApiResponse.failure("Every answer requires a fieldId.");
            RegistrationField field = fields.stream().filter(f -> f.getId().equals(answer.getFieldId())).findFirst().orElse(null);
            if (field == null || Boolean.TRUE.equals(field.getIndividual()) != individual)
                return ApiResponse.failure("Invalid registration field ID: " + answer.getFieldId());
            String value = answer.getAnswer();
            if (value == null || value.isBlank()) {
                if (Boolean.TRUE.equals(field.getRequired())) return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
                continue;
            }
            if (value.length() > 10000) return ApiResponse.failure("Answer is too long for " + field.getFieldLabel());
            String type = field.getFieldType() == null ? "" : field.getFieldType().toUpperCase();
            try {
                if (type.equals("NUMBER")) new java.math.BigDecimal(value);
                if (type.equals("DATE")) LocalDate.parse(value);
            } catch (NumberFormatException | DateTimeParseException invalid) {
                return ApiResponse.failure("Answer has an invalid format for " + field.getFieldLabel());
            }
            if (type.equals("SELECT") || type.equals("MULTISELECT") || type.contains("DROPDOWN") || type.contains("CHOICE")) {
                List<String> allowed = optionRepository.findByFieldIdOrderByOptionOrderAsc(field.getId()).stream()
                        .map(RegistrationFieldOption::getOptionValue).toList();
                List<String> chosen = type.equals("MULTISELECT") ? List.of(value.split(",")) : List.of(value);
                if (chosen.stream().map(String::trim).anyMatch(v -> !allowed.contains(v)))
                    return ApiResponse.failure("Answer is not an allowed option for " + field.getFieldLabel());
            }
        }
        for (SaveRegistrationAnswersRequest.Answer answer : submitted) {
            if (answer == null || answer.getFieldId() == null) return ApiResponse.failure("Every answer requires a fieldId.");
            RegistrationField field = fields.stream().filter(f -> f.getId().equals(answer.getFieldId())).findFirst().orElse(null);
            if (field == null || Boolean.TRUE.equals(field.getIndividual()) != individual)
                return ApiResponse.failure("Invalid registration field ID: " + answer.getFieldId());
            String value = answer.getAnswer();
            if (value == null || value.isBlank()) {
                if (Boolean.TRUE.equals(field.getRequired())) return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
                answerRepository.findById(new RegistrationAnswerId(registrationId, field.getId())).ifPresent(answerRepository::delete);
                continue;
            }
            if (value.length() > 10000) return ApiResponse.failure("Answer is too long for " + field.getFieldLabel());
            RegistrationAnswerId id = new RegistrationAnswerId(registrationId, field.getId());
            RegistrationAnswer persisted = answerRepository.findById(id).orElseGet(() -> RegistrationAnswer.builder()
                    .id(id).registration(registration).field(field).build());
            persisted.setAnswer(value);
            answerRepository.save(persisted);
        }
        return ApiResponse.success(toResponse(registration), "Answers saved.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> createTeam(Integer registrationId, String teamName) {
        EventRegistration r = ownedEditable(registrationId);
        if (r == null) return ApiResponse.failure("Editable registration not found.");
        RegistrationConfig config = configRepository.findByEventId(r.getEvent().getId()).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1) return ApiResponse.failure("Teams are not allowed for this event.");
        if (teamName == null || teamName.isBlank() || teamName.length() > 100) return ApiResponse.failure("A valid team name is required.");
        if (teamRepository.findByEvent_IdAndTeamName(r.getEvent().getId(), teamName.trim()).isPresent())
            return ApiResponse.failure("Team name is already taken for this event.");
        EventTeam team = teamRepository.save(EventTeam.builder().event(r.getEvent()).teamName(teamName.trim()).leader(r.getUser()).build());
        r.setTeam(team);
        return ApiResponse.success(toResponse(r), "Team created and leader membership established.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> finalizeRegistration(Integer registrationId) {
        EventRegistration r = ownedEditable(registrationId);
        if (r == null) return ApiResponse.failure("Editable registration not found.");
        RegistrationConfig config = configRepository.findByEventId(r.getEvent().getId()).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        Instant now = Instant.now();
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt())) return ApiResponse.failure("Registration has closed.");
        boolean solo = r.getTeam() == null;
        if (solo && config.getMinTeamMembers() > 1) return ApiResponse.failure("Solo registrations are not allowed for this event.");
        if (!solo) {
            long size = registrationRepository.countByEventIdAndTeamId(r.getEvent().getId(), r.getTeam().getId());
            if (size < config.getMinTeamMembers() || size > config.getMaxTeamMembers()) return ApiResponse.failure("Team size does not meet event requirements.");
        }
        List<RegistrationAnswer> saved = answerRepository.findByIdRegistrationId(registrationId);
        for (RegistrationField field : fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(r.getEvent().getId())) {
            if (!Boolean.TRUE.equals(field.getRequired())) continue;
            boolean applies = Boolean.TRUE.equals(field.getIndividual()) || !solo;
            if (applies && saved.stream().noneMatch(a -> a.getField().getId().equals(field.getId()) && a.getAnswer() != null && !a.getAnswer().isBlank()))
                return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
        }
        if (r.getStatus() == RegistrationStatus.COMPLETE) return ApiResponse.success(toResponse(r), "Registration is already complete.");
        r.setStatus(RegistrationStatus.COMPLETE);
        return ApiResponse.success(toResponse(r), "Registration completed.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> getRegistration(Integer eventId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        if (!eventRepository.existsById(eventId)) return ApiResponse.failure("Event not found.");
        return registrationRepository.findByEventIdAndUserId(eventId, user.getId())
                .map(r -> ApiResponse.success(toParticipantResponse(r, user), "Registration found."))
                .orElseGet(() -> ApiResponse.success(RegistrationResponse.builder().eventId(eventId)
                        .canResume(false).answers(List.of()).build(), "No registration found."));
    }

    public ApiResponse<List<RegistrationResponse>> getMyRegistrations() {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        return ApiResponse.success(registrationRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList(), "Registrations fetched.");
    }

    @Transactional
    public ApiResponse<Void> cancelRegistration(Integer eventId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventRegistration r = registrationRepository.findByEventIdAndUserId(eventId, user.getId()).orElse(null);
        if (r == null) return ApiResponse.failure("You are not registered for this event.");
        if (r.getStatus() == RegistrationStatus.COMPLETE || r.getStatus() == RegistrationStatus.CONFIRMED)
            return ApiResponse.failure("Completed registrations cannot be cancelled here.");
        r.setStatus(RegistrationStatus.CANCELLED);
        return ApiResponse.success(null, "Registration cancelled.");
    }

    private EventRegistration ownedEditable(Integer id) {
        User user = currentParticipant();
        if (user == null || id == null) return null;
        EventRegistration r = registrationRepository.findById(id).orElse(null);
        return r != null && r.getUser().getId().equals(user.getId())
                && r.getStatus() == RegistrationStatus.PENDING ? r : null;
    }

    private User currentParticipant() {
        try {
            return SecurityUtils.getCurrentUser();
        } catch (IllegalStateException unauthenticated) {
            return null;
        }
    }

    private RegistrationResponse toResponse(EventRegistration r) {
        return RegistrationResponse.builder().registrationId(r.getId()).eventId(r.getEvent().getId())
                .status(r.getStatus().name()).teamId(r.getTeam() == null ? null : r.getTeam().getId())
                .teamName(r.getTeam() == null ? null : r.getTeam().getTeamName())
                .canResume(r.getStatus() == RegistrationStatus.PENDING).build();
    }

    private RegistrationResponse toParticipantResponse(EventRegistration registration, User participant) {
        List<SavedRegistrationAnswerResponse> answers = answerRepository
                .findByIdRegistrationId(registration.getId()).stream()
                .filter(saved -> Boolean.TRUE.equals(saved.getField().getIndividual())
                        || isAssignedTeamLeader(registration, participant))
                .map(saved -> SavedRegistrationAnswerResponse.builder()
                        .fieldId(saved.getField().getId())
                        .answer(saved.getAnswer())
                        .type(Boolean.TRUE.equals(saved.getField().getIndividual()) ? "INDIVIDUAL" : "TEAM")
                        .build())
                .toList();
        return RegistrationResponse.builder().registrationId(registration.getId())
                .eventId(registration.getEvent().getId()).status(registration.getStatus().name())
                .teamId(registration.getTeam() == null ? null : registration.getTeam().getId())
                .teamName(registration.getTeam() == null ? null : registration.getTeam().getTeamName())
                .canResume(registration.getStatus() == RegistrationStatus.PENDING)
                .answers(answers).build();
    }

    private boolean isAssignedTeamLeader(EventRegistration registration, User participant) {
        EventTeam team = registration.getTeam();
        return team != null && team.getLeader() != null
                && team.getLeader().getId().equals(participant.getId());
    }
}
