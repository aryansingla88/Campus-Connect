package com.campus.Campus_Connect.controller;

import com.campus.Campus_Connect.model.Poi;
import com.campus.Campus_Connect.service.PoiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/pois")
public class PoiController {

    @Autowired
    private PoiService poiService;

    @GetMapping
    public List<Poi> getPois() {
        return poiService.getAllPois();
    }

}