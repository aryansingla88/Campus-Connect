package com.example.demo.controller;

import com.example.demo.model.Poi;
import com.example.demo.service.PoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class PoiController {

    @Autowired
    private PoiService poiService;

    @GetMapping("/pois")
    public List<Poi> getPois() {
        return poiService.getAllPois();
    }
}