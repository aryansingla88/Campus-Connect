package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.EventMemberId;
import com.campus.Campus_Connect.features.event.entity.enums.EventMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;

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
}