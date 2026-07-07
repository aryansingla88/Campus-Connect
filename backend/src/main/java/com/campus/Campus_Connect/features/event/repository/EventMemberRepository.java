package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.EventMember;
import com.campus.Campus_Connect.features.event.entity.EventMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventMemberRepository extends JpaRepository<EventMember, EventMemberId> {

}