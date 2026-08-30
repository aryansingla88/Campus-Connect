package com.campus.Campus_Connect.features.profile.service;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.repository.ConnectionRepository;
import com.campus.Campus_Connect.features.honor.repository.UserHonorRepository;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.profile.dto.request.UpdateUserProfileRequest;
import com.campus.Campus_Connect.features.profile.dto.response.UserProfileResponse;
import com.campus.Campus_Connect.features.profile.dto.response.ProfileStatsResponse;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import com.campus.Campus_Connect.features.settings.repository.UserPreferenceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.metadata.interest.repo.UserInterestRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final CourseRepository courseRepository;
    private final UserPreferenceRepository userPreferenceRepository;

    private final ClubMemberRepository clubMemberRepository;
    private final UserHonorRepository userHonorRepository;
    private final UserInterestRepository userInterestRepository;
    private final ConnectionRepository connectionRepository;


    public ApiResponse<UserProfileResponse> getMyProfile() {

        User currentUser = SecurityUtils.getCurrentUser();

        UserProfile profile = getUserProfileEntity(currentUser.getId());

        UserPreference preference = getUserPreference(currentUser.getId());

        UserProfileResponse response = buildUserProfileResponse(profile, preference);

        return ApiResponse.success(
                response,
                "Profile fetched successfully."

        );
    }


    public ApiResponse<UserProfileResponse> getUserProfile(Integer userId) {

        UserProfile profile = getUserProfileEntity(userId);

        UserPreference preference = getUserPreference(userId);

        UserProfileResponse response = buildUserProfileResponse(profile, preference);

        return ApiResponse.success(
                response,
                "Profile fetched successfully."

        );
    }


    public ApiResponse<UserProfileResponse> updateMyProfile(UpdateUserProfileRequest request) {

        User currentUser = SecurityUtils.getCurrentUser();

        UserProfile profile = getUserProfileEntity(currentUser.getId());

        applyUpdates(profile,request);

        userProfileRepository.save(profile);

        UserPreference preference = getUserPreference(currentUser.getId());

        UserProfileResponse response = buildUserProfileResponse(profile, preference);

        return ApiResponse.success(
                response,
                "Profile Updated successfully."
        );


    }

    public ApiResponse<ProfileStatsResponse> getMyProfileStats() {

        User currentUser = SecurityUtils.getCurrentUser();

        ProfileStatsResponse response =
                buildProfileStats(currentUser.getId());

        return ApiResponse.success(
                response,
                "Profile stats fetched successfully."
        );
    }

    public ApiResponse<ProfileStatsResponse> getProfileStats(Integer userId) {

        getUserProfileEntity(userId);

        ProfileStatsResponse response =
                buildProfileStats(userId);

        return ApiResponse.success(
                response,
                "Profile stats fetched successfully."
        );
    }




//    -----------------------------------------------------------------------------------\

    private UserProfile getUserProfileEntity(Integer userId) {

        return userProfileRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profile not found."));
    }
    //-------------------
    private UserPreference getUserPreference(Integer userId) {

        return userPreferenceRepository.findById(userId)
                .orElseGet(() -> {

                    UserProfile profile = getUserProfileEntity(userId);

                    UserPreference preference = UserPreference.builder()
                            .user(profile.getUser())
                            .build();

                    return userPreferenceRepository.save(preference);
                });
    }
    //-------------------
    private String formatMemberSince(LocalDateTime createdAt) {

        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("MMMM yyyy");

        return createdAt.format(formatter);
    }
    //-------------------
    // TODO: Move showPhone/showSocials privacy enforcement to the backend.
    // Currently filtered on the frontend.

    private UserProfileResponse buildUserProfileResponse(
            UserProfile profile,
            UserPreference preference
    ) {

        User user = profile.getUser();

        return UserProfileResponse.builder()
                // User
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())

                // Profile
                .fullName(profile.getFullName())
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .courseId(profile.getCourseId())
                .admissionYear(profile.getAdmissionYear())
                .hostel(profile.getHostel())
                .hometown(profile.getHometown())
                .gender(profile.getGender())
                .dob(profile.getDob() != null ? profile.getDob().toString() : null)
                .phone(profile.getPhone())
                .github(profile.getGithub())
                .linkedin(profile.getLinkedin())
                .instagram(profile.getInstagram())
                .memberSince(formatMemberSince(profile.getCreatedAt()))

                // Preferences
                .showPhone(preference.getShowPhone())
                .showSocials(preference.getShowSocials())

                .build();
    }
//---------------
private void applyUpdates(
        UserProfile profile,
        UpdateUserProfileRequest request
) {

    if (request.getFullName() != null) {
        profile.setFullName(request.getFullName());
    }

    if (request.getBio() != null) {
        profile.setBio(request.getBio());
    }

    if (request.getAvatarUrl() != null) {
        profile.setAvatarUrl(request.getAvatarUrl());
    }

    if (request.getCourseId() != null) {

        if (!courseRepository.existsById(request.getCourseId())) {
            throw new ResourceNotFoundException("Course not found.");
        }

        profile.setCourseId(request.getCourseId());
    }

    if (request.getAdmissionYear() != null) {
        profile.setAdmissionYear(request.getAdmissionYear());
    }

    if (request.getHostel() != null) {
        profile.setHostel(request.getHostel());
    }

    if (request.getHometown() != null) {
        profile.setHometown(request.getHometown());
    }

    if (request.getGender() != null) {
        profile.setGender(request.getGender());
    }

    if (request.getDob() != null) {
        profile.setDob(request.getDob());
    }

    if (request.getPhone() != null) {
        profile.setPhone(request.getPhone());
    }

    if (request.getGithub() != null) {
        profile.setGithub(request.getGithub());
    }

    if (request.getLinkedin() != null) {
        profile.setLinkedin(request.getLinkedin());
    }

    if (request.getInstagram() != null) {
        profile.setInstagram(request.getInstagram());
    }
}

//------------------------------------------------------------------------------------
private ProfileStatsResponse buildProfileStats(Integer userId) {

    return ProfileStatsResponse.builder()
            .connectionCount(getConnectionCount(userId))
            .clubCount(getClubCount(userId))
            .honorCount(getHonorCount(userId))
            .interestCount(getInterestCount(userId))
            .build();
}

    private Integer getConnectionCount(Integer userId) {
        return Math.toIntExact(
                connectionRepository.countConnectionsByUserIdAndStatus(
                        userId,
                        ConnectionStatus.CONNECTED
                )
        );
    }

    private Integer getClubCount(Integer userId) {

        return Math.toIntExact(
                clubMemberRepository.countByIdUserIdAndMemberStatus(
                        userId,
                        ClubMemberStatus.APPROVED
                )
        );
    }

    private Integer getHonorCount(Integer userId) {

        return Math.toIntExact(
                userHonorRepository.countByUser_Id(userId)
        );
    }

    private Integer getInterestCount(Integer userId) {

        return Math.toIntExact(
                userInterestRepository.countByIdUserId(userId)
        );
    }



}
