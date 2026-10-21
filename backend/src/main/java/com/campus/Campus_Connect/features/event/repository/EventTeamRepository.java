package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface EventTeamRepository
        extends JpaRepository<EventTeam, Integer> {

    Optional<EventTeam> findByEvent_IdAndTeamName(
            Integer eventId,
            String teamName
    );

    boolean existsByEvent_IdAndTeamNameIgnoreCase(Integer eventId, String teamName);

    List<EventTeam> findAllByEvent_Id(
            Integer eventId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from EventTeam t where t.id = :teamId")
    Optional<EventTeam> findByIdForUpdate(@Param("teamId") Integer teamId);
}
