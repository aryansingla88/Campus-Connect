package com.campus.Campus_Connect.features.profile.controller;


import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.profile.dto.request.UpdateUserProfileRequest;
import com.campus.Campus_Connect.features.profile.dto.response.ProfileStatsResponse;
import com.campus.Campus_Connect.features.profile.dto.response.UserProfileResponse;
import com.campus.Campus_Connect.features.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ApiResponse<UserProfileResponse> getMyProfile(){
        return userProfileService.getMyProfile();
    }


    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @PathVariable Integer userId
    ){
        return userProfileService.getUserProfile(userId);
    }

    @PatchMapping
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @RequestBody UpdateUserProfileRequest request
    ){
        return userProfileService.updateMyProfile(request);
    }

    @GetMapping("/me/stats")
    public ApiResponse<ProfileStatsResponse> getMyProfileStats() {
        return userProfileService.getMyProfileStats();
    }

    @GetMapping("/{userId}/stats")
    public ApiResponse<ProfileStatsResponse> getProfileStats(
            @PathVariable Integer userId
    ) {
        return userProfileService.getProfileStats(userId);
    }


}
