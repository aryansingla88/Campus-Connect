package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Integer> {
}