package com.campus.Campus_Connect.features.registration.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.request.SaveRegistrationAnswersRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.dto.response.AvailableTeamResponse;
import com.campus.Campus_Connect.features.registration.service.UserRegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class UserRegistrationController {
    private final UserRegistrationService service;

    /** Starts or resumes a draft. The body is ignored for compatibility with the previous contract. */
    @PostMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> initialize(@PathVariable Integer eventId,
                                                         @RequestBody(required = false) RegistrationRequest request) {
        return service.registerForEvent(eventId, request);
    }

    @GetMapping("/{eventId}/registration")
    public ApiResponse<RegistrationResponse> get(@PathVariable Integer eventId) { return service.getRegistration(eventId); }

    @DeleteMapping("/{eventId}/registration")
    public ApiResponse<Void> cancel(@PathVariable Integer eventId) { return service.cancelRegistration(eventId); }

    @GetMapping("/registrations")
    public ApiResponse<List<RegistrationResponse>> mine() { return service.getMyRegistrations(); }

    @GetMapping("/{eventId}/registration/questions")
    public ApiResponse<List<RegistrationFieldResponse>> questions(@PathVariable Integer eventId,
                                                                   @RequestParam(defaultValue = "true") boolean individual) {
        return service.getPublishedFields(eventId, individual);
    }

    @GetMapping("/{eventId}/registration/teams")
    public ApiResponse<List<AvailableTeamResponse>> teams(@PathVariable Integer eventId,
                                                           @RequestParam(required = false) String search) {
        return service.getAvailableTeams(eventId, search);
    }

    @PutMapping("/registrations/{registrationId}/answers")
    public ApiResponse<RegistrationResponse> saveAnswers(@PathVariable Integer registrationId,
            @RequestBody SaveRegistrationAnswersRequest request) {
        return service.saveAnswers(registrationId, request);
    }

    @PostMapping("/registrations/{registrationId}/team")
    public ApiResponse<RegistrationResponse> createTeam(@PathVariable Integer registrationId,
                                                         @RequestBody Map<String, String> body) {
        return service.createTeam(registrationId, body == null ? null : body.get("teamName"));
    }

    @PostMapping("/registrations/{registrationId}/finalize")
    public ApiResponse<RegistrationResponse> finalizeRegistration(@PathVariable Integer registrationId) {
        return service.finalizeRegistration(registrationId);
    }
}
