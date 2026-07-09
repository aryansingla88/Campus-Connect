package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.map.dto.request.UpdatePresenceRequest;
import com.campus.Campus_Connect.features.map.dto.response.PresenceResponse;
import com.campus.Campus_Connect.features.map.dto.response.VisibleUserResponse;
import com.campus.Campus_Connect.features.map.entity.UserPresence;
import com.campus.Campus_Connect.features.map.repository.UserPresenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserPresenceService {

    private final UserPresenceRepository userPresenceRepository;


    // ---------------------------------------------------------
    // Update Current User Presence
    // ---------------------------------------------------------

    public ApiResponse<PresenceResponse> updateMyPresence(
            UpdatePresenceRequest request
    ) {

        User currentUser = getCurrentUser();

        UserPresence presence = getOrCreatePresence(currentUser);

        presence.setLatitude(request.getLatitude());
        presence.setLongitude(request.getLongitude());

        UserPresence savedPresence =
                userPresenceRepository.save(presence);

        return ApiResponse.success(
                mapToPresenceResponse(savedPresence),
                "Location updated successfully."
        );
    }

    // ---------------------------------------------------------
    // Current Logged In User
    // ---------------------------------------------------------

    private User getCurrentUser() {

        Object principal =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication()
                        .getPrincipal();

        if (!(principal instanceof User user)) {
            throw new ResourceNotFoundException(
                    "Authenticated user not found."
            );
        }

        return user;
    }

    // ---------------------------------------------------------
    // Create Presence Row if Missing
    // ---------------------------------------------------------

    private UserPresence getOrCreatePresence(User user) {

        return userPresenceRepository
                .findByUser(user)
                .orElseGet(() -> {

                    UserPresence presence =
                            UserPresence.builder()
                                    .user(user)
                                    .latitude(0.0)
                                    .longitude(0.0)
                                    .build();

                    return userPresenceRepository.save(presence);
                });
    }

    // ---------------------------------------------------------
    // Get Current User Presence
    // ---------------------------------------------------------

    public ApiResponse<PresenceResponse> getMyPresence() {

        User currentUser = getCurrentUser();

        UserPresence presence = getOrCreatePresence(currentUser);

        return ApiResponse.success(
                mapToPresenceResponse(presence),
                "Presence fetched successfully."
        );
    }

    // ---------------------------------------------------------
    // Get Visible Users
    // ---------------------------------------------------------

    public ApiResponse<List<VisibleUserResponse>> getVisibleUsers() {

        List<UserPresence> presences =
                userPresenceRepository.findAll();

        List<VisibleUserResponse> response =
                new ArrayList<>();

        for (UserPresence presence : presences) {

            // TODO
            // Team Integration:
            // Check user_preferences.show_presence
            // Skip PRIVATE users.
            // FRIENDS visibility will be handled here.

            response.add(
                    mapToVisibleUserResponse(presence)
            );
        }

        return ApiResponse.success(
                response,
                "Visible users fetched successfully."
        );
    }

    // ---------------------------------------------------------
    // Entity -> Presence Response
    // ---------------------------------------------------------

    private PresenceResponse mapToPresenceResponse(
            UserPresence presence
    ) {

        return PresenceResponse.builder()
                .latitude(presence.getLatitude())
                .longitude(presence.getLongitude())
                .insideCampus(
                        isInsideCampus(
                                presence.getLatitude(),
                                presence.getLongitude()
                        )
                )

                // TODO
                // Replace after UserPreference implementation.
                .visibility("PUBLIC")

                .lastUpdated(
                        presence.getLastUpdated()
                )
                .build();
    }

    // ---------------------------------------------------------
    // Entity -> Visible User Response
    // ---------------------------------------------------------

    private VisibleUserResponse mapToVisibleUserResponse(
            UserPresence presence
    ) {

        return VisibleUserResponse.builder()
                .userId(
                        presence.getUser().getId()
                )
                .username(
                        presence.getUser().getUsername()
                )
                .latitude(
                        presence.getLatitude()
                )
                .longitude(
                        presence.getLongitude()
                )
                .insideCampus(
                        isInsideCampus(
                                presence.getLatitude(),
                                presence.getLongitude()
                        )
                )
                .build();
    }

    // ---------------------------------------------------------
    // Campus Boundary
    // ---------------------------------------------------------

    private boolean isInsideCampus(
            Double latitude,
            Double longitude
    ) {

        // TODO
        // Replace with PostGIS polygon check.
        // ST_Contains(campus_polygon, location)

        return true;
    }

}