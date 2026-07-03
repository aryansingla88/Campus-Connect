package com.campus.Campus_Connect.features.event.mapper;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.dto.request.CreateEventRequest;
import com.campus.Campus_Connect.features.event.dto.request.UpdateEventRequest;
import com.campus.Campus_Connect.features.event.dto.response.EventResponse;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class EventMapper {

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

                .visibilityType(
                        VisibilityType.valueOf(
                                request.getVisibilityType().toUpperCase()
                        )
                )

                .visibilityValue(request.getVisibilityValue())

                .registrationType(
                        RegistrationType.valueOf(
                                request.getRegistrationType().toUpperCase()
                        )
                )

                .registrationLink(request.getRegistrationLink())

                .priority(request.getPriority())

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
                    VisibilityType.valueOf(
                            request.getVisibilityType().toUpperCase()
                    )
            );

        if (request.getVisibilityValue() != null)
            event.setVisibilityValue(request.getVisibilityValue());

        if (request.getRegistrationType() != null)
            event.setRegistrationType(
                    RegistrationType.valueOf(
                            request.getRegistrationType().toUpperCase()
                    )
            );

        if (request.getRegistrationLink() != null)
            event.setRegistrationLink(request.getRegistrationLink());

        if (request.getPriority() != null)
            event.setPriority(request.getPriority());
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
                .endTime(event.getEndTime())

                .createdBy(event.getCreator().getId())

                .clubId(event.getClubId())

                .hostName(event.getHostName())

                .venue(event.getVenue())

                .visibilityType(
                        event.getVisibilityType().name()
                )

                .visibilityValue(event.getVisibilityValue())

                .registrationType(
                        event.getRegistrationType().name()
                )

                .registrationLink(event.getRegistrationLink())

                .approvalStatus(
                        event.getApprovalStatus().name()
                )

                .eventState(
                        event.getEventState().name()
                )

                .priority(event.getPriority())

                .build();
    }
}