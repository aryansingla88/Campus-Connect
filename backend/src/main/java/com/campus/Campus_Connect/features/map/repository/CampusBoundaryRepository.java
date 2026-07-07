package com.campus.Campus_Connect.features.map.repository;

import com.campus.Campus_Connect.features.map.entity.CampusBoundaryPoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampusBoundaryRepository
        extends JpaRepository<CampusBoundaryPoint, Integer> {

    List<CampusBoundaryPoint> findAllByOrderByPointOrderAsc();

}