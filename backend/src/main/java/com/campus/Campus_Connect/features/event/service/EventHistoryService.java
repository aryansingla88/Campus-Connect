package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.EventHistoryResponse;

public interface EventHistoryService {

    ApiResponse<EventHistoryResponse> getHistory();
}
