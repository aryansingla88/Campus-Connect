package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventMapperStateTest {

    private final EventMapper mapper = new EventMapper();

    @Test
    void missingEndTimeUsesEndOfStartDateAndCompletesAtBoundary() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        Instant start = LocalDate.of(2026, 8, 1)
                .atTime(9, 0)
                .atZone(zone)
                .toInstant();
        Event event = Event.builder()
                .startTime(start)
                .eventState(EventState.UPCOMING)
                .build();

        Instant effectiveEnd = mapper.effectiveEndTime(event);

        assertEquals(
                LocalDate.of(2026, 8, 1)
                        .plusDays(1)
                        .atStartOfDay(zone)
                        .toInstant()
                        .minusNanos(1),
                effectiveEnd
        );
        assertEquals(
                EventState.ONGOING,
                mapper.resolveEventState(event, effectiveEnd)
        );
        assertEquals(
                EventState.COMPLETED,
                mapper.resolveEventState(event, effectiveEnd.plusNanos(1))
        );
    }

    @Test
    void cancelledEventRemainsCancelledRegardlessOfDates() {
        Event event = Event.builder()
                .startTime(Instant.parse("2026-01-01T00:00:00Z"))
                .eventState(EventState.CANCELLED)
                .build();

        assertEquals(
                EventState.CANCELLED,
                mapper.resolveEventState(event, Instant.parse("2026-02-01T00:00:00Z"))
        );
    }
}
