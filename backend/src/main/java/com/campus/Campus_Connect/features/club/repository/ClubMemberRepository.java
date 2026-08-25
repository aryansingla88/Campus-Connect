package com.campus.Campus_Connect.features.club.repository;

import com.campus.Campus_Connect.features.club.entity.ClubMember;
import com.campus.Campus_Connect.features.club.entity.ClubMemberId;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberRole;
import com.campus.Campus_Connect.features.club.entity.enums.ClubMemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClubMemberRepository extends JpaRepository<ClubMember, ClubMemberId> {

    Optional<ClubMember> findByIdClubIdAndIdUserId(
            Integer clubId,
            Integer userId
    );

    boolean existsByIdClubIdAndIdUserId(
            Integer clubId,
            Integer userId
    );

    void deleteByIdClubIdAndIdUserId(
            Integer clubId,
            Integer userId
    );

    List<ClubMember> findAllByIdUserIdAndMemberStatus(
            Integer userId,
            ClubMemberStatus memberStatus
    );

    List<ClubMember> findAllByIdClubId(
            Integer clubId
    );

    List<ClubMember> findAllByIdClubIdAndMemberStatus(
            Integer clubId,
            ClubMemberStatus memberStatus
    );

    List<ClubMember> findAllByIdClubIdAndRole(
            Integer clubId,
            ClubMemberRole role
    );

    long countByIdClubIdAndMemberStatus(
            Integer clubId,
            ClubMemberStatus memberStatus
    );

    long countByIdUserIdAndMemberStatus(
            Integer userId,
            ClubMemberStatus memberStatus
    );
}