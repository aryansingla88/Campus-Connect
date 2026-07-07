package com.campus.Campus_Connect.features.event.service.Impl;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.mapper.EventMemberMapper;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.event.security.EventValidationService;
import com.campus.Campus_Connect.features.event.service.EventAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAccessServiceImpl implements EventAccessService {

    private final EventValidationService eventValidationService;

    private final EventPermissionService permissionService;

    private final EventMemberRepository eventMemberRepository;

    private final EventMemberMapper eventMemberMapper;

    @Override
    public ApiResponse<List<UserAccessResponse>> getUsersWithAccess(
            Integer eventId
    ) {

        // Validate event exists
        eventValidationService.getEvent(eventId);

        // Only Creator/Admin can view access list
        permissionService.requireManager(eventId);

        List<EventMember> members =
                eventMemberRepository.findByEventIdAndRoleIn(
                        eventId,
                        List.of(
                                EventMemberRole.CREATOR,
                                EventMemberRole.ADMIN
                        )
                );

        List<UserAccessResponse> response =
                members.stream()
                        .map(eventMemberMapper::toResponse)
                        .toList();

        return ApiResponse.success(
                response,
                "Users with access fetched successfully."
        );
    }
}