package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventTeam;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventTeamRepository
        extends JpaRepository<EventTeam, Integer> {

    Optional<EventTeam> findByEvent_IdAndTeamName(
            Integer eventId,
            String teamName
    );

    List<EventTeam> findAllByEvent_Id(
            Integer eventId
    );
}