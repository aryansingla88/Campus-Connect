package com.campus.Campus_Connect.features.club.dto.response;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMemberRequestResponse {
    private Integer clubId;
    private Integer userId;
    private String fullName;
    private String username;
    private String avatarUrl;
    private ClubMemberRole role;
    private ClubMemberStatus memberStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime joinedAt;
}
