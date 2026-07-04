package com.campus.Campus_Connect.features.event.service.Impl;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.mapper.EventMapper;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final UserRepository userRepository;

    @Override
    public ApiResponse<List<EventResponse>> getEvents() {

        List<EventResponse> events =
                eventRepository.findAll()
                        .stream()
                        .map(eventMapper::toResponse)
                        .toList();

        return ApiResponse.success(
                events,
                "Events fetched successfully."
        );
    }

    @Override
    public ApiResponse<EventResponse> getEvent(
            Integer eventId
    ) {

        Event event =
                eventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Event not found."));

        return ApiResponse.success(
                eventMapper.toResponse(event),
                "Event fetched successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<EventResponse> createEvent(
            CreateEventRequest request
    ) {

        // TODO: Replace with authenticated user after JWT integration
        User creator =
                userRepository.findById(2)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Creator not found."));

        Event event =
                eventMapper.toEntity(
                        request,
                        creator
                );

        event = eventRepository.save(event);

        return ApiResponse.success(
                eventMapper.toResponse(event),
                "Event created successfully."
        );
    }


    @Override
    @Transactional
    public ApiResponse<EventResponse> updateEvent(
            Integer eventId,
            UpdateEventRequest request
    ) {

        Event event =
                eventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Event not found."));

        eventMapper.updateEntity(
                event,
                request
        );

        event = eventRepository.save(event);

        return ApiResponse.success(
                eventMapper.toResponse(event),
                "Event updated successfully."
        );
    }


    @Override
    @Transactional
    public ApiResponse<Void> deleteEvent(
            Integer eventId
    ) {

        Event event =
                eventRepository.findById(eventId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Event not found."));

        eventRepository.delete(event);

        return ApiResponse.success(
                null,
                "Event deleted successfully."
        );
    }
}