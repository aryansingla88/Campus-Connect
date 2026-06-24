package com.campus.Campus_Connect.phase1_archive.service;

import com.campus.Campus_Connect.phase1_archive.model.UserPresence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.campus.Campus_Connect.phase1_archive.repository.UserPresenceRepository;

//@Service
public class UserPresenceService {

    @Autowired
    private UserPresenceRepository repo;

    public void updatePresence(UserPresence user) {
        repo.updatePresence(user);
    }

    public List<UserPresence> getVisibleUsers() {
        return repo.getVisibleUsers();
    }

}