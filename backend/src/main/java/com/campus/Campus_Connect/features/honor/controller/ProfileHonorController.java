package com.campus.Campus_Connect.features.honor.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.honor.dto.request.UpdateHonorPriorityRequest;
import com.campus.Campus_Connect.features.honor.dto.response.ProfileHonorsResponse;
import com.campus.Campus_Connect.features.honor.service.HonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class ProfileHonorController {

    private final HonorService honorService;

    @GetMapping("/me/honors")
    public ApiResponse<ProfileHonorsResponse> getMyHonors() {
        return honorService.getMyHonors();
    }

    @GetMapping("/{userId}/honors")
    public ApiResponse<ProfileHonorsResponse> getUserHonors(
            @PathVariable Integer userId
    ) {
        return honorService.getUserHonors(userId);
    }

    @PatchMapping("/me/honors/priority")
    public ApiResponse<Void> updateHonorPriority(
            @Valid @RequestBody UpdateHonorPriorityRequest request
    ) {
        return honorService.updateHonorPriority(request);
    }
}