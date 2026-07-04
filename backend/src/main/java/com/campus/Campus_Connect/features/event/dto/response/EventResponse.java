package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponse {

    private Integer id;

    private String title;

    private String description;

    private Double latitude;

    private Double longitude;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Integer createdBy;

    private Integer clubId;

    private String hostName;

    private String venue;

    private String visibilityType;

    private String visibilityValue;

    private String registrationType;

    private String registrationLink;

    private String approvalStatus;

    private String eventState;

    private Integer priority;
}