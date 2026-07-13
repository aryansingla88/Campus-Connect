package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.GrantAccessRequest;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.service.EventAccessService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventAccessController {

    private final EventAccessService eventAccessService;

    @GetMapping("/{eventId}/access")
    public ApiResponse<List<UserAccessResponse>> getUsersWithAccess(
            @PathVariable Integer eventId
    ) {

        return eventAccessService.getUsersWithAccess(
                eventId
        );
    }

    @GetMapping("/{eventId}/access/search")
    public ApiResponse<List<UserAccessResponse>> searchUsers(
            @PathVariable Integer eventId,
            @RequestParam String query
    ) {

        return eventAccessService.searchUsers(
                eventId,
                query
        );
    }

    @PostMapping("/{eventId}/access")
    public ApiResponse<Void> grantAccess(
            @PathVariable Integer eventId,
            @Valid @RequestBody GrantAccessRequest request
    ) {

        return eventAccessService.grantAccess(
                eventId,
                request
        );
    }

    @DeleteMapping("/{eventId}/access/{userId}")
    public ApiResponse<Void> removeAccess(
            @PathVariable Integer eventId,
            @PathVariable Integer userId
    ) {

        return eventAccessService.removeAccess(
                eventId,
                userId
        );
    }
}