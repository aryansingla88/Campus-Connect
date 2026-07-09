package com.campus.Campus_Connect.features.club.dto.response;

import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClubMemberResponse {
    private Integer userId;
    private String fullName;
    private String avatarUrl;
    private ClubMemberStatus memberStatus;
    private ClubMemberRole role;
    private LocalDateTime joinedAt;
}
