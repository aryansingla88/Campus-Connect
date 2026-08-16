package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.EventHistoryResponse;
import com.campus.Campus_Connect.features.event.service.EventHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequestMapping("/events")
    @RequiredArgsConstructor
    public class EventHistoryController {

        private final EventHistoryService eventHistoryService;

        @GetMapping("/history")
        public ApiResponse<EventHistoryResponse> getHistory() {

            return eventHistoryService.getHistory();
        }
    }

