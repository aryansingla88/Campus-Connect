package com.campus.Campus_Connect.features.settings;

import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.settings.dto.UpdateUserPreferenceRequest;
import com.campus.Campus_Connect.features.settings.entity.UserPreference;
import com.campus.Campus_Connect.features.settings.repository.UserPreferenceRepository;
import com.campus.Campus_Connect.features.settings.service.UserPreferenceService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPreferencePrivacyTest {

    @Mock private UserPreferenceRepository repository;
    @InjectMocks private UserPreferenceService service;
    private final User user = User.builder().id(5).username("owner").build();

    @BeforeEach
    void authenticate() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of())
        );
        when(repository.save(any(UserPreference.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void missingPreferenceRecordUsesExpectedVisibilityDefaults() {
        when(repository.findById(5)).thenReturn(Optional.empty());

        var response = service.getMyPreferences().getData();

        assertFalse(response.getShowPhone());
        assertTrue(response.getShowSocials());
        assertFalse(response.getShowHometown());
        assertFalse(response.getShowGender());
        assertFalse(response.getShowAge());
        assertFalse(response.getShowHonor());
    }

    @Test
    void partialPreferenceUpdatePersistsOnlyProvidedVisibilityValues() {
        UserPreference existing = UserPreference.builder().userId(5).user(user)
                .showPhone(true).showSocials(false).showHometown(false).showGender(false)
                .showAge(false).showHonor(false).build();
        when(repository.findById(5)).thenReturn(Optional.of(existing));
        UpdateUserPreferenceRequest request = UpdateUserPreferenceRequest.builder()
                .showHometown(true).showGender(true).showAge(true).showHonor(true).build();

        var response = service.updateMyPreferences(request).getData();

        assertTrue(response.getShowPhone());
        assertFalse(response.getShowSocials());
        assertTrue(response.getShowHometown());
        assertTrue(response.getShowGender());
        assertTrue(response.getShowAge());
        assertTrue(response.getShowHonor());
        verify(repository).save(existing);
    }
}
