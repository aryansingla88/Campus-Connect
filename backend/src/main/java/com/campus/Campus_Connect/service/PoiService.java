package com.campus.Campus_Connect.service;

import com.campus.Campus_Connect.model.Poi;
import com.campus.Campus_Connect.repository.PoiRepository;
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