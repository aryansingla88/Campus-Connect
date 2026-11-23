package com.campus.Campus_Connect.features.club;             //Management APIs

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.club.dto.request.ApproveClubRequest;
import com.campus.Campus_Connect.features.club.dto.request.AssignClubMemberRoleRequest;
import com.campus.Campus_Connect.features.club.dto.response.*;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @GetMapping
    public ApiResponse<List<ClubResponse>> getClubs() {
        return clubService.getClubs();
    }

    @GetMapping("/users/me")
    public ApiResponse<List<ClubResponse>> getMyClubs() {
        return clubService.getMyClubs();
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<List<ClubResponse>> getUserClubs(
            @PathVariable Integer userId
    ) {
        return clubService.getUserClubs(userId);
    }

    @GetMapping("/{clubId}/requests")
    public ApiResponse<List<ClubMemberRequestResponse>> getPendingRequests(
            @PathVariable Integer clubId
    ) {
        return clubService.getPendingClubRequests(clubId);
    }

    @PostMapping("/{clubId}/requests/{userId}/approve")
    public ApiResponse<ClubMemberRequestResponse> approveRequest(
            @PathVariable Integer clubId,
            @PathVariable Integer userId,
            @Valid @RequestBody(required = false) ApproveClubRequest request
    ) {
        return clubService.approveClubRequest(clubId, userId, request);
    }

    @PostMapping("/{clubId}/requests/{userId}/reject")
    public ApiResponse<ClubMemberRequestResponse> rejectRequest(
            @PathVariable Integer clubId,
            @PathVariable Integer userId
    ) {
        return clubService.rejectClubRequest(clubId, userId);
    }

    @PatchMapping("/{clubId}/members/{userId}/role")
    public ApiResponse<ClubMemberRequestResponse> assignMemberRole(
            @PathVariable Integer clubId,
            @PathVariable Integer userId,
            @Valid @RequestBody AssignClubMemberRoleRequest request
    ) {
        return clubService.assignClubMemberRole(clubId, userId, request);
    }

    @PostMapping("/{clubId}/join")
    public ApiResponse<ClubMembershipResponse> joinClub(
            @PathVariable Integer clubId
    ) {
        return clubService.joinClub(clubId);
    }

    @DeleteMapping("/{clubId}/leave")
    public ApiResponse<ClubMembershipResponse> leaveClub(
            @PathVariable Integer clubId
    ) {
        return clubService.leaveClub(clubId);
    }

    @GetMapping("/{clubId}")
    public ApiResponse<ClubDetailResponse> getClub(
            @PathVariable Integer clubId
    ) {
        return clubService.getClub(clubId);
    }

    @GetMapping("/{clubId}/members")
    public ApiResponse<List<ClubMemberResponse>> getClubMembers(
            @PathVariable Integer clubId
    ) {
        return clubService.getClubMembers(clubId);
    }
}
