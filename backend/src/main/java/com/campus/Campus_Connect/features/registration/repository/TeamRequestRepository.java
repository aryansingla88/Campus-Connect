package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.TeamRequest;
import com.campus.Campus_Connect.features.registration.entity.enums.TeamRequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRequestRepository extends JpaRepository<TeamRequest, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tr from TeamRequest tr where tr.id = :requestId")
    Optional<TeamRequest> findByIdForUpdate(@Param("requestId") Integer requestId);

    Optional<TeamRequest> findByRegistration_IdAndStatus(Integer registrationId, TeamRequestStatus status);

    List<TeamRequest> findByRegistration_User_IdOrderByCreatedAtDesc(Integer userId);

    List<TeamRequest> findByTeam_IdOrderByCreatedAtDesc(Integer teamId);
}
