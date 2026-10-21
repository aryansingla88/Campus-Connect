package com.campus.Campus_Connect.features.registration.service;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventTeam;
import com.campus.Campus_Connect.features.event.entity.enums.ApprovalStatus;
import com.campus.Campus_Connect.features.event.entity.enums.EventState;
import com.campus.Campus_Connect.features.event.entity.enums.RegistrationType;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.repository.EventTeamRepository;
import com.campus.Campus_Connect.features.registration.dto.request.RegistrationRequest;
import com.campus.Campus_Connect.features.registration.dto.request.AnswerType;
import com.campus.Campus_Connect.features.registration.dto.request.SaveRegistrationAnswersRequest;
import com.campus.Campus_Connect.features.registration.dto.request.CreateTeamJoinRequest;
import com.campus.Campus_Connect.features.registration.dto.request.CreateTeamInvitationRequest;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationFieldResponse;
import com.campus.Campus_Connect.features.registration.dto.response.AvailableTeamResponse;
import com.campus.Campus_Connect.features.registration.dto.response.RegistrationResponse;
import com.campus.Campus_Connect.features.registration.dto.response.SavedRegistrationAnswerResponse;
import com.campus.Campus_Connect.features.registration.dto.response.TeamRequestResponse;
import com.campus.Campus_Connect.features.registration.dto.response.TeamMemberResponse;
import com.campus.Campus_Connect.features.registration.dto.response.TeamInviteCandidateResponse;
import com.campus.Campus_Connect.features.registration.entity.*;
import com.campus.Campus_Connect.features.registration.entity.enums.RegistrationStatus;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestInitiator;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestStatus;
import com.campus.Campus_Connect.features.registration.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {
    private final EventRepository eventRepository;
    private final RegistrationConfigRepository configRepository;
    private final EventRegistrationRepository registrationRepository;
    private final RegistrationFieldRepository fieldRepository;
    private final RegistrationAnswerRepository answerRepository;
    private final RegistrationFieldOptionRepository optionRepository;
    private final EventTeamRepository teamRepository;
    private final TeamRequestRepository teamRequestRepository;

    /** Initializes only the participant's draft. Team choice and answers are separate resumable steps. */
    @Transactional
    public ApiResponse<RegistrationResponse> registerForEvent(Integer eventId, RegistrationRequest ignored) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (event.getApprovalStatus() != ApprovalStatus.VERIFIED
                || event.getEventState() == EventState.CANCELLED
                || event.getRegistrationType() != RegistrationType.THROUGH_APP)
            return ApiResponse.failure("This event is not accepting in-app registrations.");
        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        Instant now = Instant.now();
        if (config.getRegistrationCreatedAt() != null && now.isBefore(config.getRegistrationCreatedAt()))
            return ApiResponse.failure("Registration has not opened yet.");
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt()))
            return ApiResponse.failure("Registration has closed.");

        Optional<EventRegistration> existing = registrationRepository.findByEventIdAndUserId(eventId, user.getId());
        if (existing.isPresent()) {
            EventRegistration r = existing.get();
            if (r.getStatus() == RegistrationStatus.REJECTED)
                return ApiResponse.failure("This registration is no longer active.");
            if (r.getStatus() != RegistrationStatus.PENDING)
                return ApiResponse.failure("This registration has already been submitted and cannot be resumed as a draft.");
            return ApiResponse.success(toResponse(r), "Existing registration returned.");
        }
        EventRegistration registration = registrationRepository.save(EventRegistration.builder()
                .event(event).user(user).status(RegistrationStatus.PENDING).submittedAt(now).build());
        return ApiResponse.success(toResponse(registration), "Registration draft created.");
    }

    public ApiResponse<List<RegistrationFieldResponse>> getPublishedFields(Integer eventId, Boolean individual) {
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        if (event.getApprovalStatus() != ApprovalStatus.VERIFIED) return ApiResponse.failure("Event is not published.");
        List<RegistrationFieldResponse> fields = fieldRepository
                .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(eventId).stream()
                .filter(f -> individual == null || Boolean.valueOf(individual).equals(f.getIndividual()))
                .map(f -> RegistrationFieldResponse.builder().id(f.getId()).fieldLabel(f.getFieldLabel())
                        .fieldType(f.getFieldType()).required(f.getRequired()).placeholder(f.getPlaceholder())
                        .fieldOrder(f.getFieldOrder()).individual(f.getIndividual())
                        .options(optionRepository.findByFieldIdOrderByOptionOrderAsc(f.getId()).stream()
                                .map(o -> RegistrationFieldResponse.Option.builder().id(o.getId())
                                        .optionValue(o.getOptionValue()).optionOrder(o.getOptionOrder()).build()).toList())
                        .build()).toList();
        return ApiResponse.success(fields, "Registration fields fetched.");
    }

    public ApiResponse<List<AvailableTeamResponse>> getAvailableTeams(Integer eventId, String search) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        Event event = eventRepository.findById(eventId).orElse(null);
        if (event == null) return ApiResponse.failure("Event not found.");
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, user.getId()).orElse(null);
        if (registration == null) return ApiResponse.failure("Start registration before browsing teams.");
        String eligibilityError = validateRegistrationEligibility(registration);
        if (eligibilityError != null) return ApiResponse.failure(eligibilityError);
        if (registration.getTeam() != null) return ApiResponse.failure("You are already assigned to a team.");
        RegistrationConfig config = configRepository.findByEventId(eventId).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1) return ApiResponse.failure("Teams are not available for this event.");
        String query = search == null ? "" : search.trim().toLowerCase();
        List<AvailableTeamResponse> teams = teamRepository.findAllByEvent_Id(eventId).stream()
                .filter(team -> team.getTeamName().toLowerCase().contains(query))
                .map(team -> {
                    int count = (int) registrationRepository.countByEventIdAndTeamIdAndStatusIn(eventId, team.getId(),
                            List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
                    return AvailableTeamResponse.builder().teamId(team.getId()).teamName(team.getTeamName())
                            .memberCount(count).capacity(config.getMaxTeamMembers())
                            .leaderName(team.getLeader() == null ? null : team.getLeader().getUsername()).build();
                }).filter(team -> team.getMemberCount() < team.getCapacity()).toList();
        return ApiResponse.success(teams, "Available teams fetched.");
    }

    @Transactional
    public ApiResponse<Boolean> isTeamNameAvailable(Integer eventId, String teamName) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        if (teamName == null || teamName.trim().length() < 3 || teamName.trim().length() > 100)
            return ApiResponse.failure("Team name must be between 3 and 100 characters.");
        EventRegistration registration = registrationRepository.findByEventIdAndUserId(eventId, user.getId()).orElse(null);
        if (registration == null || registration.getStatus() != RegistrationStatus.PENDING || registration.getTeam() != null)
            return ApiResponse.failure("Start an unassigned registration draft before checking a team name.");
        return ApiResponse.success(!teamRepository.existsByEvent_IdAndTeamNameIgnoreCase(eventId, teamName.trim()), "Team name availability checked.");
    }

    @Transactional
    public ApiResponse<AvailableTeamResponse> getTeamSummary(Integer teamId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventTeam team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        boolean member = registrationRepository.findByEventIdAndUserId(team.getEvent().getId(), user.getId())
                .map(r -> r.getTeam() != null && r.getTeam().getId().equals(teamId)
                        && List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED).contains(r.getStatus()))
                .orElse(false);
        if (!member) return ApiResponse.failure("Only current team members can view this team.");
        RegistrationConfig config = configRepository.findByEventId(team.getEvent().getId()).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        int count = (int) registrationRepository.countByEventIdAndTeamIdAndStatusIn(team.getEvent().getId(), teamId,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
        return ApiResponse.success(AvailableTeamResponse.builder().teamId(teamId).teamName(team.getTeamName())
                .memberCount(count).capacity(config.getMaxTeamMembers())
                .leaderName(team.getLeader() == null ? null : team.getLeader().getUsername()).build(), "Team details fetched.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> saveAnswers(Integer registrationId, SaveRegistrationAnswersRequest request) {
        if (request == null || request.getType() == null)
            return ApiResponse.failure("Answer type is required.");

        User user = currentParticipant();
        if (user == null || registrationId == null)
            return ApiResponse.failure("Editable registration not found.");
        EventRegistration registration = registrationRepository.findById(registrationId).orElse(null);
        if (registration == null || registration.getStatus() != RegistrationStatus.PENDING)
            return ApiResponse.failure("Editable registration not found.");

        boolean individual = request.getType() == AnswerType.INDIVIDUAL;
        EventRegistration answerRegistration = registration;
        if (individual) {
            if (!registration.getUser().getId().equals(user.getId()))
                return ApiResponse.failure("Editable registration not found.");
        } else {
            EventTeam assignedTeam = registration.getTeam();
            if (assignedTeam == null) {
                RegistrationConfig config = configRepository.findByEventId(registration.getEvent().getId()).orElse(null);
                if (!registration.getUser().getId().equals(user.getId()) || config == null || config.getMinTeamMembers() > 1)
                    return ApiResponse.failure("Only an eligible solo participant can save shared answers without a team.");
            } else {
                if (assignedTeam.getLeader() == null || !assignedTeam.getLeader().getId().equals(user.getId())
                        || !assignedTeam.getEvent().getId().equals(registration.getEvent().getId()))
                    return ApiResponse.failure("Only the assigned team's leader can save team answers.");
                answerRegistration = registrationRepository.findByEventIdAndUserId(
                        registration.getEvent().getId(), user.getId()).orElse(null);
                if (answerRegistration == null || answerRegistration.getTeam() == null
                        || !answerRegistration.getTeam().getId().equals(assignedTeam.getId()))
                    return ApiResponse.failure("The team leader's registration is not assigned to this team.");
            }
        }
        final EventRegistration answerTarget = answerRegistration;

        List<SaveRegistrationAnswersRequest.Answer> submitted = request.getAnswers() == null
                ? List.of() : request.getAnswers();
        List<RegistrationField> fields = fieldRepository
                .findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(registration.getEvent().getId());
        // Validate the whole batch before writing anything so an invalid later field cannot
        // leave a partially applied answer update.
        for (SaveRegistrationAnswersRequest.Answer answer : submitted) {
            if (answer == null || answer.getFieldId() == null) return ApiResponse.failure("Every answer requires a fieldId.");
            RegistrationField field = fields.stream().filter(f -> f.getId().equals(answer.getFieldId())).findFirst().orElse(null);
            if (field == null || Boolean.TRUE.equals(field.getIndividual()) != individual)
                return ApiResponse.failure("Invalid registration field ID: " + answer.getFieldId());
            String value = answer.getAnswer();
            if (value == null || value.isBlank()) {
                if (Boolean.TRUE.equals(field.getRequired())) return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
                continue;
            }
            if (value.length() > 10000) return ApiResponse.failure("Answer is too long for " + field.getFieldLabel());
            String type = field.getFieldType() == null ? "" : field.getFieldType().toUpperCase();
            try {
                if (type.equals("NUMBER")) new java.math.BigDecimal(value);
                if (type.equals("DATE")) LocalDate.parse(value);
            } catch (NumberFormatException | DateTimeParseException invalid) {
                return ApiResponse.failure("Answer has an invalid format for " + field.getFieldLabel());
            }
            if (type.equals("SELECT") || type.equals("MULTISELECT") || type.contains("DROPDOWN") || type.contains("CHOICE")) {
                List<String> allowed = optionRepository.findByFieldIdOrderByOptionOrderAsc(field.getId()).stream()
                        .map(RegistrationFieldOption::getOptionValue).toList();
                List<String> chosen = type.equals("MULTISELECT") ? List.of(value.split(",")) : List.of(value);
                if (chosen.stream().map(String::trim).anyMatch(v -> !allowed.contains(v)))
                    return ApiResponse.failure("Answer is not an allowed option for " + field.getFieldLabel());
            }
        }
        for (SaveRegistrationAnswersRequest.Answer answer : submitted) {
            if (answer == null || answer.getFieldId() == null) return ApiResponse.failure("Every answer requires a fieldId.");
            RegistrationField field = fields.stream().filter(f -> f.getId().equals(answer.getFieldId())).findFirst().orElse(null);
            if (field == null || Boolean.TRUE.equals(field.getIndividual()) != individual)
                return ApiResponse.failure("Invalid registration field ID: " + answer.getFieldId());
            String value = answer.getAnswer();
            if (value == null || value.isBlank()) {
                if (Boolean.TRUE.equals(field.getRequired())) return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
                answerRepository.findById(new RegistrationAnswerId(answerTarget.getId(), field.getId()))
                        .ifPresent(answerRepository::delete);
                continue;
            }
            if (value.length() > 10000) return ApiResponse.failure("Answer is too long for " + field.getFieldLabel());
            RegistrationAnswerId answerId = new RegistrationAnswerId(answerTarget.getId(), field.getId());
            RegistrationAnswer persisted = answerRepository.findById(answerId).orElseGet(() -> RegistrationAnswer.builder()
                    .id(answerId).registration(answerTarget).field(field).build());
            persisted.setAnswer(value);
            answerRepository.save(persisted);
        }
        return ApiResponse.success(toResponse(answerTarget), "Answers saved.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> createTeam(Integer registrationId, String teamName) {
        EventRegistration r = ownedEditable(registrationId);
        if (r == null) return ApiResponse.failure("Editable registration not found.");
        RegistrationConfig config = configRepository.findByEventId(r.getEvent().getId()).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1) return ApiResponse.failure("Teams are not allowed for this event.");
        String eligibilityError = validateRegistrationEligibility(r);
        if (eligibilityError != null) return ApiResponse.failure(eligibilityError);
        if (r.getTeam() != null) return ApiResponse.failure("Registration is already assigned to a team.");
        if (teamName == null || teamName.trim().length() < 3 || teamName.trim().length() > 100) return ApiResponse.failure("Team name must be between 3 and 100 characters.");
        if (teamRepository.existsByEvent_IdAndTeamNameIgnoreCase(r.getEvent().getId(), teamName.trim()))
            return ApiResponse.failure("Team name is already taken for this event.");
        EventTeam team = teamRepository.save(EventTeam.builder().event(r.getEvent()).teamName(teamName.trim()).leader(r.getUser()).build());
        r.setTeam(team);
        return ApiResponse.success(toResponse(r), "Team created and leader membership established.");
    }

    @Transactional
    public ApiResponse<TeamRequestResponse> createTeamJoinRequest(Integer registrationId, CreateTeamJoinRequest request) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        if (request == null || request.getTeamId() == null) return ApiResponse.failure("teamId is required.");
        EventRegistration registration = registrationRepository.findByIdForUpdate(registrationId).orElse(null);
        if (registration == null || !registration.getUser().getId().equals(user.getId()))
            return ApiResponse.failure("Registration not found.");
        String eligibilityError = validateRegistrationEligibility(registration);
        if (eligibilityError != null) return ApiResponse.failure(eligibilityError);
        if (registration.getTeam() != null) return ApiResponse.failure("Registration is already assigned to a team.");

        EventTeam team = teamRepository.findById(request.getTeamId()).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        if (!team.getEvent().getId().equals(registration.getEvent().getId()))
            return ApiResponse.failure("Team and registration must belong to the same event.");
        if (team.getLeader().getId().equals(user.getId())) return ApiResponse.failure("You already lead this team.");
        RegistrationConfig config = configRepository.findByEventId(registration.getEvent().getId()).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1) return ApiResponse.failure("Teams are not available for this event.");

        TeamRequest pending = teamRequestRepository.findByRegistration_IdAndStatus(registrationId, TeamRequestStatus.PENDING).orElse(null);
        if (pending != null) {
            if (pending.getTeam().getId().equals(team.getId()) && pending.getInitiatedBy() == TeamRequestInitiator.PARTICIPANT)
                return ApiResponse.success(toTeamRequestResponse(pending), "Join request is already pending.");
            return ApiResponse.failure("A conflicting team request or invitation is already pending.");
        }
        long members = registrationRepository.countByEventIdAndTeamIdAndStatusIn(registration.getEvent().getId(), team.getId(),
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
        if (members >= config.getMaxTeamMembers()) return ApiResponse.failure("Team is full.");
        TeamRequest saved = teamRequestRepository.save(TeamRequest.builder().team(team).registration(registration)
                .initiatedBy(TeamRequestInitiator.PARTICIPANT).status(TeamRequestStatus.PENDING).build());
        return ApiResponse.success(toTeamRequestResponse(saved), "Team join request created.");
    }

    @Transactional
    public ApiResponse<TeamRequestResponse> createTeamInvitation(Integer teamId, CreateTeamInvitationRequest request) {
        User leader = currentParticipant();
        if (leader == null) return ApiResponse.failure("User is not authenticated.");
        if (request == null || request.getRegistrationId() == null) return ApiResponse.failure("registrationId is required.");
        EventTeam team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        if (team.getLeader() == null || !team.getLeader().getId().equals(leader.getId()))
            return ApiResponse.failure("Only the team leader can send invitations.");
        RegistrationConfig teamConfig = configRepository.findByEventId(team.getEvent().getId()).orElse(null);
        if (teamConfig == null) return ApiResponse.failure("Registration is not configured for this event.");
        long currentMembers = registrationRepository.countByEventIdAndTeamIdAndStatusIn(team.getEvent().getId(), teamId,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
        if (currentMembers >= teamConfig.getMaxTeamMembers()) return ApiResponse.failure("Team is full.");

        EventRegistration registration = registrationRepository.findByIdForUpdate(request.getRegistrationId()).orElse(null);
        if (registration == null) return ApiResponse.failure("Participant registration not found.");
        String eligibilityError = validateRegistrationEligibility(registration);
        if (eligibilityError != null) return ApiResponse.failure(eligibilityError);
        if (!team.getEvent().getId().equals(registration.getEvent().getId()))
            return ApiResponse.failure("Team and registration must belong to the same event.");
        if (registration.getUser().getId().equals(leader.getId())) return ApiResponse.failure("You cannot invite yourself.");
        if (registration.getTeam() != null) return ApiResponse.failure("Participant is already assigned to a team.");

        TeamRequest pending = teamRequestRepository.findByRegistration_IdAndStatus(registration.getId(), TeamRequestStatus.PENDING).orElse(null);
        if (pending != null) {
            if (pending.getTeam().getId().equals(team.getId()) && pending.getInitiatedBy() == TeamRequestInitiator.LEADER)
                return ApiResponse.success(toTeamRequestResponse(pending), "Invitation is already pending.");
            return ApiResponse.failure("A conflicting team request or invitation is already pending.");
        }
        TeamRequest saved = teamRequestRepository.save(TeamRequest.builder().team(team).registration(registration)
                .initiatedBy(TeamRequestInitiator.LEADER).status(TeamRequestStatus.PENDING).build());
        return ApiResponse.success(toTeamRequestResponse(saved), "Team invitation created.");
    }

    @Transactional
    public ApiResponse<List<TeamRequestResponse>> getMyTeamRequests() {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        return ApiResponse.success(teamRequestRepository.findByRegistration_User_IdOrderByCreatedAtDesc(user.getId())
                .stream().map(this::toTeamRequestResponse).toList(), "Team requests fetched.");
    }

    @Transactional
    public ApiResponse<List<TeamRequestResponse>> getTeamRequests(Integer teamId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventTeam team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        if (team.getLeader() == null || !team.getLeader().getId().equals(user.getId()))
            return ApiResponse.failure("Only the team leader can view team requests.");
        return ApiResponse.success(teamRequestRepository.findByTeam_IdOrderByCreatedAtDesc(teamId).stream()
                .filter(request -> request.getStatus() == TeamRequestStatus.PENDING)
                .map(this::toTeamRequestResponse).toList(), "Pending team requests fetched.");
    }

    @Transactional
    public ApiResponse<List<TeamMemberResponse>> getTeamMembers(Integer teamId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventTeam team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        boolean member = registrationRepository.findByEventIdAndUserId(team.getEvent().getId(), user.getId())
                .map(r -> r.getTeam() != null && r.getTeam().getId().equals(teamId)
                        && List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED).contains(r.getStatus()))
                .orElse(false);
        if (!member) return ApiResponse.failure("Only current team members can view team membership.");
        List<TeamMemberResponse> members = registrationRepository.findByEventIdAndTeamId(team.getEvent().getId(), teamId).stream()
                .filter(r -> List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED).contains(r.getStatus()))
                .map(r -> TeamMemberResponse.builder().registrationId(r.getId()).userId(r.getUser().getId())
                        .name(r.getUser().getUsername()).email(r.getUser().getEmail())
                        .role(r.getUser().getId().equals(team.getLeader().getId()) ? "LEADER" : "MEMBER").build())
                .toList();
        return ApiResponse.success(members, "Team members fetched.");
    }

    @Transactional
    public ApiResponse<List<TeamInviteCandidateResponse>> getTeamInviteCandidates(Integer teamId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventTeam team = teamRepository.findById(teamId).orElse(null);
        if (team == null) return ApiResponse.failure("Team not found.");
        if (team.getLeader() == null || !team.getLeader().getId().equals(user.getId()))
            return ApiResponse.failure("Only the team leader can view invitation candidates.");
        RegistrationConfig config = configRepository.findByEventId(team.getEvent().getId()).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        long members = registrationRepository.countByEventIdAndTeamIdAndStatusIn(team.getEvent().getId(), teamId,
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
        if (members >= config.getMaxTeamMembers()) return ApiResponse.success(List.of(), "Team is full.");
        List<TeamInviteCandidateResponse> candidates = registrationRepository.findByEventId(team.getEvent().getId()).stream()
                .filter(r -> !r.getUser().getId().equals(user.getId()))
                .filter(r -> r.getStatus() == RegistrationStatus.PENDING && r.getTeam() == null)
                .filter(r -> validateRegistrationEligibility(r) == null)
                .filter(r -> teamRequestRepository.findByRegistration_IdAndStatus(r.getId(), TeamRequestStatus.PENDING).isEmpty())
                .map(r -> TeamInviteCandidateResponse.builder().registrationId(r.getId()).participantId(r.getUser().getId())
                        .name(r.getUser().getUsername()).email(r.getUser().getEmail()).build())
                .toList();
        return ApiResponse.success(candidates, "Eligible invitation candidates fetched.");
    }

    @Transactional
    public ApiResponse<TeamRequestResponse> acceptTeamRequest(Integer requestId) {
        User responder = currentParticipant();
        if (responder == null) return ApiResponse.failure("User is not authenticated.");
        // Read only to discover the team ID. Lock the team before the request row so
        // team deletion's ON DELETE CASCADE cannot deadlock with acceptance.
        TeamRequest observedRequest = teamRequestRepository.findById(requestId).orElse(null);
        if (observedRequest == null) return ApiResponse.failure("Team request not found.");
        // Team is the shared serialization point with leader cancellation. Always lock it
        // before request or registration rows in both operations.
        EventTeam team = teamRepository.findByIdForUpdate(observedRequest.getTeam().getId()).orElse(null);
        if (team == null) return ApiResponse.failure("Team request is no longer valid.");
        TeamRequest request = teamRequestRepository.findByIdForUpdate(requestId).orElse(null);
        if (request == null || !request.getTeam().getId().equals(team.getId()))
            return ApiResponse.failure("Team request is no longer valid.");
        if (!isRequestRecipient(request, responder)) return ApiResponse.failure("You are not allowed to respond to this request.");
        EventRegistration registration = registrationRepository.findByIdForUpdate(request.getRegistration().getId()).orElse(null);
        if (registration == null) return ApiResponse.failure("Team request is no longer valid.");
        if (request.getStatus() == TeamRequestStatus.ACCEPTED
                && registration.getTeam() != null && registration.getTeam().getId().equals(team.getId()))
            return ApiResponse.success(toTeamRequestResponse(request), "Team request was already accepted.");
        if (request.getStatus() != TeamRequestStatus.PENDING) return ApiResponse.failure("Team request is no longer pending.");
        String eligibilityError = validateRegistrationEligibility(registration);
        if (eligibilityError != null) return cancelStaleTeamRequest(request, eligibilityError);
        if (!team.getEvent().getId().equals(registration.getEvent().getId()))
            return cancelStaleTeamRequest(request, "Team and registration no longer belong to the same event.");
        EventRegistration leaderRegistration = registrationRepository.findByEventIdAndUserId(
                team.getEvent().getId(), team.getLeader().getId()).orElse(null);
        if (leaderRegistration == null || leaderRegistration.getTeam() == null
                || !leaderRegistration.getTeam().getId().equals(team.getId())
                || leaderRegistration.getStatus() == RegistrationStatus.REJECTED)
            return cancelStaleTeamRequest(request, "Team leader registration is no longer active.");
        if (registration.getTeam() != null) {
            if (registration.getTeam().getId().equals(team.getId())) {
                request.setStatus(TeamRequestStatus.ACCEPTED);
                return ApiResponse.success(toTeamRequestResponse(teamRequestRepository.save(request)), "Participant is already assigned to this team.");
            }
            return cancelStaleTeamRequest(request, "Participant is already assigned to another team.");
        }
        RegistrationConfig config = configRepository.findByEventId(registration.getEvent().getId()).orElse(null);
        if (config == null || config.getMaxTeamMembers() <= 1)
            return cancelStaleTeamRequest(request, "Teams are not available for this event.");
        long members = registrationRepository.countByEventIdAndTeamIdAndStatusIn(registration.getEvent().getId(), team.getId(),
                List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
        if (members >= config.getMaxTeamMembers()) return cancelStaleTeamRequest(request, "Team is full.");

        registration.setTeam(team);
        registrationRepository.save(registration);
        request.setStatus(TeamRequestStatus.ACCEPTED);
        return ApiResponse.success(toTeamRequestResponse(teamRequestRepository.save(request)), "Participant added to team.");
    }

    @Transactional
    public ApiResponse<TeamRequestResponse> rejectTeamRequest(Integer requestId) {
        User responder = currentParticipant();
        if (responder == null) return ApiResponse.failure("User is not authenticated.");
        TeamRequest request = teamRequestRepository.findByIdForUpdate(requestId).orElse(null);
        if (request == null) return ApiResponse.failure("Team request not found.");
        if (!isRequestRecipient(request, responder)) return ApiResponse.failure("You are not allowed to respond to this request.");
        if (request.getStatus() == TeamRequestStatus.REJECTED)
            return ApiResponse.success(toTeamRequestResponse(request), "Team request was already rejected.");
        if (request.getStatus() != TeamRequestStatus.PENDING) return ApiResponse.failure("Team request is no longer pending.");
        request.setStatus(TeamRequestStatus.REJECTED);
        return ApiResponse.success(toTeamRequestResponse(teamRequestRepository.save(request)), "Team request rejected.");
    }

    @Transactional
    public ApiResponse<TeamRequestResponse> cancelTeamRequest(Integer requestId) {
        User initiator = currentParticipant();
        if (initiator == null) return ApiResponse.failure("User is not authenticated.");
        TeamRequest request = teamRequestRepository.findByIdForUpdate(requestId).orElse(null);
        if (request == null) return ApiResponse.failure("Team request not found.");
        boolean isInitiator = request.getInitiatedBy() == TeamRequestInitiator.PARTICIPANT
                ? request.getRegistration().getUser().getId().equals(initiator.getId())
                : request.getTeam().getLeader() != null && request.getTeam().getLeader().getId().equals(initiator.getId());
        if (!isInitiator) return ApiResponse.failure("Only the request initiator can cancel it.");
        if (request.getStatus() == TeamRequestStatus.CANCELLED)
            return ApiResponse.success(toTeamRequestResponse(request), "Team request was already cancelled.");
        if (request.getStatus() != TeamRequestStatus.PENDING) return ApiResponse.failure("Only pending requests can be cancelled.");
        request.setStatus(TeamRequestStatus.CANCELLED);
        return ApiResponse.success(toTeamRequestResponse(teamRequestRepository.save(request)), "Team request cancelled.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> finalizeRegistration(Integer registrationId) {
        User user = currentParticipant();
        if (user == null || registrationId == null) return ApiResponse.failure("Editable registration not found.");
        EventRegistration r = registrationRepository.findByIdForUpdate(registrationId).orElse(null);
        if (r == null || !r.getUser().getId().equals(user.getId()))
            return ApiResponse.failure("Editable registration not found.");
        if (r.getStatus() == RegistrationStatus.CONFIRMED)
            return ApiResponse.success(toResponse(r), "Registration is already confirmed.");
        if (r.getStatus() != RegistrationStatus.PENDING)
            return ApiResponse.failure("Editable registration not found.");
        String eligibilityError = validateRegistrationEligibility(r);
        if (eligibilityError != null) return ApiResponse.failure(eligibilityError);
        RegistrationConfig config = configRepository.findByEventId(r.getEvent().getId()).orElse(null);
        if (config == null) return ApiResponse.failure("Registration is not configured for this event.");
        Instant now = Instant.now();
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt())) return ApiResponse.failure("Registration has closed.");
        boolean solo = r.getTeam() == null;
        if (solo && config.getMinTeamMembers() > 1) return ApiResponse.failure("Solo registrations are not allowed for this event.");
        if (!solo) {
            long size = registrationRepository.countByEventIdAndTeamIdAndStatusIn(
                    r.getEvent().getId(), r.getTeam().getId(),
                    List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
            if (size < config.getMinTeamMembers() || size > config.getMaxTeamMembers()) return ApiResponse.failure("Team size does not meet event requirements.");
        }
        if (r.getTeam() != null && !r.getTeam().getEvent().getId().equals(r.getEvent().getId()))
            return ApiResponse.failure("Assigned team does not belong to this event.");
        List<RegistrationAnswer> individualAnswers = answerRepository.findByIdRegistrationId(registrationId);
        List<RegistrationAnswer> teamAnswers = List.of();
        if (r.getTeam() != null) {
            EventRegistration leaderRegistration = registrationRepository.findByEventIdAndUserId(
                    r.getEvent().getId(), r.getTeam().getLeader().getId()).orElse(null);
            if (leaderRegistration == null || leaderRegistration.getTeam() == null
                    || !leaderRegistration.getTeam().getId().equals(r.getTeam().getId()))
                return ApiResponse.failure("The team leader's registration is not assigned to this team.");
            teamAnswers = answerRepository.findByIdRegistrationId(leaderRegistration.getId());
        }
        if (solo) teamAnswers = individualAnswers;
        for (RegistrationField field : fieldRepository.findByEventIdAndDeletedAtIsNullOrderByFieldOrderAsc(r.getEvent().getId())) {
            if (!Boolean.TRUE.equals(field.getRequired())) continue;
            boolean applies = true;
            List<RegistrationAnswer> source = Boolean.TRUE.equals(field.getIndividual()) ? individualAnswers : teamAnswers;
            if (applies && source.stream().noneMatch(a -> a.getField().getId().equals(field.getId())
                    && a.getAnswer() != null && !a.getAnswer().isBlank()))
                return ApiResponse.failure("Required field missing: " + field.getFieldLabel());
        }
        boolean autoConfirm = Boolean.TRUE.equals(config.getAutoConfirm());
        r.setStatus(autoConfirm ? RegistrationStatus.CONFIRMED : RegistrationStatus.WAITLISTED);
        return ApiResponse.success(toResponse(r), autoConfirm
                ? "Registration automatically confirmed."
                : "Registration submitted for host approval.");
    }

    @Transactional
    public ApiResponse<RegistrationResponse> getRegistration(Integer eventId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        if (!eventRepository.existsById(eventId)) return ApiResponse.failure("Event not found.");
        return registrationRepository.findByEventIdAndUserId(eventId, user.getId())
                .map(r -> ApiResponse.success(toParticipantResponse(r, user), "Registration found."))
                .orElseGet(() -> ApiResponse.success(RegistrationResponse.builder().eventId(eventId)
                        .canResume(false).answers(List.of()).build(), "No registration found."));
    }

    public ApiResponse<List<RegistrationResponse>> getMyRegistrations() {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        return ApiResponse.success(registrationRepository.findByUserId(user.getId()).stream().map(this::toResponse).toList(), "Registrations fetched.");
    }

    @Transactional
    public ApiResponse<Void> cancelRegistration(Integer eventId) {
        User user = currentParticipant();
        if (user == null) return ApiResponse.failure("User is not authenticated.");
        EventRegistration observed = registrationRepository.findByEventIdAndUserId(eventId, user.getId()).orElse(null);
        if (observed == null) return ApiResponse.failure("You are not registered for this event.");
        EventTeam team = observed.getTeam();
        boolean leader = team != null && team.getLeader() != null && team.getLeader().getId().equals(user.getId());
        // Match acceptance lock order: team first, then the participant's registration.
        if (leader) team = teamRepository.findByIdForUpdate(team.getId()).orElse(null);
        EventRegistration r = registrationRepository.findByIdForUpdate(observed.getId()).orElse(null);
        if (r == null || !r.getUser().getId().equals(user.getId()))
            return ApiResponse.failure("You are not registered for this event.");
        if (r.getStatus() != RegistrationStatus.PENDING)
            return ApiResponse.failure("Only pending registrations can be cancelled.");
        if (leader) {
            if (team == null || r.getTeam() == null || !r.getTeam().getId().equals(team.getId()))
                return ApiResponse.failure("Team membership changed; retry cancellation.");
            long memberCount = registrationRepository.countByEventIdAndTeamIdAndStatusIn(
                    eventId, team.getId(), List.of(RegistrationStatus.PENDING, RegistrationStatus.WAITLISTED, RegistrationStatus.CONFIRMED));
            if (memberCount > 1)
                return ApiResponse.failure("Team leader cannot cancel while other participants remain assigned.");
            teamRepository.delete(team);
        }
        registrationRepository.delete(r);
        return ApiResponse.success(null, "Registration cancelled.");
    }

    private EventRegistration ownedEditable(Integer id) {
        User user = currentParticipant();
        if (user == null || id == null) return null;
        EventRegistration r = registrationRepository.findByIdForUpdate(id).orElse(null);
        return r != null && r.getUser().getId().equals(user.getId())
                && r.getStatus() == RegistrationStatus.PENDING ? r : null;
    }

    private User currentParticipant() {
        try {
            return SecurityUtils.getCurrentUser();
        } catch (IllegalStateException unauthenticated) {
            return null;
        }
    }

    private RegistrationResponse toResponse(EventRegistration r) {
        return RegistrationResponse.builder().registrationId(r.getId()).eventId(r.getEvent().getId())
                .status(r.getStatus().name()).teamId(r.getTeam() == null ? null : r.getTeam().getId())
                .teamName(r.getTeam() == null ? null : r.getTeam().getTeamName())
                .teamLeader(r.getTeam() != null && r.getTeam().getLeader() != null
                        && r.getTeam().getLeader().getId().equals(r.getUser().getId()))
                .canResume(r.getStatus() == RegistrationStatus.PENDING).build();
    }

    private RegistrationResponse toParticipantResponse(EventRegistration registration, User participant) {
        List<SavedRegistrationAnswerResponse> answers = answerRepository
                .findByIdRegistrationId(registration.getId()).stream()
                .filter(saved -> Boolean.TRUE.equals(saved.getField().getIndividual())
                        || registration.getTeam() == null || isAssignedTeamLeader(registration, participant))
                .map(saved -> SavedRegistrationAnswerResponse.builder()
                        .fieldId(saved.getField().getId())
                        .answer(saved.getAnswer())
                        .type(Boolean.TRUE.equals(saved.getField().getIndividual()) ? "INDIVIDUAL" : "TEAM")
                        .build())
                .toList();
        return RegistrationResponse.builder().registrationId(registration.getId())
                .eventId(registration.getEvent().getId()).status(registration.getStatus().name())
                .teamId(registration.getTeam() == null ? null : registration.getTeam().getId())
                .teamName(registration.getTeam() == null ? null : registration.getTeam().getTeamName())
                .teamLeader(isAssignedTeamLeader(registration, participant))
                .canResume(registration.getStatus() == RegistrationStatus.PENDING)
                .answers(answers).build();
    }

    private boolean isAssignedTeamLeader(EventRegistration registration, User participant) {
        EventTeam team = registration.getTeam();
        return team != null && team.getLeader() != null
                && team.getLeader().getId().equals(participant.getId());
    }

    private String validateRegistrationEligibility(EventRegistration registration) {
        if (registration.getStatus() != RegistrationStatus.PENDING)
            return "Registration is not pending.";
        Event event = registration.getEvent();
        if (event.getApprovalStatus() != ApprovalStatus.VERIFIED
                || event.getEventState() == EventState.CANCELLED
                || event.getRegistrationType() != RegistrationType.THROUGH_APP)
            return "This event is not accepting in-app registrations.";
        RegistrationConfig config = configRepository.findByEventId(event.getId()).orElse(null);
        if (config == null) return "Registration is not configured for this event.";
        Instant now = Instant.now();
        if (config.getRegistrationCreatedAt() != null && now.isBefore(config.getRegistrationCreatedAt()))
            return "Registration has not opened yet.";
        if (config.getRegistrationEndAt() != null && now.isAfter(config.getRegistrationEndAt()))
            return "Registration has closed.";
        return null;
    }

    private boolean isRequestRecipient(TeamRequest request, User user) {
        if (request.getInitiatedBy() == TeamRequestInitiator.PARTICIPANT)
            return request.getTeam().getLeader() != null
                    && request.getTeam().getLeader().getId().equals(user.getId());
        return request.getRegistration().getUser().getId().equals(user.getId());
    }

    private ApiResponse<TeamRequestResponse> cancelStaleTeamRequest(TeamRequest request, String reason) {
        request.setStatus(TeamRequestStatus.CANCELLED);
        teamRequestRepository.save(request);
        return ApiResponse.failure(reason);
    }

    private TeamRequestResponse toTeamRequestResponse(TeamRequest request) {
        User participant = request.getRegistration().getUser();
        String participantName = participant == null ? null : participant.getUsername();
        return TeamRequestResponse.builder().requestId(request.getId())
                .teamId(request.getTeam().getId()).teamName(request.getTeam().getTeamName())
                .registrationId(request.getRegistration().getId())
                .participantId(participant == null ? null : participant.getId())
                .participantName(participantName)
                .initiatedBy(request.getInitiatedBy().name()).status(request.getStatus().name())
                .createdAt(request.getCreatedAt()).build();
    }
}
