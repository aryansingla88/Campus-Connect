package com.example.demo.controller;

import com.example.demo.model.UserPresence;
import com.example.demo.service.UserPresenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
}