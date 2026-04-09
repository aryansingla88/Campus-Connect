package com.campus.Campus_Connect.controller;

import com.campus.Campus_Connect.model.UserPresence;
import com.campus.Campus_Connect.service.UserPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/presence")
public class UserPresenceController {

    @Autowired
    private UserPresenceService service;


    @PostMapping("/update")
    public String update(@RequestBody UserPresence user) {
        service.updatePresence(user);
        return "Updated Successfully";
    }


    @GetMapping("/visible")
    public List<UserPresence> getVisibleUsers() {
        return service.getVisibleUsers();
    }
}