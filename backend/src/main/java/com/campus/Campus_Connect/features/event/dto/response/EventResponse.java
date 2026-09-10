package com.campus.Campus_Connect.features.event.dto.response;

import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.entity.enums.VisibilityType;
import lombok.*;

import java.time.Instant;

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

    private Instant startTime;
    private Instant endTime;

    private Integer createdBy;

    private Integer clubId;

    private String hostName;

    private String venue;

    private VisibilityType visibilityType;

    private String visibilityValue;

    private RegistrationType registrationType;

    private String registrationLink;

    private ApprovalStatus approvalStatus;

    private EventState eventState;

    private Integer priority;

    private Integer categoryId;

    private String categoryName;

    private String posterUrl;
}