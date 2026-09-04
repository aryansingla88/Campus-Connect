package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.EventMemberId;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventMemberRepository extends JpaRepository<EventMember, EventMemberId> {
    Optional<EventMember> findByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    List<EventMember> findByEventIdAndRoleIn(
            Integer eventId,
            List<EventMemberRole> roles
    );

    boolean existsByEventIdAndUserId(
            Integer eventId,
            Integer userId
    );

    long countByUser_IdAndRole(
            Integer userId,
            EventMemberRole role
    );

    @Query("""
    SELECT em.event
    FROM EventMember em
    WHERE em.user.id = :userId
      AND em.role = com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole.CREATOR
""")
    List<Event> findMyEvents(
            @Param("userId") Integer userId
    );

    @Query("""
    SELECT em.event
    FROM EventMember em
    WHERE em.user.id = :userId
      AND em.role = com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole.ADMIN
""")
    List<Event> findSharedEvents(
            @Param("userId") Integer userId
    );

    @Query("""
    SELECT em.event
    FROM EventMember em
    WHERE em.user.id = :userId
      AND em.role IN (
          com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole.CREATOR,
          com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole.ADMIN
      )
""")
    List<Event> findManagedEvents(
            @Param("userId") Integer userId
    );
}