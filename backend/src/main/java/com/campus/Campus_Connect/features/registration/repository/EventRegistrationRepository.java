package com.campus.Campus_Connect.features.registration.repository;

import com.campus.Campus_Connect.features.registration.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRegistrationRepository
        extends JpaRepository<EventRegistration, Integer> {

    boolean existsByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    List<EventRegistration> findByEventId(
            Integer eventId
    );

    List<EventRegistration> findByUserId(
            Integer userId
    );

    Optional<EventRegistration> findByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    Optional<EventRegistration> findByIdAndEventId(
            Integer registrationId,
            Integer eventId
    );

    List<EventRegistration> findByEventIdAndTeamId(
            Integer eventId,
            Integer teamId
    );

    long countByEventIdAndTeamId(
            Integer eventId,
            Integer teamId
    );

    @Query("""
    SELECT r
    FROM EventRegistration r
    LEFT JOIN r.team t
    WHERE r.event.id = :eventId
      AND (
          r.team IS NULL
          OR (
              t.event.id = :eventId
              AND r.user.id = t.leader.id
          )
      )
""")
    List<EventRegistration> findShortRegistrations(
            @Param("eventId") Integer eventId
    );
}
