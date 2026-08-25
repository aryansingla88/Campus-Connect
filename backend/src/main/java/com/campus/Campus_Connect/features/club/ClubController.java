package com.campus.Campus_Connect.features.club;             //Management APIs

import com.campus.Campus_Connect.common.response.ApiResponse;
import com.campus.Campus_Connect.features.club.dto.response.*;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/users/me/clubs")
    public ApiResponse<List<ClubResponse>> getMyClubs() {
        return clubService.getMyClubs();
    }

    @GetMapping("/users/{userId}/clubs")
    public ApiResponse<List<ClubResponse>> getUserClubs(
            @PathVariable Integer userId
    ) {
        return clubService.getUserClubs(userId);
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

    @GetMapping("/options")
    public ApiResponse<List<ClubOptionResponse>> getClubOptions() {
        return clubService.getClubOptions();
    }

    @GetMapping("/{clubId}")
    public ApiResponse<ClubDetailResponse> getClub(
            @PathVariable Integer clubId
    ) {
        return clubService.getClub(clubId);
    }
}