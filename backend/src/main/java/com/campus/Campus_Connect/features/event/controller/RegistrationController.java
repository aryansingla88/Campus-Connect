package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.event.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    @PostMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> registerForEvent(
            @PathVariable Integer eventId) {

        return registrationService.registerForEvent(eventId);
    }
}