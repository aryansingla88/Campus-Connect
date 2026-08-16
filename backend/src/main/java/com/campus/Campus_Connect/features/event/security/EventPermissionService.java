package com.campus.Campus_Connect.features.event.security;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.exception.UnauthorizedException;
import com.campus.Campus_Connect.common.security.AuthorizationUtils;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPermissionService {

    private final EventMemberRepository eventMemberRepository;
    private final EventValidationService eventValidationService;

    public EventMember getCurrentMember(Integer eventId) {

        User currentUser = SecurityUtils.getCurrentUser();

        return eventMemberRepository
                .findByEventIdAndUserId(
                        eventId,
                        currentUser.getId()
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "You are not a manager of this event."
                        ));
    }

    public void requireCreator(
            Integer eventId
    ) {

        Event event = eventValidationService.getEvent(
                eventId
        );

        AuthorizationUtils.requireOwner(
                event.getCreator().getId()
        );
    }

    public EventMember requireManager(Integer eventId) {

        EventMember member = getCurrentMember(eventId);

        if (member.getRole() != EventMemberRole.CREATOR
                && member.getRole() != EventMemberRole.ADMIN) {

            throw new UnauthorizedException(
                    "You don't have permission to perform this action."
            );
        }

        return member;
    }

    public boolean isCreator(Integer eventId) {
        return getCurrentMember(eventId).getRole() == EventMemberRole.CREATOR;
    }

    public EventMember getMember(
            Integer eventId,
            Integer userId
    ) {

        return eventMemberRepository
                .findByEventIdAndUserId(
                        eventId,
                        userId
                )
                .orElse(null);
    }

    public EventMember requireMember(
            Integer eventId,
            Integer userId
    ) {

        return eventMemberRepository
                .findByEventIdAndUserId(
                        eventId,
                        userId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User does not have access."
                        ));
    }

    public boolean hasAccess(
            Integer eventId,
            Integer userId
    ) {
        return getMember(eventId, userId) != null;
    }
}