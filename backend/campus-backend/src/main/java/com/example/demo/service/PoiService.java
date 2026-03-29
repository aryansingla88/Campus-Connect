package com.example.demo.service;

import com.example.demo.model.Poi;
import com.example.demo.repository.PoiRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PoiService {

    @Autowired
    private PoiRepository poiRepository;

    public List<Poi> getAllPois() {
        return poiRepository.getAllPois();
    }
}