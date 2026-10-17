package com.campus.Campus_Connect.features.map.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.map.dto.response.EventMarkerResponse;
import com.campus.Campus_Connect.features.map.dto.response.EventPreviewResponse;
import com.campus.Campus_Connect.features.map.service.MapEventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/map/events")
@RequiredArgsConstructor
public class MapEventController {

    private final MapEventService mapEventService;

    @GetMapping
    public ApiResponse<List<EventMarkerResponse>> getEventMarkers(
            @RequestParam(required = false) Integer categoryId
    ) {
        return mapEventService.getEventMarkers(categoryId);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidMapParameter(
            MethodArgumentTypeMismatchException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.failure(
                        "Invalid value for parameter '" + exception.getName() + "'."
                ));
    }

    @GetMapping("/{eventId}/preview")
    public ApiResponse<EventPreviewResponse> getEventPreview(
            @PathVariable Integer eventId
    ) {
        return mapEventService.getEventPreview(eventId);
    }
}
