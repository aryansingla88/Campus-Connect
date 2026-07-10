package com.campus.Campus_Connect.features.event.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventHistoryResponse {

    private List<EventHistoryItemResponse> live;

    private List<EventHistoryItemResponse> upcoming;

    private List<EventHistoryItemResponse> past;

}