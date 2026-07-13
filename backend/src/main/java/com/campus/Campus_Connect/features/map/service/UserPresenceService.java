package com.campus.Campus_Connect.features.map.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.map.dto.request.UpdatePresenceRequest;
import com.campus.Campus_Connect.features.map.dto.response.PresenceResponse;
import com.campus.Campus_Connect.features.map.dto.response.VisibleUserResponse;
import com.campus.Campus_Connect.features.map.entity.CampusBoundaryPoint;
import com.campus.Campus_Connect.features.map.entity.UserPresence;
import com.campus.Campus_Connect.features.map.repository.CampusBoundaryRepository;
import com.campus.Campus_Connect.features.map.repository.UserPresenceRepository;
import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import com.campus.Campus_Connect.features.settings.enums.ShowPresence;
import com.campus.Campus_Connect.features.settings.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserPresenceService {

    private final UserRepository userRepository;
    private final UserPresenceRepository userPresenceRepository;
    private final UserPreferenceRepository userPreferenceRepository;
    private final CampusBoundaryRepository campusBoundaryRepository;

    // Movement threshold in meters (e.g., don't update DB if movement is < 2 meters)
    private static final double MOVEMENT_THRESHOLD_METERS = 2.0;

    // ---------------------------------------------------------
    // Update My Presence
    // ---------------------------------------------------------

    public ApiResponse<PresenceResponse> updateMyPresence(
            UpdatePresenceRequest request
    ) {
        User currentUser = SecurityUtils.getCurrentUser();
        UserPresence presence = getOrCreatePresence(currentUser);

        // Chunk 8: Movement Threshold Logic
        // Skip database update if movement is insignificant (GPS Jitter)
        if (presence.getLatitude() != 0.0 && presence.getLongitude() != 0.0) {
            double distanceMoved = calculateDistanceInMeters(
                    presence.getLatitude(), presence.getLongitude(),
                    request.getLatitude(), request.getLongitude()
            );

            if (distanceMoved < MOVEMENT_THRESHOLD_METERS) {
                // Return existing data without hitting DB write
                return ApiResponse.success(
                        mapToPresenceResponse(presence),
                        "Location skipped (Movement too small)."
                );
            }
        }

        presence.setLatitude(request.getLatitude());
        presence.setLongitude(request.getLongitude());

        UserPresence savedPresence = userPresenceRepository.save(presence);

        return ApiResponse.success(
                mapToPresenceResponse(savedPresence),
                "Location updated successfully."
        );
    }

    // ---------------------------------------------------------
    // Get My Presence
    // ---------------------------------------------------------

    public ApiResponse<PresenceResponse> getMyPresence() {
        User currentUser = SecurityUtils.getCurrentUser();
        UserPresence presence = getOrCreatePresence(currentUser);

        return ApiResponse.success(
                mapToPresenceResponse(presence),
                "Presence fetched successfully."
        );
    }

    // ---------------------------------------------------------
    // Create Presence Row if Missing
    // ---------------------------------------------------------

    private UserPresence getOrCreatePresence(User user) {
        return userPresenceRepository
                .findById(user.getId())
                .orElseGet(() -> {
                    User managedUser = userRepository
                            .findById(user.getId())
                            .orElseThrow();

                    UserPresence presence = UserPresence.builder()
                            .user(managedUser)
                            .latitude(0.0)
                            .longitude(0.0)
                            .build();

                    return userPresenceRepository.save(presence);
                });
    }

    // ---------------------------------------------------------
    // Get Visible Users
    // ---------------------------------------------------------

    public ApiResponse<List<VisibleUserResponse>> getVisibleUsers() {
        User currentUser = SecurityUtils.getCurrentUser();

        UserPresence myPresence = getOrCreatePresence(currentUser);
        boolean amIInsideCampus = isInsideCampus(myPresence.getLatitude(), myPresence.getLongitude());

        List<VisibleUserResponse> response = new ArrayList<>();

        if (!amIInsideCampus) {
            response.add(mapToVisibleUserResponse(myPresence));
            return ApiResponse.success(
                    response,
                    "You are outside campus. Only showing your location."
            );
        }

        // Chunk 7: Offline Filtering (Users active in last 15 minutes)
        LocalDateTime activeThreshold = LocalDateTime.now().minusMinutes(15);
        List<UserPresence> presences = userPresenceRepository.findByLastUpdatedAfter(activeThreshold);

        List<Integer> userIds = presences.stream()
                .map(presence -> presence.getUser().getId())
                .collect(Collectors.toList());

        Map<Integer, UserPreference> preferencesMap = userPreferenceRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(UserPreference::getUserId, pref -> pref));

        for (UserPresence presence : presences) {
            Integer targetUserId = presence.getUser().getId();

            if (targetUserId.equals(currentUser.getId())) {
                response.add(mapToVisibleUserResponse(presence));
                continue;
            }

            boolean isTargetInside = isInsideCampus(presence.getLatitude(), presence.getLongitude());
            if (!isTargetInside) {
                continue;
            }

            UserPreference preference = preferencesMap.get(targetUserId);
            if (preference == null || preference.getShowPresence() == ShowPresence.OFF) {
                continue;
            }

            /*
             * TODO: External Modules Dependency
             * - CONNECTIONS (Friends only)
             * - COURSE (Classmates only)
             * - CLUBS (Club members only)
             * * This filtering logic relies on APIs from Connections, Course, and Club modules.
             * To be implemented once those teams expose internal lookup services.
             */

            response.add(mapToVisibleUserResponse(presence));
        }

        return ApiResponse.success(
                response,
                "Visible users fetched successfully."
        );
    }

    // ---------------------------------------------------------
    // Entity -> Visible User Response
    // ---------------------------------------------------------

    private VisibleUserResponse mapToVisibleUserResponse(UserPresence presence) {
        return VisibleUserResponse.builder()
                .userId(presence.getUser().getId())
                .username(presence.getUser().getUsername())
                .latitude(presence.getLatitude())
                .longitude(presence.getLongitude())
                .insideCampus(isInsideCampus(presence.getLatitude(), presence.getLongitude()))
                .build();
    }

    // ---------------------------------------------------------
    // Entity -> Presence Response
    // ---------------------------------------------------------

    private PresenceResponse mapToPresenceResponse(UserPresence presence) {
        String visibility = ShowPresence.PUBLIC.name();

        UserPreference preference = userPreferenceRepository
                .findById(presence.getUser().getId())
                .orElse(null);

        if (preference != null) {
            visibility = preference.getShowPresence().name();
        }

        return PresenceResponse.builder()
                .latitude(presence.getLatitude())
                .longitude(presence.getLongitude())
                .insideCampus(isInsideCampus(presence.getLatitude(), presence.getLongitude()))
                .visibility(visibility)
                .lastUpdated(presence.getLastUpdated())
                .build();
    }

    // ---------------------------------------------------------
    // Chunk 9: Ray Casting Algorithm for Campus Boundary
    // ---------------------------------------------------------

    private boolean isInsideCampus(Double latitude, Double longitude) {
        if (latitude == null || longitude == null || latitude == 0.0 || longitude == 0.0) {
            return false;
        }

        List<CampusBoundaryPoint> boundary = campusBoundaryRepository.findAllByOrderByPointOrderAsc();

        // Polygon must have at least 3 points
        if (boundary.size() < 3) {
            return false;
        }

        boolean isInside = false;
        int i, j;
        int numPoints = boundary.size();

        for (i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            double latI = boundary.get(i).getLatitude().doubleValue();
            double lonI = boundary.get(i).getLongitude().doubleValue();
            double latJ = boundary.get(j).getLatitude().doubleValue();
            double lonJ = boundary.get(j).getLongitude().doubleValue();

            boolean intersect = ((lonI > longitude) != (lonJ > longitude))
                    && (latitude < (latJ - latI) * (longitude - lonI) / (lonJ - lonI) + latI);

            if (intersect) {
                isInside = !isInside;
            }
        }

        return isInside;
    }

    // ---------------------------------------------------------
    // Utility: Haversine Formula for Distance Calculation
    // ---------------------------------------------------------

    private double calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Earth radius in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters
        return distance;
    }
}