package com.campus.Campus_Connect.features.settings.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.settings.dto.UpdateUserPreferenceRequest;
import com.campus.Campus_Connect.features.settings.dto.UserPreferenceResponse;
import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import com.campus.Campus_Connect.features.settings.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPreferenceService {

    private final UserPreferenceRepository userPreferenceRepository;

    public ApiResponse<UserPreferenceResponse> getMyPreferences() {

        User currentUser = SecurityUtils.getCurrentUser();

        UserPreference preference =
                getOrCreateUserPreference(currentUser);


        UserPreferenceResponse response = buildUserPreferenceResponse(preference);

        return ApiResponse.success(
                response,
                "User's Preferences fetched successfully."
        );
    }

    public ApiResponse<UserPreferenceResponse> updateMyPreferences(UpdateUserPreferenceRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        UserPreference preference =
                getOrCreateUserPreference(currentUser);

        applyUpdates(preference,request);

        userPreferenceRepository.save(preference);

        UserPreferenceResponse response = buildUserPreferenceResponse(preference);

        return ApiResponse.success(
                response,
                "User preferences updated successfully."
        );

    }

//  ---------------------------------------------

    private UserPreference getOrCreateUserPreference(User user){

        Optional<UserPreference> preference = userPreferenceRepository.findById(user.getId());

        if(preference.isPresent()){
            return preference.get();
        }

        UserPreference newPreference = UserPreference.builder()
                .user(user)
                .build();

        return userPreferenceRepository.save(newPreference);
    }




    private UserPreferenceResponse buildUserPreferenceResponse(
            UserPreference preference
    ){
        return UserPreferenceResponse.builder()
                .showSocials(preference.getShowSocials())
                .showPhone(preference.getShowPhone())
                .showPresence(preference.getShowPresence())
                .notifyConnections(preference.getNotifyConnections())
                .notifyEvents(preference.getNotifyEvents())
                .notifyPosts(preference.getNotifyPosts())
                .theme(preference.getTheme())
                .build();

    }

    private void applyUpdates(
            UserPreference preference,
            UpdateUserPreferenceRequest request
    ) {

        if (request.getShowPhone() != null) {
            preference.setShowPhone(request.getShowPhone());
        }

        if (request.getShowSocials() != null) {
            preference.setShowSocials(request.getShowSocials());
        }

        if (request.getShowPresence() != null) {
            preference.setShowPresence(request.getShowPresence());
        }

        if (request.getNotifyConnections() != null) {
            preference.setNotifyConnections(request.getNotifyConnections());
        }

        if (request.getNotifyEvents() != null) {
            preference.setNotifyEvents(request.getNotifyEvents());
        }

        if (request.getNotifyPosts() != null) {
            preference.setNotifyPosts(request.getNotifyPosts());
        }

        if (request.getTheme() != null) {
            preference.setTheme(request.getTheme());
        }
    }


}