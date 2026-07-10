package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ApiResponse<List<EventResponse>> getEvents() {
        return eventService.getEvents();
    }

    @GetMapping("/{eventId}")
    public ApiResponse<EventResponse> getEvent(
            @PathVariable Integer eventId
    ) {
        return eventService.getEvent(eventId);
    }

    @PostMapping
    public ApiResponse<EventResponse> createEvent(
            @Valid
            @RequestBody CreateEventRequest request
    ) {
        return eventService.createEvent(request);
    }

    @PatchMapping("/{eventId}")
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Integer eventId,
            @Valid
            @RequestBody UpdateEventRequest request
    ) {
        return eventService.updateEvent(
                eventId,
                request
        );
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> deleteEvent(
            @PathVariable Integer eventId
    ) {
        return eventService.deleteEvent(eventId);
    }
}