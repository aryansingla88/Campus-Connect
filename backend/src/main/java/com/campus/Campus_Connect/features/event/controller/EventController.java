package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(consumes = "multipart/form-data")
    public ApiResponse<EventResponse> createEvent(
            @Valid
            @RequestPart("event") CreateEventRequest request,
            @RequestPart(value = "poster", required = false) MultipartFile poster
    ) {
        return eventService.createEvent(request, poster);
    }

    @PatchMapping(
            value = "/{eventId}",
            consumes = "multipart/form-data"
    )
    public ApiResponse<EventResponse> updateEvent(
            @PathVariable Integer eventId,
            @Valid
            @RequestPart("event") UpdateEventRequest request,
            @RequestPart(value = "poster", required = false) MultipartFile poster
    ) {
        return eventService.updateEvent(
                eventId,
                request,
                poster
        );
    }

    @DeleteMapping("/{eventId}")
    public ApiResponse<Void> deleteEvent(
            @PathVariable Integer eventId
    ) {
        return eventService.deleteEvent(eventId);
    }

    @GetMapping("/mine")
    public ApiResponse<List<EventResponse>> getMyEvents() {
        return eventService.getMyEvents();
    }

    // GET /events/shared
    // Events where current user is ADMIN
    @GetMapping("/shared")
    public ApiResponse<List<EventResponse>> getSharedEvents() {
        return eventService.getSharedEvents();
    }

    // GET /events/managed
    // Events where current user is CREATOR or ADMIN
    @GetMapping("/managed")
    public ApiResponse<List<EventResponse>> getManagedEvents() {
        return eventService.getManagedEvents();
    }


    // GET /api/events/feed
    @GetMapping("/feed")
    public ApiResponse<List<EventResponse>> getEventFeed() {
        // Sirf data fetch aur return, zero processing!
        return eventService.getEventFeed();
    }
}