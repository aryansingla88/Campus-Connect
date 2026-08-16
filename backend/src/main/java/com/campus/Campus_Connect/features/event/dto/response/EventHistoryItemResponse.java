package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryItemResponse {

    private Integer id;

    private String title;

    private String venue;

    private Instant startTime;

    private Instant endTime;

    private String posterUrl;

}