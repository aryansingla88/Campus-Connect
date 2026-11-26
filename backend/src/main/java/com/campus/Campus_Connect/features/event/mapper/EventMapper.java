package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventPoster;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class EventMapper {

    private static final ZoneId EVENT_ZONE = ZoneId.of("Asia/Kolkata");

    public Instant effectiveEndTime(Event event) {
        if (event.getEndTime() != null) {
            return event.getEndTime();
        }
        LocalDate eventDate = event.getStartTime()
                .atZone(EVENT_ZONE)
                .toLocalDate();
        return eventDate.plusDays(1)
                .atStartOfDay(EVENT_ZONE)
                .toInstant()
                .minus(1, ChronoUnit.NANOS);
    }

    public EventState resolveEventState(Event event, Instant now) {

        if (event.getEventState() == EventState.CANCELLED) {
            return EventState.CANCELLED;
        }

        if (now.isBefore(event.getStartTime())) {
            return EventState.UPCOMING;
        }

        if (!now.isBefore(effectiveEndTime(event))) {
            return EventState.COMPLETED;
        }

        return EventState.ONGOING;
    }

    public EventState resolveEventState(Event event) {
        return resolveEventState(event, Instant.now());
    }

    // ------------------------------------------------------------------------
    // Create Request -> Entity
    // ------------------------------------------------------------------------

    public Event toEntity(
            CreateEventRequest request,
            User creator
    ) {

        return Event.builder()
                .title(request.getTitle())
                .description(request.getDescription())

                .latitude(request.getLatitude())
                .longitude(request.getLongitude())

                .startTime(request.getStartTime())
                .endTime(request.getEndTime())

                .creator(creator)

                .clubId(request.getClubId())
                .hostName(request.getHostName())
                .venue(request.getVenue())

                .visibilityType(request.getVisibilityType())

                .visibilityValue(request.getVisibilityValue())

                .registrationType(request.getRegistrationType())

                .registrationLink(request.getRegistrationLink())

                .basePriorityLevel(request.getPriority())

                // defaults
                .approvalStatus(ApprovalStatus.PENDING)
                .eventState(EventState.UPCOMING)

                .build();
    }

    // ------------------------------------------------------------------------
    // Update Request -> Existing Entity
    // ------------------------------------------------------------------------

    public void updateEntity(
            Event event,
            UpdateEventRequest request
    ) {

        if (request.getTitle() != null)
            event.setTitle(request.getTitle());

        if (request.getDescription() != null)
            event.setDescription(request.getDescription());

        if (request.getLatitude() != null)
            event.setLatitude(request.getLatitude());

        if (request.getLongitude() != null)
            event.setLongitude(request.getLongitude());

        if (request.getStartTime() != null)
            event.setStartTime(
                    request.getStartTime()
            );

        if (request.getEndTime() != null)
            event.setEndTime(
                    request.getEndTime()
            );

        if (request.getClubId() != null)
            event.setClubId(request.getClubId());

        if (request.getHostName() != null)
            event.setHostName(request.getHostName());

        if (request.getVenue() != null)
            event.setVenue(request.getVenue());

        if (request.getVisibilityType() != null)
            event.setVisibilityType(
                    request.getVisibilityType()
            );

        if (request.getVisibilityValue() != null)
            event.setVisibilityValue(request.getVisibilityValue());

        if (request.getRegistrationType() != null)
            event.setRegistrationType(
                    request.getRegistrationType()
            );

        if (request.getRegistrationLink() != null)
            event.setRegistrationLink(request.getRegistrationLink());

        if (request.getPriority() != null)
            event.setBasePriorityLevel(request.getPriority());
    }

    // ------------------------------------------------------------------------
    // Entity -> Response
    // ------------------------------------------------------------------------

    public EventResponse toResponse(
            Event event
    ) {

        return EventResponse.builder()

                .id(event.getId())

                .title(event.getTitle())
                .description(event.getDescription())

                .latitude(event.getLatitude())
                .longitude(event.getLongitude())

                .startTime(event.getStartTime())
                .endTime(effectiveEndTime(event))

                .createdBy(event.getCreator().getId())

                .clubId(event.getClubId())

                .hostName(event.getHostName())

                .venue(event.getVenue())

                .visibilityType(
                        event.getVisibilityType()
                )

                .visibilityValue(event.getVisibilityValue())

                .registrationType(
                        event.getRegistrationType()
                )

                .registrationLink(event.getRegistrationLink())

                .approvalStatus(
                        event.getApprovalStatus()
                )

                .eventState(
                        resolveEventState(event, Instant.now())
                )

                .priority(event.getBasePriorityLevel())

                .categoryId(
                        event.getCategories()
                                .stream()
                                .findFirst()
                                .map(category -> category.getId())
                                .orElse(null)
                )

                .categoryName(
                        event.getCategories()
                                .stream()
                                .findFirst()
                                .map(category -> category.getName())
                                .orElse(null)
                )

                .posterUrl(
                        event.getPosters()
                                .stream()
                                .findFirst()
                                .map(EventPoster::getPosterUrl)
                                .orElse(null)
                )

                .build();
    }
}
