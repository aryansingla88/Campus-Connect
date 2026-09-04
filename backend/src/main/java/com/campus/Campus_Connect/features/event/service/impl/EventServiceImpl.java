package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventCategory;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.EventMemberId;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.mapper.EventMapper;
import com.campus.Campus_Connect.features.event.repository.EventCategoryRepository;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.service.EventService;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.honor.service.BadgeEvaluatorService;
import com.campus.Campus_Connect.features.honor.enums.StatisticType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventMemberRepository eventMemberRepository;
    private final EventPermissionService permissionService;
    private final EventCategoryRepository eventCategoryRepository;
    private final BadgeEvaluatorService badgeEvaluatorService;

    @Override
    public ApiResponse<List<EventResponse>> getEventFeed() {
        // 1. DB se algorithm ke through pre-sorted events fetch karo (O(1) logic overhead)
        List<Event> sortedEvents = eventRepository.findActiveEventsForFeed();

        // 2. Apne DTO (EventResponse) mein map karo.
        // (Assume kar raha hoon tumhare paas mapToResponse jaisa koi method hai)
        List<EventResponse> responseList = sortedEvents.stream()
                .map(eventMapper::toResponse) // Tumhara apna mapping logic use karna
                .toList();

        // 3. Fast response return kardo
        return ApiResponse.success(responseList, "Event feed fetched successfully");
    }

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
    public ApiResponse<List<EventResponse>> getMyEvents() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<EventResponse> events =
                eventMemberRepository
                        .findMyEvents(currentUser.getId())
                        .stream()
                        .map(eventMapper::toResponse)
                        .toList();

        return ApiResponse.success(
                events,
                "My events fetched successfully."
        );
    }

    @Override
    public ApiResponse<List<EventResponse>> getSharedEvents() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<EventResponse> events =
                eventMemberRepository
                        .findSharedEvents(currentUser.getId())
                        .stream()
                        .map(eventMapper::toResponse)
                        .toList();

        return ApiResponse.success(
                events,
                "Shared events fetched successfully."
        );
    }

    @Override
    public ApiResponse<List<EventResponse>> getManagedEvents() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<EventResponse> events =
                eventMemberRepository
                        .findManagedEvents(currentUser.getId())
                        .stream()
                        .map(eventMapper::toResponse)
                        .toList();

        return ApiResponse.success(
                events,
                "Managed events fetched successfully."
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

        User creator = SecurityUtils.getCurrentUser();

        Event event =
                eventMapper.toEntity(
                        request,
                        creator
                );

        EventCategory category =
                eventCategoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Event category not found."
                                ));

        event.getCategories().add(category);

        event = eventRepository.save(event);

        EventMember creatorMember =
                EventMember.builder()
                        .id(
                                new EventMemberId(
                                        event.getId(),
                                        creator.getId()
                                )
                        )
                        .event(event)
                        .user(creator)
                        .role(EventMemberRole.CREATOR)
                        .build();

        eventMemberRepository.save(creatorMember);

        //For Badges--aryan
        long hostedCount =
                eventMemberRepository.countByUser_IdAndRole(
                        creator.getId(),
                        EventMemberRole.CREATOR
                );

        badgeEvaluatorService.evaluateBadges(
                creator.getId(),
                StatisticType.HOSTED_EVENTS,
                Math.toIntExact(hostedCount)
        );

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

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found."));

        permissionService.requireManager(eventId);

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

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Event not found."));

        permissionService.requireCreator(eventId);

        eventRepository.delete(event);

        return ApiResponse.success(
                null,
                "Event deleted successfully."
        );
    }
}