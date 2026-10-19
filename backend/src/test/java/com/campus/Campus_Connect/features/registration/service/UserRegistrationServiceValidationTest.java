package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.registration.dto.request.AnswerType;
import com.campus.Campus_Connect.features.registration.dto.request.SaveRegistrationAnswersRequest;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import com.campus.Campus_Connect.features.registration.entity.RegistrationAnswer;
import com.campus.Campus_Connect.features.registration.entity.RegistrationField;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegistrationServiceValidationTest {
    @Mock private EventRepository eventRepository;
    @Mock private RegistrationConfigRepository configRepository;
    @Mock private EventRegistrationRepository registrationRepository;
    @Mock private RegistrationFieldRepository fieldRepository;
    @Mock private RegistrationAnswerRepository answerRepository;
    @Mock private RegistrationFieldOptionRepository optionRepository;
    @Mock private EventTeamRepository teamRepository;
    @InjectMocks private UserRegistrationService service;

    @AfterEach void clearAuth() { SecurityContextHolder.clearContext(); }

    @Test
    void unauthenticatedEventRegistrationLookupReturnsFailureInsteadOfThrowing() {
        SecurityContextHolder.clearContext();
        ApiResponse<RegistrationResponse> response = service.getRegistration(3);
        assertFalse(response.isSuccess());
        assertEquals("User is not authenticated.", response.getMessage());
        verifyNoInteractions(eventRepository, registrationRepository);
    }

    @Test
    void onlyPendingDraftCanBeResumed() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).approvalStatus(com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus.VERIFIED)
                .eventState(com.campus.Campus_Connect.features.event.entity.enums.EventState.UPCOMING)
                .registrationType(com.campus.Campus_Connect.features.event.entity.enums.RegistrationType.THROUGH_APP).build();
        when(eventRepository.findById(3)).thenReturn(Optional.of(event));
        when(configRepository.findByEventId(3)).thenReturn(Optional.of(
                com.campus.Campus_Connect.features.registration.entity.RegistrationConfig.builder()
                        .eventId(3).event(event).minTeamMembers(1).maxTeamMembers(1).build()));
        EventRegistration draft = EventRegistration.builder().id(19).event(event).user(participant)
                .status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(draft));

        ApiResponse<RegistrationResponse> resumed = service.registerForEvent(3, new RegistrationRequest());
        assertTrue(resumed.isSuccess());
        assertEquals("PENDING", resumed.getData().getStatus());

        draft.setStatus(RegistrationStatus.WAITLISTED);
        ApiResponse<RegistrationResponse> submitted = service.registerForEvent(3, new RegistrationRequest());
        assertFalse(submitted.isSuccess());
        assertEquals("WAITLISTED", draft.getStatus().name());
        verify(registrationRepository, never()).save(any(EventRegistration.class));
    }

    @Test
    void submittedRegistrationCannotBeEditedAsDraft() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        EventRegistration submitted = EventRegistration.builder().id(19).event(Event.builder().id(3).build())
                .user(participant).status(RegistrationStatus.WAITLISTED).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(submitted));

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                request(AnswerType.INDIVIDUAL, List.of(answer(55, "changed"))));

        assertFalse(response.isSuccess());
        assertEquals("Editable registration not found.", response.getMessage());
        verifyNoInteractions(fieldRepository, answerRepository);
    }

    @Test
    void pendingDraftCanBeCancelled() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventRegistration draft = EventRegistration.builder().id(19).event(event).user(participant)
                .status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(draft));
        when(registrationRepository.findByIdForUpdate(19)).thenReturn(Optional.of(draft));

        ApiResponse<Void> response = service.cancelRegistration(3);

        assertTrue(response.isSuccess());
        verify(registrationRepository).delete(draft);
    }

    @Test
    void participantRetrievesTheirSavedIndividualAnswersButNotTeamAnswers() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventTeam team = EventTeam.builder().id(7).event(event).leader(User.builder().id(11).build()).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event).user(participant)
                .team(team).status(RegistrationStatus.PENDING).build();
        RegistrationField individual = field(55, event, true);
        RegistrationField teamField = field(56, event, false);
        when(eventRepository.existsById(3)).thenReturn(true);
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(registration));
        when(answerRepository.findByIdRegistrationId(19)).thenReturn(List.of(
                savedAnswer(registration, individual, "Participant value"),
                savedAnswer(registration, teamField, "Private team value")));

        ApiResponse<RegistrationResponse> response = service.getRegistration(3);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().getAnswers().size());
        assertEquals(55, response.getData().getAnswers().get(0).getFieldId());
        assertEquals("Participant value", response.getData().getAnswers().get(0).getAnswer());
        assertEquals("INDIVIDUAL", response.getData().getAnswers().get(0).getType());
    }

    @Test
    void assignedTeamLeaderCanRetrieveIndividualAndTeamAnswers() {
        User leader = User.builder().id(10).build();
        authenticate(leader);
        Event event = Event.builder().id(3).build();
        EventTeam team = EventTeam.builder().id(7).event(event).leader(leader).teamName("Team").build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event).user(leader)
                .team(team).status(RegistrationStatus.PENDING).build();
        RegistrationField individual = field(55, event, true);
        RegistrationField teamField = field(56, event, false);
        when(eventRepository.existsById(3)).thenReturn(true);
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(registration));
        when(answerRepository.findByIdRegistrationId(19)).thenReturn(List.of(
                savedAnswer(registration, individual, "Participant value"),
                savedAnswer(registration, teamField, "Team value")));

        ApiResponse<RegistrationResponse> response = service.getRegistration(3);

        assertTrue(response.isSuccess());
        assertEquals(2, response.getData().getAnswers().size());
        assertEquals(List.of("INDIVIDUAL", "TEAM"), response.getData().getAnswers().stream()
                .map(answer -> answer.getType()).toList());
    }

    @Test
    void emptyDraftReturnsEmptyAnswers() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event).user(participant)
                .status(RegistrationStatus.PENDING).build();
        when(eventRepository.existsById(3)).thenReturn(true);
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(registration));
        when(answerRepository.findByIdRegistrationId(19)).thenReturn(List.of());

        ApiResponse<RegistrationResponse> response = service.getRegistration(3);

        assertTrue(response.isSuccess());
        assertEquals(19, response.getData().getRegistrationId());
        assertNotNull(response.getData().getAnswers());
        assertTrue(response.getData().getAnswers().isEmpty());
    }

    @Test
    void participantWhoHasNotStartedRegistrationReceivesEmptyDraftState() {
        authenticate(User.builder().id(10).build());
        when(eventRepository.existsById(3)).thenReturn(true);
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.empty());

        ApiResponse<RegistrationResponse> response = service.getRegistration(3);

        assertTrue(response.isSuccess());
        assertEquals(3, response.getData().getEventId());
        assertEquals(false, response.getData().getCanResume());
        assertNotNull(response.getData().getAnswers());
        assertTrue(response.getData().getAnswers().isEmpty());
        verifyNoInteractions(answerRepository);
    }

    @Test
    void retrievalQueryIsScopedToLoggedInParticipant() {
        authenticate(User.builder().id(10).build());
        when(eventRepository.existsById(3)).thenReturn(true);
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.empty());

        ApiResponse<RegistrationResponse> response = service.getRegistration(3);

        assertTrue(response.isSuccess());
        verify(registrationRepository).findByEventIdAndUserId(3, 10);
        verifyNoInteractions(answerRepository);
    }

    @Test
    void participantCannotSaveAnswersForAnotherUsersRegistration() {
        authenticate(User.builder().id(10).build());
        Event event = Event.builder().id(3).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(User.builder().id(11).build()).status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));
        ApiResponse<RegistrationResponse> response = service.saveAnswers(19, request(AnswerType.INDIVIDUAL,
                List.of(answer(55, "answer"))));
        assertFalse(response.isSuccess());
        verifyNoInteractions(fieldRepository, answerRepository);
    }

    @Test
    void answerForQuestionOutsideRegistrationEventIsRejectedBeforeWrites() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(participant).status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(3)).thenReturn(List.of());
        ApiResponse<RegistrationResponse> response = service.saveAnswers(19, request(AnswerType.INDIVIDUAL,
                List.of(answer(55, "answer"))));
        assertFalse(response.isSuccess());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void teamAnswersRequireTheAssignedTeamLeader() {
        User caller = User.builder().id(10).build();
        authenticate(caller);
        Event event = Event.builder().id(3).build();
        EventTeam team = EventTeam.builder().id(7).event(event).leader(User.builder().id(11).build()).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(caller).team(team).status(RegistrationStatus.PENDING).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                request(AnswerType.TEAM, List.of(answer(56, "team answer"))));

        assertFalse(response.isSuccess());
        assertEquals("Only the assigned team's leader can save team answers.", response.getMessage());
        verifyNoInteractions(fieldRepository, answerRepository);
    }

    @Test
    void assignedTeamLeaderCanSaveTeamAnswers() {
        User leader = User.builder().id(10).build();
        authenticate(leader);
        Event event = Event.builder().id(3).build();
        EventTeam team = EventTeam.builder().id(7).event(event).leader(leader).teamName("Team").build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(User.builder().id(11).build()).team(team).status(RegistrationStatus.PENDING).build();
        EventRegistration leaderRegistration = EventRegistration.builder().id(21).event(event)
                .user(leader).team(team).status(RegistrationStatus.PENDING).build();
        RegistrationField field = RegistrationField.builder().id(56).event(event).individual(false)
                .fieldType("TEXT").required(false).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));
        when(registrationRepository.findByEventIdAndUserId(3, 10)).thenReturn(Optional.of(leaderRegistration));
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(3)).thenReturn(List.of(field));
        when(answerRepository.findById(any())).thenReturn(Optional.empty());

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                request(AnswerType.TEAM, List.of(answer(56, "team answer"))));

        assertTrue(response.isSuccess());
        verify(answerRepository).save(argThat(saved -> saved.getField().getId().equals(56)
                && saved.getRegistration().getId().equals(21)
                && saved.getAnswer().equals("team answer")));
    }

    @Test
    void requiredBlankAnswerIsRejectedBeforeWrites() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(participant).status(RegistrationStatus.PENDING).build();
        RegistrationField field = RegistrationField.builder().id(55).event(event).individual(true)
                .fieldLabel("Required field").fieldType("TEXT").required(true).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(3)).thenReturn(List.of(field));

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                request(AnswerType.INDIVIDUAL, List.of(answer(55, "  "))));

        assertFalse(response.isSuccess());
        assertEquals("Required field missing: Required field", response.getMessage());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void invalidNumberAnswerIsRejectedBeforeWrites() {
        User participant = User.builder().id(10).build();
        authenticate(participant);
        Event event = Event.builder().id(3).build();
        EventRegistration registration = EventRegistration.builder().id(19).event(event)
                .user(participant).status(RegistrationStatus.PENDING).build();
        RegistrationField field = RegistrationField.builder().id(55).event(event).individual(true)
                .fieldLabel("Age").fieldType("NUMBER").required(false).build();
        when(registrationRepository.findById(19)).thenReturn(Optional.of(registration));
        when(fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(3)).thenReturn(List.of(field));

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                request(AnswerType.INDIVIDUAL, List.of(answer(55, "not-a-number"))));

        assertFalse(response.isSuccess());
        assertEquals("Answer has an invalid format for Age", response.getMessage());
        verify(answerRepository, never()).save(any());
    }

    @Test
    void answerTypeIsRequired() {
        authenticate(User.builder().id(10).build());

        ApiResponse<RegistrationResponse> response = service.saveAnswers(19,
                SaveRegistrationAnswersRequest.builder().answers(List.of()).build());

        assertFalse(response.isSuccess());
        assertEquals("Answer type is required.", response.getMessage());
        verifyNoInteractions(registrationRepository, fieldRepository, answerRepository);
    }

    private SaveRegistrationAnswersRequest request(AnswerType type, List<SaveRegistrationAnswersRequest.Answer> answers) {
        return SaveRegistrationAnswersRequest.builder().type(type).answers(answers).build();
    }

    private SaveRegistrationAnswersRequest.Answer answer(Integer fieldId, String value) {
        return SaveRegistrationAnswersRequest.Answer.builder().fieldId(fieldId).answer(value).build();
    }

    private RegistrationField field(Integer id, Event event, boolean individual) {
        return RegistrationField.builder().id(id).event(event).individual(individual)
                .fieldLabel("Question " + id).fieldType("TEXT").required(false).build();
    }

    private RegistrationAnswer savedAnswer(EventRegistration registration, RegistrationField field, String value) {
        return RegistrationAnswer.builder().registration(registration).field(field).answer(value).build();
    }

    private void authenticate(User user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, "test"));
    }
}
