package com.campus.Campus_Connect.features.event.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.ParticipantsResponse;
import com.campus.Campus_Connect.features.event.service.ParticipantService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/events")
public class ParticipantController {

    private final ParticipantService participantService;

    @GetMapping("/{eventId}/participants")
    public ApiResponse<ParticipantsResponse> getParticipants(
            @PathVariable Integer eventId
    ) {

        return participantService.getParticipants(
                eventId
        );
    }
}