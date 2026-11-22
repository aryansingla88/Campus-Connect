package com.campus.Campus_Connect.features.club;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.club.dto.response.ClubDetailResponse;
import com.campus.Campus_Connect.features.club.dto.response.ClubMemberResponse;
import com.campus.Campus_Connect.features.club.entity.Club;
import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClubServiceTest {

    @Test
    void clubMemberRolesContainAllDesignationsWithoutAdminRole() {
        assertEquals(java.util.List.of(
                ClubMemberRole.PRESIDENT,
                ClubMemberRole.VICE_PRESIDENT,
                ClubMemberRole.SECRETARY,
                ClubMemberRole.JOINT_SECRETARY,
                ClubMemberRole.ADDITIONAL_SECRETARY,
                ClubMemberRole.COMMITTEE_HEAD,
                ClubMemberRole.MEMBER
        ), java.util.List.of(ClubMemberRole.values()));
    }
    @Mock private ClubRepository clubRepository;
    @Mock private ClubMemberRepository clubMemberRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ClubService clubService;

    private User currentUser;

    @BeforeEach
    void authenticate() {
        currentUser = User.builder().id(10).username("viewer").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(currentUser, null, List.of())
        );
    }

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void clubDetailsIncludeAuthenticatedUsersMembershipAndRole() {
        Club club = Club.builder().id(4).name("Robotics").description("Build things").build();
        ClubMember membership = ClubMember.builder()
                .role(ClubMemberRole.COMMITTEE_HEAD)
                .memberStatus(ClubMemberStatus.APPROVED)
                .build();
        when(clubRepository.findById(4)).thenReturn(Optional.of(club));
        when(clubMemberRepository.findByIdClubIdAndIdUserId(4, currentUser.getId()))
                .thenReturn(Optional.of(membership));
        when(clubMemberRepository.countByIdClubIdAndMemberStatus(4, ClubMemberStatus.APPROVED))
                .thenReturn(8L);

        ApiResponse<ClubDetailResponse> response = clubService.getClub(4);

        assertTrue(response.isSuccess());
        assertEquals(4, response.getData().getClubId());
        assertEquals("Robotics", response.getData().getName());
        assertEquals("Build things", response.getData().getDescription());
        assertEquals(8, response.getData().getMemberCount());
        assertEquals(ClubMemberStatus.APPROVED, response.getData().getMemberStatus());
        assertEquals(ClubMemberRole.COMMITTEE_HEAD, response.getData().getRole());
        verify(clubMemberRepository).findByIdClubIdAndIdUserId(4, currentUser.getId());
    }

    @Test
    void membersEndpointMapsOnlyApprovedMembersAndSafeProfileFields() {
        User memberUser = User.builder().id(21).username("member21").build();
        memberUser.setProfile(UserProfile.builder()
                .userId(21).user(memberUser).fullName("Alex Member").avatarUrl("/uploads/alex.jpg").build());
        LocalDateTime joinedAt = LocalDateTime.of(2025, 2, 3, 10, 0);
        ClubMember approvedMember = ClubMember.builder()
                .id(new ClubMemberId(4, 21))
                .user(memberUser)
                .role(ClubMemberRole.PRESIDENT)
                .memberStatus(ClubMemberStatus.APPROVED)
                .joinedAt(joinedAt)
                .build();
        when(clubRepository.existsById(4)).thenReturn(true);
        when(clubMemberRepository.findAllWithUserProfileByIdClubIdAndMemberStatus(4, ClubMemberStatus.APPROVED))
                .thenReturn(List.of(approvedMember));

        ApiResponse<List<ClubMemberResponse>> response = clubService.getClubMembers(4);

        assertTrue(response.isSuccess());
        assertEquals(1, response.getData().size());
        ClubMemberResponse item = response.getData().get(0);
        assertEquals(21, item.getUserId());
        assertEquals("Alex Member", item.getFullName());
        assertEquals("/uploads/alex.jpg", item.getAvatarUrl());
        assertEquals(ClubMemberRole.PRESIDENT, item.getRole());
        assertEquals(joinedAt, item.getJoinedAt());
        verify(clubMemberRepository).findAllWithUserProfileByIdClubIdAndMemberStatus(4, ClubMemberStatus.APPROVED);
    }

    @Test
    void membersEndpointReturnsFailureForUnknownClub() {
        when(clubRepository.existsById(404)).thenReturn(false);

        ApiResponse<List<ClubMemberResponse>> response = clubService.getClubMembers(404);

        assertFalse(response.isSuccess());
        assertEquals("Club not found.", response.getMessage());
        verifyNoInteractions(clubMemberRepository);
    }

    @Test
    void membersEndpointRequiresTheSameAuthenticatedContextAsClubDetails() {
        SecurityContextHolder.clearContext();

        assertThrows(IllegalStateException.class, () -> clubService.getClubMembers(4));
        assertThrows(IllegalStateException.class, () -> clubService.getClub(4));
    }
}
