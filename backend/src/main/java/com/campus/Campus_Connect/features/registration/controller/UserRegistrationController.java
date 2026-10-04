package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    @PostMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> registerForEvent(
            @PathVariable Integer eventId,
            @RequestBody RegistrationRequest request) {
        return userRegistrationService.registerForEvent(eventId, request);
    }

    @GetMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> getRegistration(
            @PathVariable Integer eventId) {
        return userRegistrationService.getRegistration(eventId);
    }

    @DeleteMapping("/{eventId}/registration")
    public ApiResponse<Void> cancelRegistration(
            @PathVariable Integer eventId) {
        return userRegistrationService.cancelRegistration(eventId);
    }

    @GetMapping("/registrations")
    public ApiResponse<List<RegistrationResponse>> getMyRegistrations() {
        return userRegistrationService.getMyRegistrations();
    }
}
