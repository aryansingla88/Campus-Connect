package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.service.EventAccessService;
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
}