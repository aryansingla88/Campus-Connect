package com.example.demo.service;

import com.example.demo.model.UserPresence;
import com.example.demo.repository.UserPresenceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserPresenceService {

    @Autowired
    private UserPresenceRepository repo;

    public void updatePresence(UserPresence user) {
        repo.updatePresence(user);
    }
}