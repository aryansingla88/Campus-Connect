package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.registration.constants.QuickFieldMask;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationConfigRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationFieldRequest;
import com.campus.Campus_Connect.features.registration.dto.response.*;
import com.campus.Campus_Connect.features.registration.entity.*;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class HostRegistrationService {

    private final EventRepository eventRepository;
    private final RegistrationConfigRepository configRepository;
    private final EventRegistrationRepository registrationRepository;
    private final RegistrationAnswerRepository answerRepository;
    private final RegistrationFieldRepository fieldRepository;
    private final RegistrationFieldOptionRepository optionRepository;

    // ============================================================
    // CONFIGURATION
    // ============================================================

    public ApiResponse<RegistrationConfigResponse> getRegistrationConfig(Integer eventId) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (!isOrganizer(event)) return ApiResponse.failure("Only the event organizer can view configuration.");

        Optional<RegistrationConfig> configOpt = configRepository.findByEventId(eventId);
        if (configOpt.isEmpty()) {
            return ApiResponse.failure("Registration configuration not found for this event.");
        }
        return ApiResponse.success(toConfigResponse(configOpt.get()), "Registration configuration fetched successfully.");
    }

    @Transactional
    public ApiResponse<RegistrationConfigResponse> createRegistrationConfig(Integer eventId, RegistrationConfigRequest request) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (!isOrganizer(event)) return ApiResponse.failure("Only the event organizer can create configuration.");

        if (configRepository.findByEventId(eventId).isPresent()) {
            return ApiResponse.failure("Registration configuration already exists for this event. Use PUT to update.");
        }

        String valErr = validateConfigRequest(request);
        if (valErr != null) return ApiResponse.failure(valErr);

        RegistrationConfig config = RegistrationConfig.builder()
                .event(event)
                .minTeamMembers(request.getMinTeamMembers())
                .maxTeamMembers(request.getMaxTeamMembers())
                .quickFieldMask(request.getQuickFieldMask() != null ? request.getQuickFieldMask() : 0L)
                .registrationCreatedAt(request.getRegistrationCreatedAt())
                .registrationEndAt(request.getRegistrationEndAt())
                .build();

        config = configRepository.save(config);
        return ApiResponse.success(toConfigResponse(config), "Registration configuration created successfully.");
    }

    @Transactional
    public ApiResponse<RegistrationConfigResponse> updateRegistrationConfig(Integer eventId, RegistrationConfigRequest request) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (!isOrganizer(event)) return ApiResponse.failure("Only the event organizer can update configuration.");

        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null) {
            return ApiResponse.failure("Registration configuration not found. Use POST to create.");
        }

        String valErr = validateConfigRequest(request);
        if (valErr != null) return ApiResponse.failure(valErr);

        config.setMinTeamMembers(request.getMinTeamMembers());
        config.setMaxTeamMembers(request.getMaxTeamMembers());
        config.setQuickFieldMask(request.getQuickFieldMask() != null ? request.getQuickFieldMask() : 0L);
        config.setRegistrationCreatedAt(request.getRegistrationCreatedAt());
        config.setRegistrationEndAt(request.getRegistrationEndAt());

        configRepository.save(config);
        return ApiResponse.success(toConfigResponse(config), "Registration configuration updated successfully.");
    }

    private String validateConfigRequest(RegistrationConfigRequest request) {
        if (request.getMinTeamMembers() == null || request.getMinTeamMembers() < 1) {
            return "minTeamMembers must be greater than or equal to 1.";
        }
        if (request.getMaxTeamMembers() == null || request.getMaxTeamMembers() < request.getMinTeamMembers()) {
            return "maxTeamMembers must be greater than or equal to minTeamMembers.";
        }
        if (!QuickFieldMask.isValidMask(request.getQuickFieldMask())) {
            return "Invalid quickFieldMask provided.";
        }
        return null;
    }

