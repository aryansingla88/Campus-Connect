package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.registration.dto.request.QuickFieldsConfigRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationConfigResponse;
import com.campus.Campus_Connect.features.registration.entity.RegistrationConfig;
import com.campus.Campus_Connect.features.registration.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostRegistrationServiceQuickFieldsTest {

    private static final int EVENT_ID = 3;

    @Mock private EventRepository eventRepository;
    @Mock private RegistrationConfigRepository configRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private RegistrationAnswerRepository answerRepository;
    @Mock private RegistrationFieldRepository fieldRepository;
    @Mock private RegistrationFieldOptionRepository optionRepository;

    @InjectMocks private HostRegistrationService service;

    private User organizer;
    private Event event;
    private RegistrationConfig config;

    @BeforeEach
    void setUp() {
        organizer = User.builder().id(7).build();
        event = Event.builder().id(EVENT_ID).creator(organizer).build();
        config = RegistrationConfig.builder()
                .event(event)
                .eventId(EVENT_ID)
                .minTeamMembers(2)
                .maxTeamMembers(6)
                .quickFieldMask(1L)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(organizer, "test"));
        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void patchChangesOnlyQuickFieldMaskAndReturnsUpdatedConfig() {
        when(configRepository.findByEventId(EVENT_ID)).thenReturn(Optional.of(config));
        when(configRepository.save(any(RegistrationConfig.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<RegistrationConfigResponse> response =
                service.updateQuickFieldsConfig(EVENT_ID, new QuickFieldsConfigRequest(12L));

        assertTrue(response.isSuccess());
        assertEquals(12L, response.getData().getQuickFieldMask());
        assertEquals(2, config.getMinTeamMembers());
        assertEquals(6, config.getMaxTeamMembers());
        verify(configRepository).save(config);
    }

    @Test
    void patchRejectsInvalidMaskWithoutSaving() {
        ApiResponse<RegistrationConfigResponse> response = service.updateQuickFieldsConfig(
                EVENT_ID, new QuickFieldsConfigRequest(1L << 12));

        assertFalse(response.isSuccess());
        assertEquals(1L, config.getQuickFieldMask());
        verify(configRepository, never()).save(any());
    }

    @Test
    void patchRejectsNonOrganizer() {
        User otherUser = User.builder().id(8).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(otherUser, "test"));

        ApiResponse<RegistrationConfigResponse> response = service.updateQuickFieldsConfig(
                EVENT_ID, new QuickFieldsConfigRequest(2L));

        assertFalse(response.isSuccess());
        verify(configRepository, never()).findByEventId(any());
        verify(configRepository, never()).save(any());
    }
}
