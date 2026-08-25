package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationDetailResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;


    @PostMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> registerForEvent(
            @PathVariable Integer eventId,
            @RequestBody RegistrationRequest request) {

        return registrationService.registerForEvent(
                eventId,
                request
        );
    }


    @GetMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> getRegistration(
            @PathVariable Integer eventId) {

        return registrationService.getRegistration(eventId);
    }


    @DeleteMapping("/{eventId}/registration")
    public ApiResponse<Void> cancelRegistration(
            @PathVariable Integer eventId) {

        return registrationService.cancelRegistration(eventId);
    }


    @GetMapping("/registrations")
    public ApiResponse<List<RegistrationResponse>>
    getMyRegistrations() {

        return registrationService.getMyRegistrations();
    }


    @GetMapping("/{eventId}/registrations")
    public ApiResponse<List<RegistrationDetailResponse>>
    getEventRegistrations(
            @PathVariable Integer eventId) {

        return registrationService.getEventRegistrations(eventId);
    }


    @GetMapping("/{eventId}/registrations/{registrationId}")
    public ApiResponse<RegistrationDetailResponse>
    getEventRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {

        return registrationService.getEventRegistration(
                eventId,
                registrationId
        );
    }


    @PatchMapping(
            "/{eventId}/registrations/{registrationId}/approve"
    )
    public ApiResponse<RegistrationResponse>
    approveRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {

        return registrationService.approveRegistration(
                eventId,
                registrationId
        );
    }


    @PatchMapping(
            "/{eventId}/registrations/{registrationId}/reject"
    )
    public ApiResponse<RegistrationResponse>
    rejectRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {

        return registrationService.rejectRegistration(
                eventId,
                registrationId
        );
    }
}