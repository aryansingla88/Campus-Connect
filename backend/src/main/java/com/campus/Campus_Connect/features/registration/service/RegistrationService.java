package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationAnswerRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.*;
import com.campus.Campus_Connect.features.registration.entity.*;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository registrationRepository;
    private final RegistrationFieldRepository fieldRepository;
    private final RegistrationAnswerRepository answerRepository;


    // ============================================================
    // REGISTER + ANSWERS
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationResponse> registerForEvent(
            Integer eventId,
            RegistrationRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (registrationRepository.existsByEventIdAndUserId(
                eventId,
                currentUser.getId())) {

            return ApiResponse.failure(
                    "You are already registered for this event."
            );
        }

        List<RegistrationField> fields =
                fieldRepository.findByEventIdOrderByFieldOrderAsc(eventId);

        List<RegistrationAnswerRequest> submittedAnswers =
                request.getAnswers() == null
                        ? List.of()
                        : request.getAnswers();


        // Validate required fields
        for (RegistrationField field : fields) {

            if (!field.getRequired()) {
                continue;
            }

            boolean answered =
                    submittedAnswers.stream()
                            .anyMatch(answer ->
                                    answer.getFieldId().equals(field.getId())
                                            && answer.getAnswer() != null
                                            && !answer.getAnswer().isBlank()
                            );

            if (!answered) {
                return ApiResponse.failure(
                        "Required field missing: "
                                + field.getFieldLabel()
                );
            }
        }


        // Validate submitted field IDs belong to this event
        for (RegistrationAnswerRequest submitted : submittedAnswers) {

            boolean validField =
                    fields.stream()
                            .anyMatch(field ->
                                    field.getId().equals(
                                            submitted.getFieldId()
                                    )
                            );

            if (!validField) {
                return ApiResponse.failure(
                        "Invalid registration field."
                );
            }
        }


        EventRegistration registration =
                EventRegistration.builder()
                        .event(event)
                        .user(currentUser)
                        .status(RegistrationStatus.CONFIRMED)
                        .submittedAt(Instant.now())
                        .build();

        registration = registrationRepository.save(registration);


        // Save answers
        for (RegistrationAnswerRequest submitted : submittedAnswers) {

            RegistrationAnswer answer =
                    RegistrationAnswer.builder()
                            .id(
                                    new RegistrationAnswerId(
                                            registration.getId(),
                                            submitted.getFieldId()
                                    )
                            )
                            .registration(registration)
                            .field(
                                    fieldRepository.findById(
                                            submitted.getFieldId()
                                    ).orElseThrow()
                            )
                            .answer(submitted.getAnswer())
                            .build();

            answerRepository.save(answer);
        }


        RegistrationResponse response =
                RegistrationResponse.builder()
                        .registrationId(registration.getId())
                        .registered(true)
                        .status(registration.getStatus().name())
                        .build();

        return ApiResponse.success(
                response,
                "Registered successfully."
        );
    }


    // ============================================================
    // GET MY REGISTRATION FOR EVENT
    // ============================================================

    public ApiResponse<RegistrationResponse> getRegistration(
            Integer eventId) {

        User currentUser = SecurityUtils.getCurrentUser();

        if (!eventRepository.existsById(eventId)) {
            return ApiResponse.failure("Event not found.");
        }

        Optional<EventRegistration> registration =
                registrationRepository.findByEventIdAndUserId(
                        eventId,
                        currentUser.getId()
                );

        if (registration.isEmpty()) {

            return ApiResponse.success(
                    RegistrationResponse.builder()
                            .registered(false)
                            .build(),
                    "You are not registered for this event."
            );
        }

        EventRegistration data = registration.get();

        return ApiResponse.success(
                RegistrationResponse.builder()
                        .registrationId(data.getId())
                        .registered(true)
                        .status(data.getStatus().name())
                        .build(),
                "Registration found."
        );
    }


    // ============================================================
    // CANCEL MY REGISTRATION
    // ============================================================

    @Transactional
    public ApiResponse<Void> cancelRegistration(
            Integer eventId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Optional<EventRegistration> registration =
                registrationRepository.findByEventIdAndUserId(
                        eventId,
                        currentUser.getId()
                );

        if (registration.isEmpty()) {
            return ApiResponse.failure(
                    "You are not registered for this event."
            );
        }

        registrationRepository.delete(registration.get());

        return ApiResponse.success(
                null,
                "Registration cancelled successfully."
        );
    }


    // ============================================================
    // MY REGISTRATIONS
    // ============================================================

    public ApiResponse<List<RegistrationResponse>>
    getMyRegistrations() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<EventRegistration> registrations =
                registrationRepository.findByUserId(
                        currentUser.getId()
                );

        List<RegistrationResponse> response =
                registrations.stream()
                        .map(registration ->
                                RegistrationResponse.builder()
                                        .registrationId(
                                                registration.getId()
                                        )
                                        .registered(true)
                                        .status(
                                                registration
                                                        .getStatus()
                                                        .name()
                                        )
                                        .build()
                        )
                        .toList();

        return ApiResponse.success(
                response,
                "Registrations fetched successfully."
        );
    }


    // ============================================================
    // ORGANIZER: GET ALL REGISTRATIONS
    // ============================================================

    public ApiResponse<List<RegistrationDetailResponse>>
    getEventRegistrations(Integer eventId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can view registrations."
            );
        }

        List<EventRegistration> registrations =
                registrationRepository.findByEventId(eventId);

        List<RegistrationDetailResponse> response =
                registrations.stream()
                        .map(this::toDetailResponse)
                        .toList();

        return ApiResponse.success(
                response,
                "Event registrations fetched successfully."
        );
    }


    // ============================================================
    // ORGANIZER: GET ONE REGISTRATION
    // ============================================================

    public ApiResponse<RegistrationDetailResponse>
    getEventRegistration(
            Integer eventId,
            Integer registrationId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can view registrations."
            );
        }

        EventRegistration registration =
                registrationRepository
                        .findByIdAndEventId(
                                registrationId,
                                eventId
                        )
                        .orElse(null);

        if (registration == null) {
            return ApiResponse.failure(
                    "Registration not found."
            );
        }

        return ApiResponse.success(
                toDetailResponse(registration),
                "Registration fetched successfully."
        );
    }


    // ============================================================
    // APPROVE
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationResponse>
    approveRegistration(
            Integer eventId,
            Integer registrationId) {

        return updateStatus(
                eventId,
                registrationId,
                RegistrationStatus.CONFIRMED
        );
    }


    // ============================================================
    // REJECT
    // ============================================================

    @Transactional
    public ApiResponse<RegistrationResponse>
    rejectRegistration(
            Integer eventId,
            Integer registrationId) {

        return updateStatus(
                eventId,
                registrationId,
                RegistrationStatus.REJECTED
        );
    }


    private ApiResponse<RegistrationResponse> updateStatus(
            Integer eventId,
            Integer registrationId,
            RegistrationStatus status) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (!event.getCreator().getId().equals(currentUser.getId())) {
            return ApiResponse.failure(
                    "Only the event organizer can manage registrations."
            );
        }

        EventRegistration registration =
                registrationRepository
                        .findByIdAndEventId(
                                registrationId,
                                eventId
                        )
                        .orElse(null);

        if (registration == null) {
            return ApiResponse.failure(
                    "Registration not found."
            );
        }

        registration.setStatus(status);

        registrationRepository.save(registration);

        return ApiResponse.success(
                RegistrationResponse.builder()
                        .registrationId(registration.getId())
                        .registered(true)
                        .status(status.name())
                        .build(),
                "Registration status updated successfully."
        );
    }


    // ============================================================
    // MAPPER
    // ============================================================

    private RegistrationDetailResponse toDetailResponse(
            EventRegistration registration) {

        List<RegistrationAnswerResponse> answers =
                answerRepository
                        .findByIdRegistrationId(registration.getId())
                        .stream()
                        .map(answer ->
                                RegistrationAnswerResponse.builder()
                                        .fieldId(
                                                answer.getField().getId()
                                        )
                                        .fieldLabel(
                                                answer.getField()
                                                        .getFieldLabel()
                                        )
                                        .answer(answer.getAnswer())
                                        .build()
                        )
                        .toList();

        return RegistrationDetailResponse.builder()
                .registrationId(registration.getId())
                .eventId(registration.getEvent().getId())
                .userId(registration.getUser().getId())
                .teamId(
                        registration.getTeam() != null
                                ? registration.getTeam().getId()
                                : null
                )
                .status(registration.getStatus().name())
                .submittedAt(registration.getSubmittedAt())
                .answers(answers)
                .build();
    }
}