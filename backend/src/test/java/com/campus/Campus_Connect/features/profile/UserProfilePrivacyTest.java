package com.campus.Campus_Connect.features.profile;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.connection.ConnectionService;
import com.campus.Campus_Connect.features.connection.repository.ConnectionRepository;
import com.campus.Campus_Connect.features.honor.repository.UserHonorRepository;
import com.campus.Campus_Connect.features.metadata.courses.CourseRepository;
import com.campus.Campus_Connect.features.metadata.interest.repo.UserInterestRepository;
import com.campus.Campus_Connect.features.profile.dto.response.UserProfileResponse;
import com.campus.Campus_Connect.features.profile.dto.response.ProfileStatsResponse;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import com.campus.Campus_Connect.features.profile.repository.UserProfileRepository;
import com.campus.Campus_Connect.features.profile.service.UserProfileService;
import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import com.campus.Campus_Connect.features.settings.repository.UserPreferenceRepository;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.common.storage.MediaStorageService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfilePrivacyTest {

    @Mock private UserProfileRepository userProfileRepository;
    @Mock private CourseRepository courseRepository;
    @Mock private UserPreferenceRepository userPreferenceRepository;
    @Mock private ClubMemberRepository clubMemberRepository;
    @Mock private UserHonorRepository userHonorRepository;
    @Mock private UserInterestRepository userInterestRepository;
    @Mock private ConnectionRepository connectionRepository;
    @Mock private MediaStorageService mediaStorageService;
    @Mock private ConnectionService connectionService;
    @InjectMocks private UserProfileService service;

    private final User requester = User.builder().id(2).username("requester").build();

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(requester, null, List.of())
        );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ownerReceivesCompleteProfileRegardlessOfPreferences() {
        UserProfile profile = profile(2);
        when(userProfileRepository.findByUserIdWithUser(2)).thenReturn(Optional.of(profile));
        when(userPreferenceRepository.findById(2)).thenReturn(Optional.of(preferences(false)));

        UserProfileResponse response = service.getMyProfile().getData();

        assertEquals("person@example.com", response.getEmail());
        assertEquals("Hostel 1", response.getHostel());
        assertEquals("12345", response.getPhone());
        assertEquals("home", response.getHometown());
        assertEquals("X", response.getGender());
        assertEquals("2000-01-02", response.getDob());
        assertNotNull(response.getAge());
        assertEquals("git", response.getGithub());
    }

    @Test
    void publicProfileHidesDisabledFieldsAndNeverReturnsExactDob() {
        UserProfile profile = profile(9);
        when(userProfileRepository.findById(9)).thenReturn(Optional.of(profile));
        when(userPreferenceRepository.findById(9)).thenReturn(Optional.of(preferences(false)));

        UserProfileResponse response = service.getUserProfile(9).getData();

        assertEquals("person@example.com", response.getEmail());
        assertEquals("Hostel 1", response.getHostel());
        assertNull(response.getPhone());
        assertNull(response.getGithub());
        assertNull(response.getLinkedin());
        assertNull(response.getInstagram());
        assertNull(response.getHometown());
        assertNull(response.getGender());
        assertNull(response.getDob());
        assertNull(response.getAge());
    }

    @Test
    void publicProfileReturnsOnlyCalculatedAgeWhenEnabled() {
        UserProfile profile = profile(9);
        UserPreference preference = preferences(false);
        preference.setShowAge(true);
        preference.setShowPhone(true);
        preference.setShowSocials(true);
        preference.setShowHometown(true);
        preference.setShowGender(true);
        preference.setShowHonor(true);
        when(userProfileRepository.findById(9)).thenReturn(Optional.of(profile));
        when(userPreferenceRepository.findById(9)).thenReturn(Optional.of(preference));

        UserProfileResponse response = service.getUserProfile(9).getData();

        assertEquals(Period.between(profile.getDob(), LocalDate.now()).getYears(), response.getAge());
        assertNull(response.getDob());
        assertEquals("12345", response.getPhone());
        assertEquals("git", response.getGithub());
        assertEquals("home", response.getHometown());
        assertEquals("X", response.getGender());
        assertTrue(response.getShowHonor());
        assertNotNull(response.getHeaderBadges());
        assertNotNull(response.getHeaderMedals());
    }

    @Test
    void profileUpdateRequestDoesNotExposeReadOnlyFields() {
        var fieldNames = java.util.Arrays.stream(
                        com.campus.Campus_Connect.features.profile.dto.request.UpdateUserProfileRequest.class.getDeclaredFields()
                ).map(java.lang.reflect.Field::getName).toList();
        assertFalse(fieldNames.contains("fullName"));
        assertFalse(fieldNames.contains("courseId"));
        assertFalse(fieldNames.contains("admissionYear"));
        assertFalse(fieldNames.contains("gender"));
        assertFalse(fieldNames.contains("dob"));
    }

    @Test
    void showHonorFiltersOnlyHeaderDataAndDoesNotChangeHonorStatistics() {
        UserProfile profile = profile(9);
        UserPreference preference = preferences(false);
        when(userProfileRepository.findById(9)).thenReturn(Optional.of(profile));
        when(userPreferenceRepository.findById(9)).thenReturn(Optional.of(preference));
        when(userHonorRepository.countByUser_Id(9)).thenReturn(3L);

        UserProfileResponse response = service.getUserProfile(9).getData();
        ProfileStatsResponse stats = service.getProfileStats(9).getData();

        assertTrue(response.getHeaderBadges().isEmpty());
        assertTrue(response.getHeaderMedals().isEmpty());
        assertEquals(3, stats.getHonorCount());
        verify(userHonorRepository, never()).findByUser_IdAndHonor_TypeOrderByPriorityAsc(eq(9), any());
    }

    @Test
    void missingPreferenceDefaultsHideNewFieldsAndPreserveExistingDefaults() {
        UserPreference defaults = UserPreference.builder().build();
        assertFalse(defaults.getShowPhone());
        assertTrue(defaults.getShowSocials());
        assertFalse(defaults.getShowHometown());
        assertFalse(defaults.getShowGender());
        assertFalse(defaults.getShowAge());
        assertFalse(defaults.getShowHonor());
    }

    @Test
    void changedPreferenceIsAppliedToTheNextPublicProfileResponse() {
        UserProfile profile = profile(9);
        UserPreference preference = preferences(false);
        when(userProfileRepository.findById(9)).thenReturn(Optional.of(profile));
        when(userPreferenceRepository.findById(9)).thenReturn(Optional.of(preference));

        UserProfileResponse hidden = service.getUserProfile(9).getData();
        preference.setShowPhone(true);
        UserProfileResponse visible = service.getUserProfile(9).getData();

        assertNull(hidden.getPhone());
        assertEquals("12345", visible.getPhone());
    }

    private UserProfile profile(Integer id) {
        User user = User.builder().id(id).username("person").email("person@example.com").build();
        return UserProfile.builder()
                .userId(id).user(user).fullName("Person").courseId(1).admissionYear(2020)
                .hostel("Hostel 1").hometown("home").gender("X")
                .dob(LocalDate.of(2000, 1, 2)).phone("12345").github("git")
                .linkedin("linkedin").instagram("instagram").createdAt(LocalDateTime.now())
                .build();
    }

    private UserPreference preferences(boolean showHonor) {
        return UserPreference.builder().userId(9).user(User.builder().id(9).build())
                .showPhone(false).showSocials(false).showHometown(false).showGender(false)
                .showAge(false).showHonor(showHonor).build();
    }
}
