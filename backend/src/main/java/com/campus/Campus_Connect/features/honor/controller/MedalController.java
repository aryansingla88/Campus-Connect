package com.campus.Campus_Connect.features.honor.controller;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.honor.dto.request.AwardMedalRequest;
import com.campus.Campus_Connect.features.honor.dto.response.MedalsResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalCandidateResponse;
import com.campus.Campus_Connect.features.honor.service.HonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class MedalController {

    private final HonorService honorService;

    @GetMapping("/{eventId}/medals")
    public ApiResponse<MedalsResponse> getEventMedals(
            @PathVariable Integer eventId
    ) {

        return honorService.getEventMedals(eventId);
    }

    @GetMapping("/{eventId}/participants/eligible-for-medal")
    public ApiResponse<List<MedalCandidateResponse>> getEligibleParticipants(
            @PathVariable Integer eventId
    ) {

        return honorService.getEligibleParticipants(
                eventId
        );
    }

    @PostMapping("/{eventId}/medals")
    public ApiResponse<Void> awardMedal(
            @PathVariable Integer eventId,
            @Valid @RequestBody AwardMedalRequest request
    ) {

        return honorService.awardMedal(
                eventId,
                request
        );
    }

    @DeleteMapping("/{eventId}/medals/{honorId}")
    public ApiResponse<Void> deleteMedal(
            @PathVariable Integer eventId,
            @PathVariable Integer honorId
    ) {

        return honorService.deleteMedal(
                eventId,
                honorId
        );
    }
}