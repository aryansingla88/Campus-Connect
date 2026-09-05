package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface EventService {
    // Get pre-sorted event feed
    ApiResponse<List<EventResponse>> getEventFeed();
    ApiResponse<List<EventResponse>> getEvents();

    ApiResponse<EventResponse> getEvent(Integer eventId);

    ApiResponse<EventResponse> createEvent(
            CreateEventRequest request,
            MultipartFile poster
    );

    ApiResponse<EventResponse> updateEvent(
            Integer eventId,
            UpdateEventRequest request,
            MultipartFile poster
    );

    ApiResponse<Void> deleteEvent(
            Integer eventId
    );

    ApiResponse<List<EventResponse>> getMyEvents();

    ApiResponse<List<EventResponse>> getSharedEvents();

    ApiResponse<List<EventResponse>> getManagedEvents();

}