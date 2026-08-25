package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.request.UpdatePresenceRequest;
import com.campus.Campus_Connect.features.map.dto.response.PresenceResponse;
import com.campus.Campus_Connect.features.map.dto.response.UserPreviewResponse;
import com.campus.Campus_Connect.features.map.dto.response.VisibleUserResponse;
import com.campus.Campus_Connect.features.map.service.UserPresenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presence")
@RequiredArgsConstructor
public class UserPresenceController {

    private final UserPresenceService userPresenceService;

    // ---------------------------------------------------------
    // Update Current User Location
    // ---------------------------------------------------------

    @PatchMapping("/me")
    public ApiResponse<PresenceResponse> updateMyPresence(
            @Valid
            @RequestBody
            UpdatePresenceRequest request
    ) {
        return userPresenceService.updateMyPresence(request);
    }

    // ---------------------------------------------------------
    // Get Current User Location
    // ---------------------------------------------------------

    @GetMapping("/me")
    public ApiResponse<PresenceResponse> getMyPresence() {
        return userPresenceService.getMyPresence();
    }

    // ---------------------------------------------------------
    // Get Visible Users
    // ---------------------------------------------------------

    @GetMapping
    public ApiResponse<List<VisibleUserResponse>> getVisibleUsers() {
        return userPresenceService.getVisibleUsers();
    }

    // ---------------------------------------------------------
    // Get User Preview Card Details
    // ---------------------------------------------------------

    @GetMapping("/users/{userId}/preview")
    public ApiResponse<UserPreviewResponse> getUserPreview(
            @PathVariable Integer userId
    ) {
        return userPresenceService.getUserPreview(userId);
    }
}