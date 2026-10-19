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
import com.campus.Campus_Connect.features.registration.dto.request.CreateTeamInvitationRequest;
import com.campus.Campus_Connect.features.registration.dto.request.CreateTeamJoinRequest;
import com.campus.Campus_Connect.features.registration.dto.response.TeamRequestResponse;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import com.campus.Campus_Connect.features.registration.entity.RegistrationConfig;
import com.campus.Campus_Connect.features.registration.entity.TeamRequest;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestInitiator;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationTeamWorkflowTest {
    private static final int EVENT_ID = 3;
    private static final int REGISTRATION_ID = 19;
    private static final int TEAM_ID = 7;

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
    private User leader;
    private Event event;
    private EventTeam team;
    private EventRegistration registration;
    private RegistrationConfig config;

    @BeforeEach
    void setUp() {
        participant = User.builder().id(10).username("participant").build();
        leader = User.builder().id(20).username("leader").build();
        event = Event.builder().id(EVENT_ID).approvalStatus(ApprovalStatus.VERIFIED)
                .eventState(EventState.UPCOMING).registrationType(RegistrationType.THROUGH_APP).build();
        team = EventTeam.builder().id(TEAM_ID).event(event).leader(leader).teamName("Team").build();
        registration = EventRegistration.builder().id(REGISTRATION_ID).event(event).user(participant)
                .status(RegistrationStatus.PENDING).build();
        EventRegistration leaderRegistration = EventRegistration.builder().id(20).event(event).user(leader)
                .team(team).status(RegistrationStatus.PENDING).build();
        lenient().when(registrationRepository.findByEventIdAndUserId(EVENT_ID, leader.getId()))
                .thenReturn(Optional.of(leaderRegistration));
        config = RegistrationConfig.builder().eventId(EVENT_ID).event(event).minTeamMembers(1).maxTeamMembers(3)
                .quickFieldMask(0L).autoConfirm(true).build();
        lenient().when(configRepository.findByEventId(EVENT_ID)).thenReturn(Optional.of(config));
        lenient().when(teamRequestRepository.save(any(TeamRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));
        lenient().when(registrationRepository.save(any(EventRegistration.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void participantCreatesJoinRequestWithoutAssigningTeam() {
        authenticate(participant);
        CreateTeamJoinRequest body = new CreateTeamJoinRequest();
        body.setTeamId(TEAM_ID);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(teamRequestRepository.findByRegistration_IdAndStatus(REGISTRATION_ID, TeamRequestStatus.PENDING))
                .thenReturn(Optional.empty());

        ApiResponse<TeamRequestResponse> response = service.createTeamJoinRequest(REGISTRATION_ID, body);

        assertTrue(response.isSuccess());
        assertEquals("PARTICIPANT", response.getData().getInitiatedBy());
        assertEquals("PENDING", response.getData().getStatus());
        assertNull(registration.getTeam());
        verify(teamRequestRepository).save(argThat(saved -> saved.getTeam().getId().equals(TEAM_ID)
                && saved.getRegistration().getId().equals(REGISTRATION_ID)));
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void repeatedJoinRequestIsIdempotentAndConflictingPendingRequestIsRejected() {
        authenticate(participant);
        CreateTeamJoinRequest body = new CreateTeamJoinRequest();
        body.setTeamId(TEAM_ID);
        TeamRequest existing = request(TeamRequestInitiator.PARTICIPANT, TeamRequestStatus.PENDING, team, registration);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        when(teamRequestRepository.findByRegistration_IdAndStatus(REGISTRATION_ID, TeamRequestStatus.PENDING))
                .thenReturn(Optional.of(existing));

        ApiResponse<TeamRequestResponse> response = service.createTeamJoinRequest(REGISTRATION_ID, body);

        assertTrue(response.isSuccess());
        assertEquals("Join request is already pending.", response.getMessage());
        verify(teamRequestRepository, never()).save(any());
    }

    @Test
    void crossEventJoinRequestIsRejected() {
        authenticate(participant);
        Event otherEvent = Event.builder().id(4).approvalStatus(ApprovalStatus.VERIFIED)
                .eventState(EventState.UPCOMING).registrationType(RegistrationType.THROUGH_APP).build();
        EventTeam otherTeam = EventTeam.builder().id(8).event(otherEvent).leader(leader).teamName("Other").build();
        CreateTeamJoinRequest body = new CreateTeamJoinRequest();
        body.setTeamId(8);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRepository.findById(8)).thenReturn(Optional.of(otherTeam));

        ApiResponse<TeamRequestResponse> response = service.createTeamJoinRequest(REGISTRATION_ID, body);

        assertFalse(response.isSuccess());
        assertEquals("Team and registration must belong to the same event.", response.getMessage());
        verify(teamRequestRepository, never()).save(any());
    }

    @Test
    void leaderCanInviteButNonLeaderCannot() {
        CreateTeamInvitationRequest body = new CreateTeamInvitationRequest();
        body.setRegistrationId(REGISTRATION_ID);
        authenticate(participant);
        when(teamRepository.findById(TEAM_ID)).thenReturn(Optional.of(team));
        ApiResponse<TeamRequestResponse> denied = service.createTeamInvitation(TEAM_ID, body);
        assertFalse(denied.isSuccess());
        assertEquals("Only the team leader can send invitations.", denied.getMessage());
        verify(registrationRepository, never()).findByIdForUpdate(any());

        authenticate(leader);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRequestRepository.findByRegistration_IdAndStatus(REGISTRATION_ID, TeamRequestStatus.PENDING))
                .thenReturn(Optional.empty());
        ApiResponse<TeamRequestResponse> allowed = service.createTeamInvitation(TEAM_ID, body);
        assertTrue(allowed.isSuccess());
        assertEquals("LEADER", allowed.getData().getInitiatedBy());
        verify(teamRequestRepository).save(argThat(saved -> saved.getInitiatedBy() == TeamRequestInitiator.LEADER));
    }

    @Test
    void onlyCorrectRecipientCanAcceptAndAcceptanceAssignsMembershipTransactionally() {
        TeamRequest request = request(TeamRequestInitiator.PARTICIPANT, TeamRequestStatus.PENDING, team, registration);
        when(teamRequestRepository.findById(31)).thenReturn(Optional.of(request));
        when(teamRequestRepository.findByIdForUpdate(31)).thenReturn(Optional.of(request));
        when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(team));
        authenticate(participant);
        ApiResponse<TeamRequestResponse> denied = service.acceptTeamRequest(31);
        assertFalse(denied.isSuccess());
        verify(registrationRepository, never()).findByIdForUpdate(any());

        authenticate(leader);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(team));
        when(registrationRepository.countByEventIdAndTeamIdAndStatusIn(EVENT_ID, TEAM_ID,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED))).thenReturn(1L);

        ApiResponse<TeamRequestResponse> accepted = service.acceptTeamRequest(31);

        assertTrue(accepted.isSuccess());
        assertSame(team, registration.getTeam());
        assertEquals(TeamRequestStatus.ACCEPTED, request.getStatus());
        InOrder locks = inOrder(teamRepository, teamRequestRepository, registrationRepository);
        locks.verify(teamRepository).findByIdForUpdate(TEAM_ID);
        locks.verify(teamRequestRepository).findByIdForUpdate(31);
        locks.verify(registrationRepository).findByIdForUpdate(REGISTRATION_ID);
        verify(registrationRepository).save(registration);
    }

    @Test
    void acceptanceFailsIfLeaderCancellationAlreadyRemovedTheTeam() {
        TeamRequest request = request(TeamRequestInitiator.PARTICIPANT, TeamRequestStatus.PENDING, team, registration);
        when(teamRequestRepository.findById(31)).thenReturn(Optional.of(request));
        authenticate(leader);
        when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.empty());

        ApiResponse<TeamRequestResponse> response = service.acceptTeamRequest(31);

        assertFalse(response.isSuccess());
        assertEquals("Team request is no longer valid.", response.getMessage());
        verify(registrationRepository, never()).findByIdForUpdate(any());
    }

    @Test
    void leaderCancellationRechecksMembershipUnderTeamThenRegistrationLocks() {
        EventRegistration leaderRegistration = EventRegistration.builder().id(20).event(event).user(leader)
                .team(team).status(RegistrationStatus.PENDING).build();
        authenticate(leader);
        when(registrationRepository.findByEventIdAndUserId(EVENT_ID, leader.getId()))
                .thenReturn(Optional.of(leaderRegistration));
        when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(team));
        when(registrationRepository.findByIdForUpdate(20)).thenReturn(Optional.of(leaderRegistration));
        when(registrationRepository.countByEventIdAndTeamIdAndStatusIn(EVENT_ID, TEAM_ID,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED)))
                .thenReturn(2L);

        ApiResponse<Void> response = service.cancelRegistration(EVENT_ID);

        assertFalse(response.isSuccess());
        assertEquals("Team leader cannot cancel while other participants remain assigned.", response.getMessage());
        InOrder locks = inOrder(teamRepository, registrationRepository);
        locks.verify(teamRepository).findByIdForUpdate(TEAM_ID);
        locks.verify(registrationRepository).findByIdForUpdate(20);
        verify(teamRepository, never()).delete(any(EventTeam.class));
        verify(registrationRepository, never()).delete(any(EventRegistration.class));
    }

    @Test
    void fullTeamRejectsAcceptanceAndClosesStaleRequest() {
        TeamRequest request = request(TeamRequestInitiator.LEADER, TeamRequestStatus.PENDING, team, registration);
        when(teamRequestRepository.findById(31)).thenReturn(Optional.of(request));
        when(teamRequestRepository.findByIdForUpdate(31)).thenReturn(Optional.of(request));
        authenticate(participant);
        when(registrationRepository.findByIdForUpdate(REGISTRATION_ID)).thenReturn(Optional.of(registration));
        when(teamRepository.findByIdForUpdate(TEAM_ID)).thenReturn(Optional.of(team));
        when(registrationRepository.countByEventIdAndTeamIdAndStatusIn(EVENT_ID, TEAM_ID,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED))).thenReturn(3L);

        ApiResponse<TeamRequestResponse> response = service.acceptTeamRequest(31);

        assertFalse(response.isSuccess());
        assertEquals("Team is full.", response.getMessage());
        assertEquals(TeamRequestStatus.CANCELLED, request.getStatus());
        assertNull(registration.getTeam());
        verify(registrationRepository, never()).save(any());
    }

    @Test
    void rejectingOrCancellingTeamRequestDoesNotClearExistingAssignment() {
        EventTeam existing = EventTeam.builder().id(9).event(event).leader(leader).teamName("Existing").build();
        registration.setTeam(existing);
        TeamRequest request = request(TeamRequestInitiator.PARTICIPANT, TeamRequestStatus.PENDING, team, registration);
        when(teamRequestRepository.findByIdForUpdate(31)).thenReturn(Optional.of(request));
        authenticate(leader);

        ApiResponse<TeamRequestResponse> rejected = service.rejectTeamRequest(31);

        assertTrue(rejected.isSuccess());
        assertSame(existing, registration.getTeam());

        request.setStatus(TeamRequestStatus.PENDING);
        authenticate(participant);
        ApiResponse<TeamRequestResponse> cancelled = service.cancelTeamRequest(31);
        assertTrue(cancelled.isSuccess());
        assertSame(existing, registration.getTeam());
        verify(registrationRepository, never()).save(any());
    }

    private TeamRequest request(TeamRequestInitiator initiator, TeamRequestStatus status,
                                EventTeam requestTeam, EventRegistration requestRegistration) {
        return TeamRequest.builder().id(31).team(requestTeam).registration(requestRegistration)
                .initiatedBy(initiator).status(status).build();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user, "test"));
    }
}
