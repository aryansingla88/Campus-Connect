package com.campus.Campus_Connect.features.club;

import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ConflictException;
import com.campus.Campus_Connect.common.exception.ForbiddenException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.club.dto.request.ApproveClubRequest;
import com.campus.Campus_Connect.features.club.dto.request.AssignClubMemberRoleRequest;
import com.campus.Campus_Connect.features.club.entity.Club;
import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import com.campus.Campus_Connect.features.profile.entity.UserProfile;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubMembershipManagementServiceTest {

    private static final Integer CLUB_ID = 4;
    private static final Integer ACTOR_ID = 10;
    private static final Integer APPLICANT_ID = 20;

    @Mock private ClubRepository clubRepository;
    @Mock private ClubMemberRepository clubMemberRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ClubService clubService;

    private User actingUser;
    private Club club;

    @BeforeEach
    void authenticate() {
        actingUser = User.builder().id(ACTOR_ID).username("leader").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actingUser, null, List.of())
        );
        club = club(CLUB_ID, "Student Club");
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, ACTOR_ID))
                .thenReturn(Optional.of(membership(club, actingUser, ClubMemberStatus.APPROVED,
                        ClubMemberRole.SECRETARY, 10)));
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void pendingRequestsAreClubScopedAndIncludeOnlyMinimalApplicantProfile() {
        LocalDateTime requestedAt = LocalDateTime.of(2026, 1, 2, 3, 4);
        User applicant = applicant();
        when(clubMemberRepository.findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                CLUB_ID, ClubMemberStatus.PENDING
        )).thenReturn(List.of(membership(club, applicant, ClubMemberStatus.PENDING,
                ClubMemberRole.MEMBER, APPLICANT_ID, requestedAt)));

        var response = clubService.getPendingClubRequests(CLUB_ID);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        var item = response.getData().get(0);
        assertEquals(CLUB_ID, item.getClubId());
        assertEquals(APPLICANT_ID, item.getUserId());
        assertEquals("Alex Applicant", item.getFullName());
        assertEquals("applicant", item.getUsername());
        assertEquals("/uploads/applicant.jpg", item.getAvatarUrl());
        assertEquals(ClubMemberStatus.PENDING, item.getMemberStatus());
        assertEquals(requestedAt, item.getRequestedAt());
        verify(clubMemberRepository)
                .findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                        CLUB_ID, ClubMemberStatus.PENDING
                );
    }

    @Test
    void pendingRequestsCanBeEmpty() {
        when(clubMemberRepository.findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                CLUB_ID, ClubMemberStatus.PENDING
        )).thenReturn(List.of());

        var response = clubService.getPendingClubRequests(CLUB_ID);

        assertTrue(response.isSuccess());
        assertTrue(response.getData().isEmpty());
    }

    @Test
    void approvedClubSecretaryAndSocietyPresidentCanManageButOtherTypeRolePairsCannot() {
        List<LeaderCase> cases = List.of(
                new LeaderCase("CLUB", ClubMemberRole.SECRETARY, true),
                new LeaderCase("Student Club", ClubMemberRole.SECRETARY, true),
                new LeaderCase("Society", ClubMemberRole.PRESIDENT, true),
                new LeaderCase("Society", ClubMemberRole.SECRETARY, false),
                new LeaderCase("CLUB", ClubMemberRole.PRESIDENT, false),
                new LeaderCase("CLUB", ClubMemberRole.MEMBER, false)
        );

        int id = 30;
        for (LeaderCase leaderCase : cases) {
            int clubId = id;
            reset(clubRepository, clubMemberRepository);
            Club scopedClub = club(clubId, leaderCase.clubType());
            when(clubRepository.findById(clubId)).thenReturn(Optional.of(scopedClub));
            when(clubMemberRepository.findByIdClubIdAndIdUserId(clubId, ACTOR_ID))
                    .thenReturn(Optional.of(membership(scopedClub, actingUser,
                            ClubMemberStatus.APPROVED, leaderCase.role(), ACTOR_ID)));

            if (leaderCase.authorized()) {
                when(clubMemberRepository
                        .findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                                clubId, ClubMemberStatus.PENDING
                        )).thenReturn(List.of());
                assertTrue(clubService.getPendingClubRequests(clubId).isSuccess());
            } else {
                assertThrows(ForbiddenException.class,
                        () -> clubService.getPendingClubRequests(clubId));
                verify(clubMemberRepository, never())
                        .findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                                anyInt(), any()
                        );
            }
            id++;
        }
    }

    @Test
    void leaderOfAnotherClubCannotManageThisClubsRequests() {
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, ACTOR_ID))
                .thenReturn(Optional.empty());

        assertThrows(ForbiddenException.class, () -> clubService.getPendingClubRequests(CLUB_ID));
        verify(clubMemberRepository, never())
                .findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                        anyInt(), any()
                );
    }

    @Test
    void missingClubAndMissingRequestAreReported() {
        when(clubRepository.findById(404)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> clubService.getPendingClubRequests(404));

        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> clubService.approveClubRequest(CLUB_ID, APPLICANT_ID, null));
    }

    @Test
    void approvingPendingRequestDefaultsToMemberAndSetsJoinedAndUpdatedTimes() {
        ClubMember request = membership(club, applicant(), ClubMemberStatus.PENDING,
                ClubMemberRole.MEMBER, APPLICANT_ID, LocalDateTime.of(2026, 1, 1, 10, 0));
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(request));
        when(clubMemberRepository.save(request)).thenReturn(request);

        var response = clubService.approveClubRequest(CLUB_ID, APPLICANT_ID, null);

        assertTrue(response.isSuccess());
        assertEquals(ClubMemberStatus.APPROVED, request.getMemberStatus());
        assertEquals(ClubMemberRole.MEMBER, request.getRole());
        assertNotNull(request.getJoinedAt());
        assertNotNull(request.getUpdatedAt());
        assertEquals(ClubMemberStatus.APPROVED, response.getData().getMemberStatus());
        assertEquals(ClubMemberRole.MEMBER, response.getData().getRole());
        assertNotNull(response.getData().getRequestedAt());
    }

    @Test
    void approvingAllowsSupportedNonLeadershipRole() {
        ClubMember request = membership(club, applicant(), ClubMemberStatus.PENDING,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(request));
        when(clubMemberRepository.save(request)).thenReturn(request);

        var response = clubService.approveClubRequest(CLUB_ID, APPLICANT_ID,
                ApproveClubRequest.builder().role(ClubMemberRole.VICE_PRESIDENT).build());

        assertTrue(response.isSuccess());
        assertEquals(ClubMemberRole.VICE_PRESIDENT, response.getData().getRole());
    }

    @Test
    void approvingCannotGrantLeadershipRole() {
        ClubMember request = membership(club, applicant(), ClubMemberStatus.PENDING,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(request));

        assertThrows(ForbiddenException.class, () -> clubService.approveClubRequest(
                CLUB_ID, APPLICANT_ID, ApproveClubRequest.builder()
                        .role(ClubMemberRole.SECRETARY).build()));
        verify(clubMemberRepository, never()).save(any());
    }

    @Test
    void approvingAlreadyApprovedMembershipIsRejected() {
        ClubMember approved = membership(club, applicant(), ClubMemberStatus.APPROVED,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(approved));

        assertThrows(ConflictException.class,
                () -> clubService.approveClubRequest(CLUB_ID, APPLICANT_ID, null));
        verify(clubMemberRepository, never()).save(any());
    }

    @Test
    void rejectingPendingRequestMarksItRejectedAndRejectsAlreadyApprovedMembership() {
        ClubMember pending = membership(club, applicant(), ClubMemberStatus.PENDING,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(pending));
        when(clubMemberRepository.save(pending)).thenReturn(pending);

        var response = clubService.rejectClubRequest(CLUB_ID, APPLICANT_ID);

        assertTrue(response.isSuccess());
        assertEquals(ClubMemberStatus.REJECTED, pending.getMemberStatus());
        assertNotNull(pending.getUpdatedAt());
        assertEquals(ClubMemberStatus.REJECTED, response.getData().getMemberStatus());

        ClubMember approved = membership(club, applicant(), ClubMemberStatus.APPROVED,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(approved));
        assertThrows(ConflictException.class,
                () -> clubService.rejectClubRequest(CLUB_ID, APPLICANT_ID));
    }

    @Test
    void roleAssignmentRequiresApprovedMemberAndCannotGrantDesignatedLeadershipRole() {
        ClubMember approved = membership(club, applicant(), ClubMemberStatus.APPROVED,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(approved));
        when(clubMemberRepository.save(approved)).thenReturn(approved);

        var response = clubService.assignClubMemberRole(CLUB_ID, APPLICANT_ID,
                AssignClubMemberRoleRequest.builder().role(ClubMemberRole.COMMITTEE_HEAD).build());
        assertEquals(ClubMemberRole.COMMITTEE_HEAD, response.getData().getRole());
        assertNotNull(approved.getUpdatedAt());

        assertThrows(ForbiddenException.class, () -> clubService.assignClubMemberRole(
                CLUB_ID, APPLICANT_ID,
                AssignClubMemberRoleRequest.builder().role(ClubMemberRole.SECRETARY).build()));
        assertThrows(BadRequestException.class,
                () -> clubService.assignClubMemberRole(CLUB_ID, APPLICANT_ID, null));

        approved.setMemberStatus(ClubMemberStatus.PENDING);
        assertThrows(ConflictException.class, () -> clubService.assignClubMemberRole(
                CLUB_ID, APPLICANT_ID,
                AssignClubMemberRoleRequest.builder().role(ClubMemberRole.MEMBER).build()));
    }

    @Test
    void societyPresidentRoleIsProtectedFromAssignmentButSocietySecretaryIsAssignable() {
        Club society = club(CLUB_ID, "Society");
        when(clubRepository.findById(CLUB_ID)).thenReturn(Optional.of(society));
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, ACTOR_ID))
                .thenReturn(Optional.of(membership(society, actingUser,
                        ClubMemberStatus.APPROVED, ClubMemberRole.PRESIDENT, ACTOR_ID)));
        ClubMember approved = membership(society, applicant(), ClubMemberStatus.APPROVED,
                ClubMemberRole.MEMBER, APPLICANT_ID);
        when(clubMemberRepository.findByIdClubIdAndIdUserId(CLUB_ID, APPLICANT_ID))
                .thenReturn(Optional.of(approved));
        when(clubMemberRepository.save(approved)).thenReturn(approved);

        var response = clubService.assignClubMemberRole(CLUB_ID, APPLICANT_ID,
                AssignClubMemberRoleRequest.builder().role(ClubMemberRole.SECRETARY).build());
        assertEquals(ClubMemberRole.SECRETARY, response.getData().getRole());

        assertThrows(ForbiddenException.class, () -> clubService.assignClubMemberRole(
                CLUB_ID, APPLICANT_ID,
                AssignClubMemberRoleRequest.builder().role(ClubMemberRole.PRESIDENT).build()));
    }

    private User applicant() {
        User user = User.builder().id(APPLICANT_ID).username("applicant").build();
        user.setProfile(UserProfile.builder()
                .userId(APPLICANT_ID)
                .user(user)
                .fullName("Alex Applicant")
                .avatarUrl("/uploads/applicant.jpg")
                .build());
        return user;
    }

    private Club club(Integer id, String type) {
        return Club.builder().id(id).name("Test club").clubType(type).build();
    }

    private ClubMember membership(
            Club club,
            User user,
            ClubMemberStatus status,
            ClubMemberRole role,
            Integer userId
    ) {
        return membership(club, user, status, role, userId, LocalDateTime.now());
    }

    private ClubMember membership(
            Club club,
            User user,
            ClubMemberStatus status,
            ClubMemberRole role,
            Integer userId,
            LocalDateTime createdAt
    ) {
        return ClubMember.builder()
                .id(new ClubMemberId(club.getId(), userId))
                .club(club)
                .user(user)
                .memberStatus(status)
                .role(role)
                .createdAt(createdAt)
                .updatedAt(createdAt)
                .build();
    }

    private record LeaderCase(String clubType, ClubMemberRole role, boolean authorized) {
    }
}
