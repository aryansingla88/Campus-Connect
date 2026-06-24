package com.campus.Campus_Connect.phase1_archive.service;

import com.campus.Campus_Connect.phase1_archive.model.Event;
import com.campus.Campus_Connect.phase1_archive.repository.EventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
public class EventService {

    @Autowired
    private EventRepository repo;

    public int createEvent(Event event) {
        return repo.createEvent(event);
    }

    public List<Event> getAllEvents() {
        return repo.getAllEvents();
    }
}