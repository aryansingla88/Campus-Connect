package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.entity.*;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationFinalizationTest {
    private static final int EVENT_ID = 3;
    private static final int REGISTRATION_ID = 19;

    @Mock private EventRepository eventRepository;
    @Mock private RegistrationConfigRepository configRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private RegistrationFieldRepository fieldRepository;
    @Mock private RegistrationAnswerRepository answerRepository;
    @Mock private RegistrationFieldOptionRepository optionRepository;
    @Mock private EventTeamRepository teamRepository;
    @Mock private TeamRequestRepository teamRequestRepository;
    @InjectMocks private UserRegistrationService service;

    private User participant;
    private Event event;
    private EventRegistration registration;
    private RegistrationConfig config;

    @BeforeEach
    void setUp() {
        participant = User.builder().id(10).username("participant").build();
        event = Event.builder().id(EVENT_ID).approvalStatus(ApprovalStatus.VERIFIED)
                .eventState(EventState.UPCOMING).registrationType(RegistrationType.THROUGH_APP).build();
        registration = EventRegistration.builder().id(REGISTRATION_ID).event(event).user(participant)
                .status(RegistrationStatus.PENDING).build();
        config = RegistrationConfig.builder().eventId(EVENT_ID).event(event).minTeamMembers(1).maxTeamMembers(1)
                .quickFieldMask(0L).autoConfirm(true).build();
        authenticate(participant);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        lenient().when(configRepository.findByEventId(EVENT_ID)).thenReturn(Optional.of(config));
        lenient().when(answerRepository.findByIdRegistrationId(REGISTRATION_ID)).thenReturn(List.of());
        lenient().when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(EVENT_ID)).thenReturn(List.of());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autoConfirmTrueConfirmsEligibleRegistration() {
        ApiResponse<RegistrationResponse> response = service.finalizeRegistration(REGISTRATION_ID);
        assertTrue(response.isSuccess());
        assertEquals(RegistrationStatus.CONFIRMED, registration.getStatus());
        assertEquals("CONFIRMED", response.getData().getStatus());
    }

    @Test
    void autoConfirmFalseSubmitsRegistrationAsAwaitingHostApproval() {
        config.setAutoConfirm(false);

        ApiResponse<RegistrationResponse> response = service.finalizeRegistration(REGISTRATION_ID);

        assertTrue(response.isSuccess());
        assertEquals(RegistrationStatus.WAITLISTED, registration.getStatus());
        assertEquals("WAITLISTED", response.getData().getStatus());
        assertEquals("Registration submitted for host approval.", response.getMessage());
    }

    @Test
    void soloDraftMustContainRequiredSharedQuestionAnswer() {
        RegistrationField sharedQuestion = RegistrationField.builder().id(57).event(event).individual(false)
                .fieldLabel("Shared goal").fieldType("TEXT").required(true).build();
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(EVENT_ID)).thenReturn(List.of(sharedQuestion));

        ApiResponse<RegistrationResponse> response = service.finalizeRegistration(REGISTRATION_ID);

        assertFalse(response.isSuccess());
        assertEquals("Required field missing: Shared goal", response.getMessage());
        assertEquals(RegistrationStatus.PENDING, registration.getStatus());
    }

    @Test
    void submittedRegistrationCannotBeFinalizedAgainAsDraft() {
        registration.setStatus(RegistrationStatus.WAITLISTED);
        ApiResponse<RegistrationResponse> response = service.finalizeRegistration(REGISTRATION_ID);
        assertFalse(response.isSuccess());
        assertEquals("Editable registration not found.", response.getMessage());
        assertEquals(RegistrationStatus.WAITLISTED, registration.getStatus());
    }

    @Test
    void requiredTeamAnswersAreValidatedAgainstLeaderRegistration() {
        User leader = participant;
        User member = User.builder().id(11).username("member").build();
        EventTeam team = EventTeam.builder().id(8).event(event).leader(leader).teamName("Team").build();
        EventRegistration leaderRegistration = EventRegistration.builder().id(20).event(event).user(leader)
                .team(team).status(RegistrationStatus.PENDING).build();
        registration.setUser(member);
        authenticate(member);
        registration.setTeam(team);
        config.setMinTeamMembers(2);
        config.setMaxTeamMembers(4);
        RegistrationField teamQuestion = RegistrationField.builder().id(56).event(event).individual(false)
                .fieldLabel("Team statement").fieldType("TEXT").required(true).build();
        when(registrationRepository.findByEventIdAndUserId(EVENT_ID, leader.getId())).thenReturn(Optional.of(leaderRegistration));
        when(registrationRepository.countByEventIdAndTeamIdAndStatusIn(
                EVENT_ID, team.getId(), List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED))).thenReturn(2L);
        when(answerRepository.findByIdRegistrationId(20)).thenReturn(List.of());
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(EVENT_ID)).thenReturn(List.of(teamQuestion));

        ApiResponse<RegistrationResponse> response = service.finalizeRegistration(REGISTRATION_ID);

        assertFalse(response.isSuccess());
        assertEquals("Required field missing: Team statement", response.getMessage());
        assertEquals(RegistrationStatus.PENDING, registration.getStatus());
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "test"));
    }
}
