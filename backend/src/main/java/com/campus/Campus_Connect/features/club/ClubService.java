package com.campus.Campus_Connect.features.club;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.common.exception.BadRequestException;
import com.campus.Campus_Connect.common.exception.ConflictException;
import com.campus.Campus_Connect.common.exception.ForbiddenException;
import com.campus.Campus_Connect.common.exception.ResourceNotFoundException;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.club.dto.response.*;
import com.campus.Campus_Connect.features.club.dto.request.ApproveClubRequest;
import com.campus.Campus_Connect.features.club.dto.request.AssignClubMemberRoleRequest;
import com.campus.Campus_Connect.features.club.entity.Club;
import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.dto.response.ClubResponseMemberStatus;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubRepository clubRepository;
    private final ClubMemberRepository clubMemberRepository;
    private final UserRepository userRepository;

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubResponse>> getClubs() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<Club> clubs = clubRepository.findAll();

        List<ClubResponse> response = new ArrayList<>();

        for (Club club : clubs) {
            response.add(
                    buildClubResponse(
                            club,
                            currentUser.getId()
                    )
            );
        }

        return ApiResponse.success(
                response,
                "Clubs fetched successfully."
        );
    }

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<ClubDetailResponse> getClub(Integer clubId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Club club = clubRepository.findById(clubId).orElse(null);

        if (club == null) {
            return ApiResponse.failure("Club not found.");
        }

        ClubDetailResponse response = buildClubDetailResponse(
                club,
                currentUser.getId()
        );

        return ApiResponse.success(
                response,
                "Club fetched successfully."
        );
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMemberResponse>> getClubMembers(Integer clubId) {
        // Match GET /clubs/{clubId}: member listings require an authenticated context.
        SecurityUtils.getCurrentUser();
        if (!clubRepository.existsById(clubId)) {
            return ApiResponse.failure("Club not found.");
        }

        List<ClubMemberResponse> members = clubMemberRepository
                .findAllWithUserProfileByIdClubIdAndMemberStatus(
                        clubId,
                        ClubMemberStatus.APPROVED
                )
                .stream()
                .map(member -> {
                    User user = member.getUser();
                    String fullName = user.getProfile() == null
                            ? null
                            : user.getProfile().getFullName();
                    String avatarUrl = user.getProfile() == null
                            ? null
                            : user.getProfile().getAvatarUrl();
                    if (fullName == null || fullName.isBlank()) {
                        fullName = user.getUsername();
                    }
                    return ClubMemberResponse.builder()
                            .userId(user.getId())
                            .fullName(fullName)
                            .avatarUrl(avatarUrl)
                            .role(member.getRole())
                            .joinedAt(member.getJoinedAt())
                            .build();
                })
                .toList();

        return ApiResponse.success(members, "Club members fetched successfully.");
    }

    @Transactional(readOnly = true)
    public ApiResponse<List<ClubMemberRequestResponse>> getPendingClubRequests(Integer clubId) {
        User actingUser = SecurityUtils.getCurrentUser();
        Club club = getClubOrThrow(clubId);
        requireClubLeader(club, actingUser.getId());

        List<ClubMemberRequestResponse> requests = clubMemberRepository
                .findAllWithUserProfileByIdClubIdAndMemberStatusOrderByCreatedAtDesc(
                        clubId,
                        ClubMemberStatus.PENDING
                )
                .stream()
                .map(this::toClubMemberRequestResponse)
                .toList();

        return ApiResponse.success(requests, "Pending club requests fetched successfully.");
    }

    @Transactional
    public ApiResponse<ClubMemberRequestResponse> approveClubRequest(
            Integer clubId,
            Integer userId,
            ApproveClubRequest request
    ) {
        User actingUser = SecurityUtils.getCurrentUser();
        Club club = getClubOrThrow(clubId);
        requireClubLeader(club, actingUser.getId());

        ClubMember membership = getMembershipOrThrow(clubId, userId);
        requirePendingRequest(membership);

        ClubMemberRole assignedRole = request == null || request.getRole() == null
                ? ClubMemberRole.MEMBER
                : request.getRole();
        validateAssignableRole(club, assignedRole);

        LocalDateTime now = LocalDateTime.now();
        membership.setRole(assignedRole);
        membership.setMemberStatus(ClubMemberStatus.APPROVED);
        membership.setJoinedAt(now);
        membership.setUpdatedAt(now);

        return ApiResponse.success(
                toClubMemberRequestResponse(clubMemberRepository.save(membership)),
                "Club membership request approved successfully."
        );
    }

    @Transactional
    public ApiResponse<ClubMemberRequestResponse> rejectClubRequest(Integer clubId, Integer userId) {
        User actingUser = SecurityUtils.getCurrentUser();
        Club club = getClubOrThrow(clubId);
        requireClubLeader(club, actingUser.getId());

        ClubMember membership = getMembershipOrThrow(clubId, userId);
        requirePendingRequest(membership);

        membership.setMemberStatus(ClubMemberStatus.REJECTED);
        membership.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.success(
                toClubMemberRequestResponse(clubMemberRepository.save(membership)),
                "Club membership request rejected successfully."
        );
    }

    @Transactional
    public ApiResponse<ClubMemberRequestResponse> assignClubMemberRole(
            Integer clubId,
            Integer userId,
            AssignClubMemberRoleRequest request
    ) {
        User actingUser = SecurityUtils.getCurrentUser();
        Club club = getClubOrThrow(clubId);
        requireClubLeader(club, actingUser.getId());

        if (request == null || request.getRole() == null) {
            throw new BadRequestException("A membership role is required.");
        }

        ClubMember membership = getMembershipOrThrow(clubId, userId);
        if (membership.getMemberStatus() != ClubMemberStatus.APPROVED) {
            throw new ConflictException("Only approved club members can be assigned a role.");
        }

        validateAssignableRole(club, request.getRole());
        membership.setRole(request.getRole());
        membership.setUpdatedAt(LocalDateTime.now());

        return ApiResponse.success(
                toClubMemberRequestResponse(clubMemberRepository.save(membership)),
                "Club member role updated successfully."
        );
    }

    //------------------------------------------------------
    @Transactional
    public ApiResponse<ClubMembershipResponse> joinClub(Integer clubId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Club club = clubRepository.findById(clubId).orElse(null);

        if (club == null) {
            return ApiResponse.failure("Club not found.");
        }

        if (clubMemberRepository.existsByIdClubIdAndIdUserId(
                clubId,
                currentUser.getId()
        )) {
            return ApiResponse.failure(
                    "You have already joined or requested to join this club."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        ClubMember member = ClubMember.builder()
                .id(new ClubMemberId(clubId, currentUser.getId()))
                .club(club)
                .user(currentUser)
                .memberStatus(ClubMemberStatus.PENDING)
                .role(ClubMemberRole.MEMBER)
                .createdAt(now)
                .updatedAt(now)
                .build();

        clubMemberRepository.save(member);

        ClubMembershipResponse response = ClubMembershipResponse.builder()
                .clubId(clubId)
                .memberStatus(ClubMemberStatus.PENDING)
                .build();

        return ApiResponse.success(
                response,
                "Club join request submitted successfully."
        );
    }

    //------------------------------------------------------
    @Transactional
    public ApiResponse<ClubMembershipResponse> leaveClub(Integer clubId) {

        User currentUser = SecurityUtils.getCurrentUser();

        Club club = clubRepository.findById(clubId).orElse(null);

        if (club == null) {
            return ApiResponse.failure("Club not found.");
        }

        ClubMember member = getMembership(
                clubId,
                currentUser.getId()
        );

        if (member == null) {
            return ApiResponse.failure("You are not associated with this club.");
        }

        clubMemberRepository.delete(member);

        ClubMembershipResponse response = ClubMembershipResponse.builder()
                .clubId(clubId)
                .memberStatus(null)
                .build();

        return ApiResponse.success(
                response,
                "Left the club successfully."
        );
    }

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubResponse>> getMyClubs() {

        User currentUser = SecurityUtils.getCurrentUser();

        List<ClubMember> memberships = clubMemberRepository
                .findAllByIdUserIdAndMemberStatus(
                        currentUser.getId(),
                        ClubMemberStatus.APPROVED
                );

        List<ClubResponse> response = new ArrayList<>();

        for (ClubMember membership : memberships) {
            response.add(
                    buildClubResponse(
                            membership.getClub(),
                            membership
                    )
            );
        }

        return ApiResponse.success(
                response,
                "User clubs fetched successfully."
        );
    }
    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubResponse>> getUserClubs(Integer userId) {

        if (!userRepository.existsById(userId)) {
            return ApiResponse.failure("User not found.");
        }

        SecurityUtils.getCurrentUser();

        List<ClubMember> memberships = clubMemberRepository
                .findAllByIdUserIdAndMemberStatus(
                        userId,
                        ClubMemberStatus.APPROVED
                );

        List<ClubResponse> response = new ArrayList<>();

        for (ClubMember membership : memberships) {
            response.add(
                    buildClubResponse(
                            membership.getClub(),
                            membership
                    )
            );
        }

        return ApiResponse.success(
                response,
                "User's clubs fetched successfully."
        );
    }


    //------------------------------------------------------
    private ClubMember getMembership(                          //getClubs(),getClub(),leaveClub(),getUserClubs()
            Integer clubId,
            Integer userId
    ) {
        return clubMemberRepository
                .findByIdClubIdAndIdUserId(
                        clubId,
                        userId
                )
                .orElse(null);
    }

    private Club getClubOrThrow(Integer clubId) {
        return clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found."));
    }

    private ClubMember getMembershipOrThrow(Integer clubId, Integer userId) {
        return clubMemberRepository.findByIdClubIdAndIdUserId(clubId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Club membership request not found."));
    }

    private void requireClubLeader(Club club, Integer actingUserId) {
        ClubMember actingMembership = getMembership(club.getId(), actingUserId);
        if (actingMembership == null
                || actingMembership.getMemberStatus() != ClubMemberStatus.APPROVED
                || !hasLeadershipRole(club, actingMembership.getRole())) {
            throw new ForbiddenException("Only an approved leader of this club may manage membership.");
        }
    }

    private void requirePendingRequest(ClubMember membership) {
        if (membership.getMemberStatus() != ClubMemberStatus.PENDING) {
            throw new ConflictException("Only pending membership requests can be processed.");
        }
    }

    private void validateAssignableRole(Club club, ClubMemberRole role) {
        if (role == null) {
            throw new BadRequestException("A membership role is required.");
        }
        if (!isSupportedClubType(club)) {
            throw new BadRequestException("Membership roles are not supported for this club type.");
        }
        if (hasLeadershipRole(club, role)) {
            throw new ForbiddenException("Leadership roles cannot be assigned through member management.");
        }
    }

    private boolean hasLeadershipRole(Club club, ClubMemberRole role) {
        if (role == null) {
            return false;
        }
        String normalizedType = normalizeClubType(club.getClubType());
        return (isClubType(normalizedType) && role == ClubMemberRole.SECRETARY)
                || ("SOCIETY".equals(normalizedType) && role == ClubMemberRole.PRESIDENT);
    }

    private boolean isSupportedClubType(Club club) {
        String normalizedType = normalizeClubType(club.getClubType());
        return isClubType(normalizedType) || "SOCIETY".equals(normalizedType);
    }

    private boolean isClubType(String normalizedType) {
        return "CLUB".equals(normalizedType) || "STUDENTCLUB".equals(normalizedType);
    }

    private String normalizeClubType(String clubType) {
        return clubType == null
                ? ""
                : clubType.replaceAll("[\\s_-]+", "").toUpperCase(Locale.ROOT);
    }

    private ClubMemberRequestResponse toClubMemberRequestResponse(ClubMember member) {
        User user = member.getUser();
        String username = user == null ? null : user.getUsername();
        String fullName = user == null || user.getProfile() == null
                ? null
                : user.getProfile().getFullName();
        String avatarUrl = user == null || user.getProfile() == null
                ? null
                : user.getProfile().getAvatarUrl();
        if (fullName == null || fullName.isBlank()) {
            fullName = username;
        }

        return ClubMemberRequestResponse.builder()
                .clubId(member.getId().getClubId())
                .userId(member.getId().getUserId())
                .fullName(fullName)
                .username(username)
                .avatarUrl(avatarUrl)
                .role(member.getRole())
                .memberStatus(member.getMemberStatus())
                .requestedAt(member.getCreatedAt())
                .joinedAt(member.getJoinedAt())
                .build();
    }


    private int getMemberCount(Integer clubId) {
        return (int) clubMemberRepository
                .countByIdClubIdAndMemberStatus(
                        clubId,
                        ClubMemberStatus.APPROVED
                );
    }

    private ClubResponse buildClubResponse(                           //getClubs(),getMyClubs(),getUserClubs()
            Club club,
            Integer currentUserId
    ) {
        ClubMember member = getMembership(
                club.getId(),
                currentUserId
        );

        return buildClubResponse(club, member);
    }

    private ClubResponse buildClubResponse(
            Club club,
            ClubMember member
    ) {

        return ClubResponse.builder()
                .clubId(club.getId())
                .name(club.getName())
                .clubType(club.getClubType())
                .logoUrl(club.getLogoUrl())
                .memberCount(getMemberCount(club.getId()))
                .memberStatus(toResponseMemberStatus(club, member))
                .build();
    }

    private ClubResponseMemberStatus toResponseMemberStatus(
            Club club,
            ClubMember member
    ) {
        if (member == null || member.getMemberStatus() == null) {
            return null;
        }

        if (member.getMemberStatus() != ClubMemberStatus.APPROVED) {
            return ClubResponseMemberStatus.valueOf(member.getMemberStatus().name());
        }

        ClubMemberRole role = member.getRole();
        if (role == null) {
            return ClubResponseMemberStatus.APPROVED;
        }

        return hasLeadershipRole(club, role)
                ? ClubResponseMemberStatus.LEAD
                : ClubResponseMemberStatus.APPROVED;
    }



    private ClubDetailResponse buildClubDetailResponse(
            Club club,
            Integer currentUserId
    ) {

        ClubMember member = getMembership(
                club.getId(),
                currentUserId
        );

        return ClubDetailResponse.builder()
                .clubId(club.getId())
                .name(club.getName())
                .clubType(club.getClubType())
                .description(club.getDescription())
                .logoUrl(club.getLogoUrl())
                .memberCount(getMemberCount(club.getId()))
                .memberStatus(
                        member == null
                                ? null
                                : member.getMemberStatus()
                )
                .role(
                        member == null
                                ? null
                                : member.getRole()
                )
                .build();
    }
}
