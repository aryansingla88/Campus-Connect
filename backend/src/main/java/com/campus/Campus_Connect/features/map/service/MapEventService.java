package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.map.dto.response.EventPreviewResponse;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MapEventService {

    private final EventRepository eventRepository;
    private final EventMemberRepository eventMemberRepository;
    private final UserProfileRepository userProfileRepository;

    // ---------------------------------------------------------
    // Get Event Preview for Map Card (Poster & Detail View)
    // ---------------------------------------------------------

    public ApiResponse<EventPreviewResponse> getEventPreview(Integer eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with ID: " + eventId));

        // Sirf CREATOR, ADMIN, aur HOST roles ke members fetch honge
        List<EventMemberRole> allowedHostRoles = Arrays.asList(
                EventMemberRole.CREATOR,
                EventMemberRole.ADMIN
        );

        List<EventMember> members = eventMemberRepository.findByEventIdAndRoleIn(
                eventId,
                allowedHostRoles
        );

        List<EventPreviewResponse.HostPreview> hostPreviews = new ArrayList<>();

        for (EventMember member : members) {
            Integer userId = member.getId().getUserId();

            // Strictly using UserProfileRepository only for fullName and avatarUrl
            UserProfile profile = userProfileRepository.findById(userId).orElse(null);

            if (profile != null) {
                String fullName = profile.getFullName();
                String avatarUrl = profile.getAvatarUrl();
                String role = (member.getRole() != null) ? member.getRole().name() : "HOST";

                hostPreviews.add(EventPreviewResponse.HostPreview.builder()
                        .userId(userId)
                        .fullName(fullName)
                        .avatarUrl(avatarUrl)
                        .role(role)
                        .build());
            }
        }

        // ---------------------------------------------------------
        // Dynamic isJoined Check from DB
        // ---------------------------------------------------------
        User currentUser = SecurityUtils.getCurrentUser();
        boolean isJoined = false;

        if (currentUser != null) {
            isJoined = eventMemberRepository.existsByEventIdAndUserId(eventId, currentUser.getId());
        }

        boolean isReminderEnabled = false;
        Integer priority = 1;

        EventPreviewResponse preview = EventPreviewResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .posterUrl(null)
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .venue(event.getVenue())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .registrationType(event.getRegistrationType() != null ? event.getRegistrationType().name() : "FREE")
                .registrationLink(event.getRegistrationLink())
                .isJoined(isJoined)
                .isReminderEnabled(isReminderEnabled)
                .priority(priority)
                .hosts(hostPreviews)
                .build();

        return ApiResponse.success(
                preview,
                "Event preview fetched successfully."
        );
    }
}