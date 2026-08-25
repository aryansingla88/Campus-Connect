package com.campus.Campus_Connect.features.event.service.impl;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.event.dto.request.GrantAccessRequest;
import com.campus.Campus_Connect.features.event.dto.response.UserAccessResponse;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.EventMemberId;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.mapper.EventMemberMapper;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import com.campus.Campus_Connect.features.event.security.EventPermissionService;
import com.campus.Campus_Connect.features.event.security.EventValidationService;
import com.campus.Campus_Connect.features.event.service.EventAccessService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventAccessServiceImpl implements EventAccessService {

    private final UserRepository userRepository;

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

    @Override
    public ApiResponse<List<UserAccessResponse>> searchUsers(
            Integer eventId,
            String query
    ) {

        permissionService.requireCreator(eventId);

        query = query.trim();

        if (query.isBlank()) {
            return ApiResponse.success(
                    List.of(),
                    "Users fetched successfully."
            );
        }

        List<User> users = userRepository.searchUsers(query);

        List<UserAccessResponse> response = new ArrayList<>();

        for (User user : users) {

            EventMember member =
                    permissionService.getMember(
                            eventId,
                            user.getId()
                    );

            // Don't show creator
            if (member != null
                    && member.getRole() == EventMemberRole.CREATOR) {
                continue;
            }

            response.add(
                    eventMemberMapper.toResponse(
                            user,
                            member != null
                                    ? member.getRole()
                                    : null,
                            member != null
                    )
            );
        }

        return ApiResponse.success(
                response,
                "Users fetched successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> grantAccess(
            Integer eventId,
            GrantAccessRequest request
    ) {

        // Check event exists
        eventValidationService.getEvent(eventId);

        // Only creator can grant access
        permissionService.requireCreator(eventId);

        // Target user
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found."
                        ));

        // Already has access?
        if (permissionService.hasAccess(
                eventId,
                user.getId()
        )) {

            throw new BadRequestException(
                    "User already has access."
            );
        }

        EventMember member =
                EventMember.builder()
                        .id(
                                new EventMemberId(
                                        eventId,
                                        user.getId()
                                )
                        )
                        .event(
                                eventValidationService.getEvent(eventId)
                        )
                        .user(user)
                        .role(EventMemberRole.ADMIN)
                        .build();

        eventMemberRepository.save(member);

        return ApiResponse.success(
                null,
                "Access granted successfully."
        );
    }

    @Override
    @Transactional
    public ApiResponse<Void> removeAccess(
            Integer eventId,
            Integer userId
    ) {

        eventValidationService.getEvent(eventId);

        permissionService.requireCreator(eventId);

        EventMember member =
                permissionService.requireMember(
                        eventId,
                        userId
                );

        if (member.getRole() == EventMemberRole.CREATOR) {
            throw new BadRequestException(
                    "Creator access cannot be removed."
            );
        }

        eventMemberRepository.delete(member);

        return ApiResponse.success(
                null,
                "Access removed successfully."
        );
    }
}