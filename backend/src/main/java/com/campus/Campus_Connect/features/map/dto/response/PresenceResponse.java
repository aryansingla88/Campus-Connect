package com.campus.Campus_Connect.features.map.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceResponse {

    private Double latitude;

    private Double longitude;

    private Boolean insideCampus;

    private String visibility;

    private LocalDateTime lastUpdated;
}