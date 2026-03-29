package com.example.demo.controller;

import com.example.demo.model.Event;
import com.example.demo.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class EventController {

    @Autowired
    private EventService service;

    @PostMapping("/events")
    public Map<String, Object> createEvent(@RequestBody Event event) {

        int id = service.createEvent(event);

        Map<String, Object> res = new HashMap<>();
        res.put("status", "success");
        res.put("event_id", id);
        res.put("message", "Event created successfully");

        return res;
    }

    @GetMapping("/events")
    public List<Event> getEvents() {
        return service.getAllEvents();
    }
}