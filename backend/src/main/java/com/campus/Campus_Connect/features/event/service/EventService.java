package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;

import java.util.List;

public interface EventService {

    ApiResponse<List<EventResponse>> getEvents();

    ApiResponse<EventResponse> getEvent(Integer eventId);

    ApiResponse<EventResponse> createEvent(
            CreateEventRequest request
    );

    ApiResponse<EventResponse> updateEvent(
            Integer eventId,
            UpdateEventRequest request
    );

    ApiResponse<Void> deleteEvent(
            Integer eventId
    );
}