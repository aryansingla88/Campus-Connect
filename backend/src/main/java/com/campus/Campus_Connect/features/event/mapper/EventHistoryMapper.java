package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.event.dto.response.EventHistoryItemResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventHistoryMapper {

    public EventHistoryItemResponse toResponse(
            Event event
    ) {

        return EventHistoryItemResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .venue(event.getVenue())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .posterUrl(null)
                .build();
    }
}