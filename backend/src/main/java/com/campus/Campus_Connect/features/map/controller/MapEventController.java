package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.response.EventPreviewResponse;
import com.campus.Campus_Connect.features.map.service.MapEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/map/events")
@RequiredArgsConstructor
public class MapEventController {

    private final MapEventService mapEventService;

    // ---------------------------------------------------------
    // Get Event Preview Card Details
    // ---------------------------------------------------------

    @GetMapping("/{eventId}/preview")
    public ApiResponse<EventPreviewResponse> getEventPreview(
            @PathVariable Integer eventId
    ) {
        return mapEventService.getEventPreview(eventId);
    }
}