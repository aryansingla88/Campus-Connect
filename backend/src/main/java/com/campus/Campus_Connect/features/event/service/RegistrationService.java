package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventRegistration;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.event.repository.EventRegistrationRepository;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

    @Transactional
    public ApiResponse<RegistrationResponse> registerForEvent(Integer eventId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Event event = eventRepository.findById(eventId).orElse(null);

        if (event == null) {
            return ApiResponse.failure("Event not found.");
        }

        if (eventRegistrationRepository.existsByEventIdAndUserId(
                eventId,
                currentUser.getId()
        )) {
            return ApiResponse.failure("You are already registered for this event.");
        }

        EventRegistration registration = EventRegistration.builder()
                .event(event)
                .user(currentUser)
                .status(RegistrationStatus.CONFIRMED)
                .submittedAt(LocalDateTime.now())
                .teamId(null)                         
                .build();

        registration = eventRegistrationRepository.save(registration);

        RegistrationResponse response = RegistrationResponse.builder()
                .registrationId(registration.getId())
                .registered(true)
                .status(registration.getStatus().name())
                .build();

        return ApiResponse.success(
                response,
                "Registered successfully."
        );
    }
}