// ============================================================
// FIELDS
// ============================================================

    public ApiResponse<List<RegistrationFieldResponse>> getRegistrationFields(
            Integer eventId) {

        if (!eventRepository.existsById(eventId)) {
            return ApiResponse.failure("Event not found.");
        }

        List<RegistrationFieldResponse> response =
                fieldRepository
                        .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(eventId)
                        .stream()
                        .map(this::toFieldResponse)
                        .toList();

        return ApiResponse.success(
                response,
                "Registration fields fetched successfully."
        );
    }

    @Transactional
    public ApiResponse<List<RegistrationFieldResponse>> createRegistrationFields(
            Integer eventId,
            List<RegistrationFieldRequest> requests) {

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!isOrganizer(event)) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registration fields."
            );
        }

        if (requests == null) {
            return ApiResponse.failure("Registration fields are required.");
        }

        List<RegistrationField> savedFields = new java.util.ArrayList<>();

        for (RegistrationFieldRequest request : requests) {

            String validationError = validateFieldRequest(request);

            if (validationError != null) {
                return ApiResponse.failure(validationError);
            }

            RegistrationField field = RegistrationField.builder()
                    .event(event)
                    .fieldLabel(request.getFieldLabel())
                    .fieldType(request.getFieldType())
                    .required(
                            request.getRequired() != null
                                    ? request.getRequired()
                                    : false
                    )
                    .placeholder(request.getPlaceholder())
                    .fieldOrder(
                            request.getFieldOrder() != null
                                    ? request.getFieldOrder()
                                    : 0
                    )
                    .individual(
                            request.getIndividual() != null
                                    ? request.getIndividual()
                                    : true
                    )
                    .build();

            field = fieldRepository.save(field);

            saveOptions(field, request.getOptions());

            savedFields.add(field);
        }

        return ApiResponse.success(
                savedFields.stream()
                        .map(this::toFieldResponse)
                        .toList(),
                "Registration fields created successfully."
        );
    }

    @Transactional
    public ApiResponse<List<RegistrationFieldResponse>> updateRegistrationFields(
            Integer eventId,
            List<RegistrationFieldRequest> requests) {

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!isOrganizer(event)) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registration fields."
            );
        }

        if (requests == null) {
            return ApiResponse.failure("Registration fields are required.");
        }

        List<RegistrationField> existingFields =
                fieldRepository
                        .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(eventId);

        Set<Integer> submittedIds = new HashSet<>();
        List<RegistrationField> savedFields = new java.util.ArrayList<>();

        for (RegistrationFieldRequest request : requests) {

            String validationError = validateFieldRequest(request);

            if (validationError != null) {
                return ApiResponse.failure(validationError);
            }

            RegistrationField field;

            if (request.getId() != null) {

                field = fieldRepository
                        .findByIdAndEventIdAndDeletedAtIsNull(
                                request.getId(),
                                eventId
                        )
                        .orElse(null);

                if (field == null) {
                    return ApiResponse.failure(
                            "Registration field not found: "
                                    + request.getId()
                    );
                }

                submittedIds.add(field.getId());

            } else {

                field = RegistrationField.builder()
                        .event(event)
                        .build();
            }

            field.setFieldLabel(request.getFieldLabel());
            field.setFieldType(request.getFieldType());
            field.setRequired(
                    request.getRequired() != null
                            ? request.getRequired()
                            : false
            );
            field.setPlaceholder(request.getPlaceholder());
            field.setFieldOrder(
                    request.getFieldOrder() != null
                            ? request.getFieldOrder()
                            : 0
            );
            field.setIndividual(
                    request.getIndividual() != null
                            ? request.getIndividual()
                            : true
            );
            field.setDeletedAt(null);

            field = fieldRepository.save(field);

            optionRepository.deleteAll(
                    optionRepository.findByFieldIdOrderByOptionOrderAsc(
                            field.getId()
                    )
            );

            saveOptions(field, request.getOptions());

            savedFields.add(field);
        }

        // Anything that existed before but is no longer submitted
        // is soft deleted.
        for (RegistrationField existing : existingFields) {

            if (!submittedIds.contains(existing.getId())) {

                boolean stillSubmitted =
                        requests.stream()
                                .anyMatch(request ->
                                        request.getId() != null
                                                && request.getId().equals(existing.getId())
                                );

                if (!stillSubmitted) {
                    existing.setDeletedAt(LocalDateTime.now());
                    fieldRepository.save(existing);
                }
            }
        }

        return ApiResponse.success(
                savedFields.stream()
                        .map(this::toFieldResponse)
                        .toList(),
                "Registration fields updated successfully."
        );
    }

    private String validateFieldRequest(
            RegistrationFieldRequest request) {

        if (request == null) {
            return "Registration field cannot be null.";
        }

        if (request.getFieldLabel() == null
                || request.getFieldLabel().isBlank()) {
            return "Field label is required.";
        }

        if (request.getFieldType() == null
                || request.getFieldType().isBlank()) {
            return "Field type is required.";
        }

        return null;
    }

    // ============================================================
    // REGISTRATION RESPONSES
    // ============================================================


    /**
     * Short response used by the host registration list.
     * Important:
     * - Does not fetch registration answers.
     * - Team registrations are represented once, by the team leader.
     * - Individual registrations are represented directly.
     */
    public ApiResponse<List<RegistrationShortResponse>> getEventRegistrations(
            Integer eventId) {

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!isOrganizer(event)) {
            return ApiResponse.failure(
                    "Only the event organizer can view registrations."
            );
        }

        List<EventRegistration> registrations =
                registrationRepository.findShortRegistrations(eventId);

        List<RegistrationShortResponse> response =
                registrations.stream()
                        .map(this::toShortResponse)
                        .toList();

        return ApiResponse.success(
                response,
                "Event registrations fetched successfully."
        );
    }

    /**
     * Detailed response for one clicked registration.
     *
     * Team registration:
     *   team answers
     *   members[]
     *       quickFields[]
     *       individualAnswers[]
     *
     * Individual registration:
     *   team = null
     *   members[0]
     *       quickFields[]
     *       individualAnswers[]
     */
    public ApiResponse<RegistrationDetailResponse> getEventRegistration(
            Integer eventId,
            Integer registrationId) {

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!isOrganizer(event)) {
            return ApiResponse.failure(
                    "Only the event organizer can view registrations."
            );
        }

        EventRegistration registration =
                registrationRepository
                        .findByIdAndEventId(registrationId, eventId)
                        .orElse(null);

        if (registration == null) {
            return ApiResponse.failure("Registration not found.");
        }

        return ApiResponse.success(
                toDetailResponse(registration),
                "Registration fetched successfully."
        );
    }

    private RegistrationShortResponse toShortResponse(
            EventRegistration registration) {

        EventTeam team = registration.getTeam();

        if (team != null) {
            long memberCount =
                    registrationRepository.countByEventIdAndTeamId(
                            registration.getEvent().getId(),
                            team.getId()
                    );

            return RegistrationShortResponse.builder()
                    .registrationId(registration.getId())
                    .type("TEAM")
                    .teamName(team.getTeamName())
                    .memberCount((int) memberCount)
                    .leaderEmail(
                            team.getLeader() != null
                                    ? team.getLeader().getEmail()
                                    : null
                    )
                    .status(registration.getStatus().name())
                    .submittedAt(registration.getSubmittedAt())
                    .build();
        }

        User user = registration.getUser();
        UserProfile profile =
                user != null ? user.getProfile() : null;

        return RegistrationShortResponse.builder()
                .registrationId(registration.getId())
                .type("INDIVIDUAL")
                .name(
                        profile != null && profile.getFullName() != null
                                ? profile.getFullName()
                                : user != null
                                ? user.getUsername()
                                : null
                )
                .email(user != null ? user.getEmail() : null)
                .status(registration.getStatus().name())
                .build();
    }

    private RegistrationDetailResponse toDetailResponse(
            EventRegistration registration) {

        EventTeam team = registration.getTeam();

        List<EventRegistration> members;

        if (team != null) {
            members =
                    registrationRepository.findByEventIdAndTeamId(
                            registration.getEvent().getId(),
                            team.getId()
                    );
        } else {
            members = List.of(registration);
        }

        List<RegistrationDetailResponse.Member> memberResponses =
                members.stream()
                        .map(this::toMemberResponse)
                        .toList();

        RegistrationDetailResponse.Team teamResponse = null;

        if (team != null) {

            EventRegistration leaderRegistration =
                    members.stream()
                            .filter(member ->
                                    member.getUser() != null
                                            && team.getLeader() != null
                                            && team.getLeader().getId()
                                            .equals(member.getUser().getId()))
                            .findFirst()
                            .orElse(null);

            List<RegistrationDetailResponse.Answer> teamAnswers =
                    leaderRegistration == null
                            ? List.<RegistrationDetailResponse.Answer>of()
                            : answerRepository
                            .findByIdRegistrationId(
                                    leaderRegistration.getId()
                            )
                            .stream()
                            .filter(answer ->
                                    Boolean.FALSE.equals(
                                            answer.getField().getIndividual()
                                    ))
                            .map(this::toAnswerResponse)
                            .toList();

            teamResponse =
                    RegistrationDetailResponse.Team.builder()
                            .teamId(team.getId())
                            .teamName(team.getTeamName())
                            .memberCount(members.size())
                            .leader(
                                    leaderRegistration != null
                                            ? toMemberResponse(
                                            leaderRegistration
                                    )
                                            : null
                            )
                            .answers(teamAnswers)
                            .build();
        }

        return RegistrationDetailResponse.builder()
                .registrationId(registration.getId())
                .eventId(registration.getEvent().getId())
                .type(team != null ? "TEAM" : "INDIVIDUAL")
                .status(registration.getStatus().name())
                .submittedAt(registration.getSubmittedAt())
                .team(teamResponse)
                .members(memberResponses)
                .build();
    }

    private RegistrationDetailResponse.Member toMemberResponse(
            EventRegistration registration) {

        if (registration == null || registration.getUser() == null) {
            return null;
        }

        User user = registration.getUser();
        UserProfile profile = user.getProfile();

        EventTeam team = registration.getTeam();

        boolean leader =
                team != null
                        && team.getLeader() != null
                        && team.getLeader().getId().equals(user.getId());

        List<RegistrationDetailResponse.QuickField> quickFields =
                getQuickFields(
                        user,
                        registration.getEvent().getId()
                );

        List<RegistrationDetailResponse.Answer> individualAnswers =
                answerRepository
                        .findByIdRegistrationId(registration.getId())
                        .stream()
                        .filter(answer ->
                                Boolean.TRUE.equals(
                                        answer.getField().getIndividual()
                                ))
                        .map(this::toAnswerResponse)
                        .toList();

        return RegistrationDetailResponse.Member.builder()
                .registrationId(registration.getId())
                .userId(user.getId())
                .name(
                        profile != null && profile.getFullName() != null
                                ? profile.getFullName()
                                : user.getUsername()
                )
                .email(user.getEmail())
                .leader(leader)
                .quickFields(quickFields)
                .individualAnswers(individualAnswers)
                .build();
    }

    private RegistrationDetailResponse.Answer toAnswerResponse(
            RegistrationAnswer answer) {

        return RegistrationDetailResponse.Answer.builder()
                .fieldId(answer.getField().getId())
                .fieldLabel(answer.getField().getFieldLabel())
                .fieldType(answer.getField().getFieldType())
                .answer(answer.getAnswer())
                .build();
    }

    private List<RegistrationDetailResponse.QuickField> getQuickFields(
            User user,
            Integer eventId) {

        RegistrationConfig config =
                configRepository.findByEventId(eventId).orElse(null);

        if (config == null || config.getQuickFieldMask() == null) {
            return List.of();
        }

        UserProfile profile = user.getProfile();

        if (profile == null) {
            return List.of();
        }

        long mask = config.getQuickFieldMask();

        List<RegistrationDetailResponse.QuickField> result =
                new ArrayList<>();

        addQuickField(
                result,
                mask,
                QuickFieldMask.FULL_NAME,
                "FULL_NAME",
                profile.getFullName()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.EMAIL,
                "EMAIL",
                user.getEmail()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.PHONE,
                "PHONE",
                profile.getPhone()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.COURSE,
                "COURSE",
                profile.getCourseId() != null
                        ? String.valueOf(profile.getCourseId())
                        : null
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.YEAR_BATCH,
                "YEAR_BATCH",
                profile.getAdmissionYear() != null
                        ? String.valueOf(profile.getAdmissionYear())
                        : null
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.ROLL_NUMBER,
                "ROLL_NUMBER",
                profile.getRollNumber()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.HOSTEL,
                "HOSTEL",
                profile.getHostel()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.HOMETOWN,
                "HOMETOWN",
                profile.getHometown()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.GENDER,
                "GENDER",
                profile.getGender()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.DATE_OF_BIRTH,
                "DATE_OF_BIRTH",
                profile.getDob() != null
                        ? profile.getDob().toString()
                        : null
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.GITHUB,
                "GITHUB",
                profile.getGithub()
        );

        addQuickField(
                result,
                mask,
                QuickFieldMask.LINKEDIN,
                "LINKEDIN",
                profile.getLinkedin()
        );

        return result;
    }

    private void addQuickField(
            List<RegistrationDetailResponse.QuickField> result,
            long mask,
            long bit,
            String field,
            String value) {

        if ((mask & bit) != 0 && value != null) {
            result.add(
                    RegistrationDetailResponse.QuickField.builder()
                            .field(field)
                            .value(value)
                            .build()
            );
        }
    }

    // ============================================================
    // STATUS APPROVAL / REJECTION
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationResponse> approveRegistration(Integer eventId, Integer registrationId) {
        return updateStatus(eventId, registrationId, RegistrationStatus.CONFIRMED);
    }

    @Transactional
    public ApiResponse<RegistrationResponse> rejectRegistration(Integer eventId, Integer registrationId) {
        return updateStatus(eventId, registrationId, RegistrationStatus.REJECTED);
    }

    private ApiResponse<RegistrationResponse> updateStatus(Integer eventId, Integer registrationId, RegistrationStatus targetStatus) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (!isOrganizer(event)) return ApiResponse.failure("Only the event organizer can manage registrations.");

        EventRegistration registration = registrationRepository.findByIdAndEventId(registrationId, eventId).orElse(null);
        if (registration == null) return ApiResponse.failure("Registration not found.");

        if (registration.getStatus() == targetStatus) {
            return ApiResponse.failure("Registration is already " + targetStatus.name());
        }

        registration.setStatus(targetStatus);
        registrationRepository.save(registration);
        return ApiResponse.success(
                RegistrationResponse.builder()
                        .registrationId(registration.getId())
                        .registered(true)
                        .status(targetStatus.name())
                        .build(),
                "Registration status updated to " + targetStatus.name() + " successfully.");
    }

    // ============================================================
    // PRIVATE HELPERS
    // ============================================================

    private boolean isOrganizer(Event event) {
        User currentUser = SecurityUtils.getCurrentUser();
        return currentUser != null && event.getCreator() != null && currentUser.getId().equals(event.getCreator().getId());
    }

    private void saveOptions(RegistrationField field, List<RegistrationFieldRequest.Option> options) {
        if (options == null) return;
        for (RegistrationFieldRequest.Option req : options) {
            optionRepository.save(RegistrationFieldOption.builder()
                    .field(field)
                    .optionValue(req.getOptionValue())
                    .optionOrder(req.getOptionOrder() != null ? req.getOptionOrder() : 0)
                    .build());
        }
    }

    private RegistrationConfigResponse toConfigResponse(RegistrationConfig config) {
        return RegistrationConfigResponse.builder()
                .eventId(config.getEventId())
                .minTeamMembers(config.getMinTeamMembers())
                .maxTeamMembers(config.getMaxTeamMembers())
                .quickFieldMask(config.getQuickFieldMask())
                .registrationCreatedAt(config.getRegistrationCreatedAt())
                .registrationEndAt(config.getRegistrationEndAt())
                .build();
    }

    private RegistrationFieldResponse toFieldResponse(RegistrationField field) {
        List<RegistrationFieldResponse.Option> options = optionRepository.findByFieldIdOrderByOptionOrderAsc(field.getId())
                .stream().map(opt -> RegistrationFieldResponse.Option.builder()
                        .id(opt.getId())
                        .optionValue(opt.getOptionValue())
                        .optionOrder(opt.getOptionOrder())
                        .build()).toList();

        return RegistrationFieldResponse.builder()
                .id(field.getId())
                .fieldLabel(field.getFieldLabel())
                .fieldType(field.getFieldType())
                .required(field.getRequired())
                .placeholder(field.getPlaceholder())
                .fieldOrder(field.getFieldOrder())
                .individual(field.getIndividual())
                .options(options)
                .build();
    }

}
