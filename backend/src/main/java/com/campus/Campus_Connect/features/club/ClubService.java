package com.campus.Campus_Connect.features.club;

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.common.security.SecurityUtils;
import com.campus.Campus_Connect.features.auth.entity.User;
import com.campus.Campus_Connect.features.auth.repository.UserRepository;
import com.campus.Campus_Connect.features.club.dto.response.*;
import com.campus.Campus_Connect.features.club.entity.Club;
import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import com.campus.Campus_Connect.features.club.repository.ClubMemberRepository;
import com.campus.Campus_Connect.features.club.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

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

        ClubMember member = ClubMember.builder()
                .id(new ClubMemberId(clubId, currentUser.getId()))
                .club(club)
                .user(currentUser)
                .memberStatus(ClubMemberStatus.PENDING)
                .role(ClubMemberRole.MEMBER)
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
                            currentUser.getId()
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

        User currentUser = SecurityUtils.getCurrentUser();

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
                            currentUser.getId()
                    )
            );
        }

        return ApiResponse.success(
                response,
                "User's clubs fetched successfully."
        );
    }

    //------------------------------------------------------
    @Transactional(readOnly = true)
    public ApiResponse<List<ClubOptionResponse>> getClubOptions() {

        List<ClubOptionResponse> response = clubRepository.findAll()
                .stream()
                .map(club -> ClubOptionResponse.builder()
                        .clubId(club.getId())
                        .name(club.getName())
                        .build())
                .toList();

        return ApiResponse.success(
                response,
                "Club options fetched successfully."
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

        return ClubResponse.builder()
                .clubId(club.getId())
                .name(club.getName())
                .logoUrl(club.getLogoUrl())
                .memberCount(getMemberCount(club.getId()))
                .memberStatus(
                        member == null
                                ? null
                                : member.getMemberStatus()
                )
                .build();
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