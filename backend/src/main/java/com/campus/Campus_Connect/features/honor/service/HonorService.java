package com.campus.Campus_Connect.features.honor.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.honor.dto.request.AwardMedalRequest;
import com.campus.Campus_Connect.features.honor.dto.response.EventMedalsResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalCandidateResponse;

import java.util.List;

public interface HonorService {

    ApiResponse<EventMedalsResponse> getEventMedals(
            Integer eventId
    );

    ApiResponse<List<MedalCandidateResponse>> getEligibleParticipants(
            Integer eventId
    );

    ApiResponse<Void> awardMedal(
            Integer eventId,
            AwardMedalRequest request
    );

    ApiResponse<Void> deleteMedal(
            Integer eventId,
            Integer honorId
    );

}