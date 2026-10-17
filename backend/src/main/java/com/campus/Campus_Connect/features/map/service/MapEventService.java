package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventCategory;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import com.campus.Campus_Connect.features.event.repository.EventMemberRepository;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import com.campus.Campus_Connect.features.registration.repository.EventRegistrationRepository;
import com.campus.Campus_Connect.features.map.dto.response.EventMarkerResponse;
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
    private final ClubRepository clubRepository;
    private final EventRegistrationRepository eventRegistrationRepository;

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
        // Event previews are also used by unauthenticated map visitors.
        // A missing session must not turn the preview endpoint into HTTP 500.
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isJoined = false;
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof User currentUser) {
            isJoined = eventMemberRepository.existsByEventIdAndUserId(
                    eventId,
                    currentUser.getId()
            );
        }

        boolean isReminderEnabled = false;
        Integer priority = 1;

        EventPreviewResponse preview = EventPreviewResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .posterUrl(null)
                .hostName(event.getHostName())
                .creatorName(getCreatorName(event))
                .clubName(getClubName(event.getClubId()))
                .registrationCount(Math.toIntExact(eventRegistrationRepository.countByEventId(eventId)))
                .eventType(event.getCategories().stream()
                        .min(java.util.Comparator.comparing(EventCategory::getId))
                        .map(EventCategory::getName)
                        .orElse(null))
                .categories(event.getCategories().stream()
                        .sorted(java.util.Comparator.comparing(EventCategory::getId))
                        .map(category -> EventPreviewResponse.CategoryPreview.builder()
                                .id(category.getId())
                                .name(category.getName())
                                .build())
                        .toList())
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .venue(event.getVenue())
                .registrationType(
                        event.getRegistrationType() != null
                                ? event.getRegistrationType()
                                : RegistrationType.NONE
                )
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

    public ApiResponse<List<EventMarkerResponse>> getEventMarkers(Integer categoryId) {

        List<Event> events = categoryId == null
                ? eventRepository.findAll()
                : eventRepository.findEventsByCategoryId(categoryId);

        List<EventMarkerResponse> markers = events.stream()
                .filter(event ->
                        event.getLatitude() != null
                                && event.getLongitude() != null
                )
                .map(event -> EventMarkerResponse.builder()
                        .id(event.getId())
                        .title(event.getTitle())
                        .latitude(event.getLatitude())
                        .longitude(event.getLongitude())
                        .priority(1)
                        .build()
                )
                .toList();

        return ApiResponse.success(
                markers,
                "Event markers fetched successfully."
        );
    }

    private String getCreatorName(Event event) {
        if (event.getCreator() == null) return null;
        return userProfileRepository.findById(event.getCreator().getId())
                .map(UserProfile::getFullName)
                .orElse(event.getCreator().getUsername());
    }

    private String getClubName(Integer clubId) {
        if (clubId == null) return null;
        return clubRepository.findById(clubId).map(club -> club.getName()).orElse(null);
    }
}
