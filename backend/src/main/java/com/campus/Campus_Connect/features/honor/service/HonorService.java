package com.campus.Campus_Connect.features.honor.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.honor.dto.request.AwardMedalRequest;
import com.campus.Campus_Connect.features.honor.dto.request.UpdateHonorPriorityRequest;
import com.campus.Campus_Connect.features.honor.dto.response.MedalsResponse;
import com.campus.Campus_Connect.features.honor.dto.response.MedalCandidateResponse;
import com.campus.Campus_Connect.features.honor.dto.response.ProfileHonorsResponse;

import java.util.List;

public interface HonorService {

    ApiResponse<MedalsResponse> getEventMedals(
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

    ApiResponse<ProfileHonorsResponse> getMyHonors();

    ApiResponse<ProfileHonorsResponse> getUserHonors(
            Integer userId
    );

    ApiResponse<Void> updateHonorPriority(
            UpdateHonorPriorityRequest request
    );

}