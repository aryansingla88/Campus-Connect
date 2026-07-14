package com.campus.Campus_Connect.features.settings.controller;


import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.settings.dto.UpdateUserPreferenceRequest;
import com.campus.Campus_Connect.features.settings.dto.UserPreferenceResponse;
import com.campus.Campus_Connect.features.settings.service.UserPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/settings/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService userPreferenceService;

    @GetMapping
    public ApiResponse<UserPreferenceResponse> getMyPreferences() {
        return userPreferenceService.getMyPreferences();
    }

    @PatchMapping
    public ApiResponse<UserPreferenceResponse> updateMyPreferences(
            @RequestBody UpdateUserPreferenceRequest request
    ) {
        return userPreferenceService.updateMyPreferences(request);
    }


}
