package com.campus.Campus_Connect.features.event.scheduler;

import com.campus.Campus_Connect.features.event.entity.Event;
import com.campus.Campus_Connect.features.event.repository.EventRepository;
import com.campus.Campus_Connect.features.event.service.priority.PriorityCalculatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPriorityScheduler {

    private final EventRepository eventRepository;
    private final PriorityCalculatorService priorityCalculatorService;

    // fixedRate = 600000
    @Scheduled(fixedRate = 600000)
    @Transactional
    public void runEventPriorityUpdate() {
        log.info("⏳ Starting Event Priority Algorithm Scheduler...");

        // 1. pick events for calculation (ACTIVE / UPCOMING)
        List<Event> applicableEvents = eventRepository.findEventsForPriorityCalculation();

        if (applicableEvents.isEmpty()) {
            log.info("✅ No active/upcoming events found to update.");
            return;
        }

        // 2. run for every event (Calculator)
        for (Event event : applicableEvents) {
            priorityCalculatorService.calculateAndUpdatePriority(event);
        }

        // 3. all events DB save (Batch Update)
        eventRepository.saveAll(applicableEvents);

        log.info("✅ Successfully updated priority for {} events.", applicableEvents.size());
    }
}