package com.campus.Campus_Connect.features.profile.service;

import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.connection.ConnectionService;
import com.campus.Campus_Connect.features.connection.dto.ConnectionRelationshipStatus;
import com.campus.Campus_Connect.features.connection.entity.ConnectionStatus;
import com.campus.Campus_Connect.features.connection.repository.ConnectionRepository;
import com.campus.Campus_Connect.features.honor.repository.UserHonorRepository;
import com.campus.Campus_Connect.features.honor.dto.response.HonorResponse;
import com.campus.Campus_Connect.features.honor.entity.UserHonor;
import com.campus.Campus_Connect.features.honor.enums.HonorType;
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
import com.campus.Campus_Connect.common.storage.MediaStorageService;
import com.campus.Campus_Connect.common.storage.MediaType;
import org.springframework.web.multipart.MultipartFile;



import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.metadata.interest.repo.UserInterestRepository;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.Period;
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

    private final MediaStorageService mediaStorageService;
    private final ConnectionService connectionService;

    public ApiResponse<UserProfileResponse> getMyProfile() {

        User currentUser = SecurityUtils.getCurrentUser();

        UserProfile profile = userProfileRepository
                .findByUserIdWithUser(currentUser.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Profile not found."));

        UserPreference preference = getUserPreference(currentUser.getId());

        UserProfileResponse response = buildUserProfileResponse(profile, preference, null, true);

        return ApiResponse.success(
                response,
                "Profile fetched successfully."

        );
    }


    public ApiResponse<UserProfileResponse> getUserProfile(Integer userId) {

        UserProfile profile = getUserProfileEntity(userId);

        UserPreference preference = getUserPreference(profile.getUserId());

        User currentUser = SecurityUtils.getCurrentUser();
        boolean isOwner = currentUser.getId().equals(profile.getUserId());

        ConnectionRelationshipStatus relationshipStatus =
                connectionService.getRelationshipStatus(
                        currentUser.getId(),
                        userId
                );

        return ApiResponse.success(
                buildUserProfileResponse(
                        profile,
                        preference,
                        relationshipStatus,
                        isOwner
                ),
                "Profile fetched successfully."
        );
    }


    public ApiResponse<UserProfileResponse> updateMyProfile(
            UpdateUserProfileRequest request,
            MultipartFile image
    ) {

        User currentUser = SecurityUtils.getCurrentUser();

        UserProfile profile =
                getUserProfileEntity(currentUser.getId());

        applyUpdates(profile, request);

        if (image != null && !image.isEmpty()) {

            String avatarUrl =
                    mediaStorageService.store(
                            image,
                            MediaType.PROFILE_IMAGE
                    );

            profile.setAvatarUrl(avatarUrl);
        }

        userProfileRepository.save(profile);

        UserPreference preference =
                getUserPreference(currentUser.getId());

        UserProfileResponse response =
                buildUserProfileResponse(profile, preference, null, true);

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
    private UserProfileResponse buildUserProfileResponse(
            UserProfile profile,
            UserPreference preference,
            ConnectionRelationshipStatus relationshipStatus,
            boolean isOwner
    ) {

        User user = profile.getUser();
        boolean showPhone = isOwner || Boolean.TRUE.equals(preference.getShowPhone());
        boolean showSocials = isOwner || Boolean.TRUE.equals(preference.getShowSocials());
        boolean showHometown = isOwner || Boolean.TRUE.equals(preference.getShowHometown());
        boolean showGender = isOwner || Boolean.TRUE.equals(preference.getShowGender());
        boolean showAge = isOwner || Boolean.TRUE.equals(preference.getShowAge());
        boolean showHonor = isOwner || Boolean.TRUE.equals(preference.getShowHonor());

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
                .hometown(showHometown ? profile.getHometown() : null)
                .gender(showGender ? profile.getGender() : null)
                .dob(isOwner && profile.getDob() != null ? profile.getDob().toString() : null)
                .age(showAge ? calculateAge(profile.getDob()) : null)
                .phone(showPhone ? profile.getPhone() : null)
                .github(showSocials ? profile.getGithub() : null)
                .linkedin(showSocials ? profile.getLinkedin() : null)
                .instagram(showSocials ? profile.getInstagram() : null)
                .memberSince(formatMemberSince(profile.getCreatedAt()))
                .relationshipStatus(relationshipStatus)

                // Preferences
                .showPhone(showPhone)
                .showSocials(showSocials)
                .showHometown(showHometown)
                .showGender(showGender)
                .showAge(showAge)
                .showHonor(showHonor)
                .headerBadges(showHonor ? getHeaderHonors(profile.getUserId(), HonorType.BADGE) : java.util.List.of())
                .headerMedals(showHonor ? getHeaderHonors(profile.getUserId(), HonorType.MEDAL) : java.util.List.of())

                .build();
    }

    private Integer calculateAge(LocalDate dob) {
        return dob == null ? null : Period.between(dob, LocalDate.now()).getYears();
    }

    private java.util.List<HonorResponse> getHeaderHonors(Integer userId, HonorType type) {
        return userHonorRepository.findByUser_IdAndHonor_TypeOrderByPriorityAsc(userId, type)
                .stream()
                .map(this::toHonorResponse)
                .toList();
    }

    private HonorResponse toHonorResponse(UserHonor userHonor) {
        var honor = userHonor.getHonor();
        return HonorResponse.builder()
                .honorId(honor.getId())
                .type(honor.getType())
                .title(honor.getTitle())
                .subtitle(honor.getSubtitle())
                .iconUrl(honor.getIconUrl())
                .eventId(honor.getEvent() == null ? null : honor.getEvent().getId())
                .priority(userHonor.getPriority())
                .awardedAt(userHonor.getAwardedAt())
                .build();
    }
//---------------
private void applyUpdates(
            UserProfile profile,
            UpdateUserProfileRequest request
    ) {
        if (request.getBio() != null) {
            profile.setBio(request.getBio());
        }

        if (request.getAvatarUrl() != null) {
            profile.setAvatarUrl(request.getAvatarUrl());
        }

        if (request.getHostel() != null) {
            profile.setHostel(request.getHostel());
        }

        if (request.getHometown() != null) {
            profile.setHometown(request.getHometown());
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
