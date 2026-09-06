package com.campus.Campus_Connect.features.map.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMarkerResponse {

    private Integer id;

    private String title;

    private Double latitude;

    private Double longitude;

    private Integer priority;
}