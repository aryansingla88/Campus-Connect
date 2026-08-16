package com.campus.Campus_Connect.features.event.repository;

import com.campus.Campus_Connect.features.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;


public interface EventRepository extends JpaRepository<Event, Integer> {

    List<Event> findByCreator_Id(
            Integer creatorId
    );

    // 1. App ki Feed ke liye: Seedha Level (6 to 1) aur Score ke basis par sorted
    // Note: Agar tumhara EventState enum me 'ACTIVE' ki jagah 'PUBLISHED' ya 'ONGOING' hai, toh usko replace kar lena.
    @Query("SELECT e FROM Event e WHERE e.eventState = 'ACTIVE' ORDER BY e.priorityLevel DESC, e.priorityScore DESC")
    List<Event> findActiveEventsForFeed();

    // 2. Scheduler ke liye: Sirf un events ko uthao jinki calculation karni hai
    @Query("SELECT e FROM Event e WHERE e.eventState IN ('ACTIVE', 'UPCOMING')")
    List<Event> findEventsForPriorityCalculation();
}