package com.campus.Campus_Connect.features.event.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.request.GrantAccessRequest;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;

import java.util.List;

public interface EventAccessService {

    ApiResponse<List<UserAccessResponse>> getUsersWithAccess(
            Integer eventId
    );

    ApiResponse<List<UserAccessResponse>> searchUsers(
            Integer eventId,
            String query
    );

    ApiResponse<Void> grantAccess(
            Integer eventId,
            GrantAccessRequest request
    );

    ApiResponse<Void> removeAccess(
            Integer eventId,
            Integer userId
    );

}
