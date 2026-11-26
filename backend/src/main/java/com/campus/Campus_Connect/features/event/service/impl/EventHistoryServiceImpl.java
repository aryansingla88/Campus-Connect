package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.response.EventHistoryItemResponse;
import com.campus.Campus_Connect.features.event.dto.response.EventHistoryResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.mapper.EventHistoryMapper;
import com.campus.Campus_Connect.features.event.mapper.EventMapper;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.service.EventHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventHistoryServiceImpl implements EventHistoryService {

    private final EventRepository eventRepository;

    private final EventHistoryMapper eventHistoryMapper;
    private final EventMapper eventMapper;

    @Override
    @Transactional
    public ApiResponse<EventHistoryResponse> getHistory() {

        User creator = SecurityUtils.getCurrentUser();

        List<Event> events =
                eventRepository.findByCreator_Id(
                        creator.getId()
                );

        List<EventHistoryItemResponse> live =
                new ArrayList<>();

        List<EventHistoryItemResponse> upcoming =
                new ArrayList<>();

        List<EventHistoryItemResponse> past =
                new ArrayList<>();

        Instant now = Instant.now();

        for (Event event : events) {

            if (event.getEndTime() == null) {
                event.setEndTime(eventMapper.effectiveEndTime(event));
            }
            event.setEventState(eventMapper.resolveEventState(event, now));

            EventHistoryItemResponse response =
                    eventHistoryMapper.toResponse(event);

            response.setEndTime(event.getEndTime());

            if (event.getEventState() == EventState.UPCOMING) {

                upcoming.add(response);

            } else if (event.getEventState() == EventState.COMPLETED) {

                past.add(response);

            } else {

                live.add(response);
            }
        }

        return ApiResponse.success(
                EventHistoryResponse.builder()
                        .live(live)
                        .upcoming(upcoming)
                        .past(past)
                        .build(),
                "History fetched successfully."
        );
    }
}
