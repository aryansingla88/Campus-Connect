package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationConfigRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationFieldRequest;
import com.campus.Campus_Connect.features.registration.dto.response.*;
import com.campus.Campus_Connect.features.registration.service.HostRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class HostRegistrationController {

    private final HostRegistrationService hostRegistrationService;

    // ============================================================
    // CONFIGURATION
    // ============================================================

    @GetMapping("/{eventId}/registration/config")
    public ApiResponse<RegistrationConfigResponse> getRegistrationConfig(
            @PathVariable Integer eventId) {
        return hostRegistrationService.getRegistrationConfig(eventId);
    }

    @PostMapping("/{eventId}/registration/config")
    public ApiResponse<RegistrationConfigResponse> createRegistrationConfig(
            @PathVariable Integer eventId,
            @RequestBody RegistrationConfigRequest request) {
        return hostRegistrationService.createRegistrationConfig(eventId, request);
    }

    @PutMapping("/{eventId}/registration/config")
    public ApiResponse<RegistrationConfigResponse> updateRegistrationConfig(
            @PathVariable Integer eventId,
            @RequestBody RegistrationConfigRequest request) {
        return hostRegistrationService.updateRegistrationConfig(eventId, request);
    }

    // ============================================================
    // REGISTRATION FIELDS
    // ============================================================

    @GetMapping("/{eventId}/registration/fields")
    public ApiResponse<List<RegistrationFieldResponse>> getRegistrationFields(
            @PathVariable Integer eventId) {
        return hostRegistrationService.getRegistrationFields(eventId);
    }

    @PostMapping("/{eventId}/registration/fields")
    public ApiResponse<List<RegistrationFieldResponse>> createRegistrationFields(
            @PathVariable Integer eventId,
            @RequestBody List<RegistrationFieldRequest> requests) {
        return hostRegistrationService.createRegistrationFields(eventId, requests);
    }

    @PutMapping("/{eventId}/registration/fields")
    public ApiResponse<List<RegistrationFieldResponse>> updateRegistrationFields(
            @PathVariable Integer eventId,
            @RequestBody List<RegistrationFieldRequest> requests) {
        return hostRegistrationService.updateRegistrationFields(eventId, requests);
    }

    // ============================================================
    // REGISTRATION RESPONSES
    // ============================================================


    @GetMapping("/{eventId}/registrations")
    public ApiResponse<List<RegistrationShortResponse>> getEventRegistrations(
            @PathVariable Integer eventId) {
        return hostRegistrationService.getEventRegistrations(eventId);
    }

    @GetMapping("/{eventId}/registrations/{registrationId}")
    public ApiResponse<RegistrationDetailResponse> getEventRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {
        return hostRegistrationService.getEventRegistration(eventId, registrationId);
    }

    // ============================================================
    // REGISTRATION STATUS
    // ============================================================

    @PatchMapping("/{eventId}/registrations/{registrationId}/approve")
    public ApiResponse<RegistrationResponse> approveRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {
        return hostRegistrationService.approveRegistration(eventId, registrationId);
    }

    @PatchMapping("/{eventId}/registrations/{registrationId}/reject")
    public ApiResponse<RegistrationResponse> rejectRegistration(
            @PathVariable Integer eventId,
            @PathVariable Integer registrationId) {
        return hostRegistrationService.rejectRegistration(eventId, registrationId);
    }
}