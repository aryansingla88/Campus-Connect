package com.campus.Campus_Connect.features.profile.controller;


import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.profile.dto.request.UpdateUserProfileRequest;
import com.campus.Campus_Connect.features.profile.dto.response.ProfileStatsResponse;
import com.campus.Campus_Connect.features.profile.dto.response.UserProfileResponse;
import com.campus.Campus_Connect.features.profile.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping("/me")
    public ApiResponse<UserProfileResponse> getMyProfile(){
        return userProfileService.getMyProfile();
    }


    @GetMapping("/{userId}")
    public ApiResponse<UserProfileResponse> getUserProfile(
            @PathVariable Integer userId
    ){
        return userProfileService.getUserProfile(userId);
    }

    @PatchMapping(
            value = "/me",
            consumes = "multipart/form-data"
    )
    public ApiResponse<UserProfileResponse> updateMyProfile(
            @RequestPart("profile") UpdateUserProfileRequest request,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return userProfileService.updateMyProfile(request, image);
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
