package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.response.EventMarkerResponse;
import com.campus.Campus_Connect.features.map.dto.response.EventPreviewResponse;
import com.campus.Campus_Connect.features.map.service.MapEventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/map/events")
@RequiredArgsConstructor
public class MapEventController {

    private final MapEventService mapEventService;

    @GetMapping
    public ApiResponse<List<EventMarkerResponse>> getEventMarkers() {
        return mapEventService.getEventMarkers();
    }

    @GetMapping("/{eventId}/preview")
    public ApiResponse<EventPreviewResponse> getEventPreview(
            @PathVariable Integer eventId
    ) {
        return mapEventService.getEventPreview(eventId);
    }
}