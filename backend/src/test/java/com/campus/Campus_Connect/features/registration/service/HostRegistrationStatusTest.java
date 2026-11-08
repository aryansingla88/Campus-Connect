package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HostRegistrationStatusTest {
    @Mock private EventRepository eventRepository;
    @Mock private EventTeamRepository teamRepository;
    @Mock private RegistrationConfigRepository configRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private RegistrationAnswerRepository answerRepository;
    @Mock private RegistrationFieldRepository fieldRepository;
    @Mock private RegistrationFieldOptionRepository optionRepository;
    @InjectMocks private HostRegistrationService service;

    @AfterEach
    void clearSecurityContext() { SecurityContextHolder.clearContext(); }

    @Test
    void hostCannotApproveOrRejectUnsubmittedDraft() {
        User host = User.builder().id(1).username("host").build();
        Event event = Event.builder().id(3).creator(host).build();
        EventRegistration draft = EventRegistration.builder().id(19).event(event)
                .status(RegistrationStatus.PENDING).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(host, "test"));
        when(eventRepository.findById(3)).thenReturn(Optional.of(event));
        when(registrationRepository.findByIdForUpdate(19)).thenReturn(Optional.of(draft));

        ApiResponse<RegistrationResponse> approved = service.approveRegistration(3, 19);
        ApiResponse<RegistrationResponse> rejected = service.rejectRegistration(3, 19);

        assertFalse(approved.isSuccess());
        assertFalse(rejected.isSuccess());
        assertEquals(RegistrationStatus.PENDING, draft.getStatus());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void hostCanReviewSubmittedRegistrationAwaitingApproval() {
        User host = User.builder().id(1).username("host").build();
        Event event = Event.builder().id(3).creator(host).build();
        EventRegistration submitted = EventRegistration.builder().id(19).event(event)
                .status(RegistrationStatus.WAITLISTED).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(host, "test"));
        when(eventRepository.findById(3)).thenReturn(Optional.of(event));
        when(registrationRepository.findByIdForUpdate(19)).thenReturn(Optional.of(submitted));

        ApiResponse<RegistrationResponse> response = service.approveRegistration(3, 19);

        assertTrue(response.isSuccess());
        assertEquals(RegistrationStatus.CONFIRMED, submitted.getStatus());
        verify(registrationRepository).save(submitted);
    }

    @Test
    void hostApprovalUpdatesEveryRegistrationOnTheTeam() {
        User host = User.builder().id(1).username("host").build();
        User leader = User.builder().id(2).username("leader").build();
        User member = User.builder().id(3).username("member").build();
        Event event = Event.builder().id(3).creator(host).build();
        EventTeam team = EventTeam.builder().id(8).event(event).leader(leader).teamName("Team").build();
        EventRegistration leaderRegistration = EventRegistration.builder().id(19).event(event).user(leader)
                .team(team).status(RegistrationStatus.WAITLISTED).build();
        EventRegistration memberRegistration = EventRegistration.builder().id(20).event(event).user(member)
                .team(team).status(RegistrationStatus.WAITLISTED).build();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(host, "test"));
        when(eventRepository.findById(3)).thenReturn(Optional.of(event));
        when(registrationRepository.findById(19)).thenReturn(Optional.of(leaderRegistration));
        when(teamRepository.findByIdForUpdate(8)).thenReturn(Optional.of(team));
        when(registrationRepository.findByIdForUpdate(19)).thenReturn(Optional.of(leaderRegistration));
        when(registrationRepository.findByEventIdAndUserId(3, leader.getId())).thenReturn(Optional.of(leaderRegistration));
        when(registrationRepository.findByEventIdAndTeamId(3, 8)).thenReturn(List.of(leaderRegistration, memberRegistration));

        ApiResponse<RegistrationResponse> response = service.approveRegistration(3, 19);

        assertTrue(response.isSuccess());
        assertEquals(RegistrationStatus.CONFIRMED, leaderRegistration.getStatus());
        assertEquals(RegistrationStatus.CONFIRMED, memberRegistration.getStatus());
        verify(registrationRepository).saveAll(List.of(leaderRegistration, memberRegistration));
    }
}
